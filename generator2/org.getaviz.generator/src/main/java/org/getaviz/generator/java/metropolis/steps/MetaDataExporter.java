package org.getaviz.generator.java.metropolis.steps;

import org.apache.commons.lang3.math.NumberUtils;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeProperties;
import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.generator.java.enums.JavaRelationLabels;
import org.getaviz.generator.repository.ACityElement;
import org.getaviz.generator.repository.ACityRepository;
import org.getaviz.generator.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.run.local.java.common.Maps;
import org.neo4j.driver.v1.types.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MetaDataExporter {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private SourceNodeRepository nodeRepository;
    private ACityRepository aCityRepository;

    public MetaDataExporter(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository) {
        this.nodeRepository = sourceNodeRepository;
        this.aCityRepository = aCityRepository;
    }

    public void exportMetaDataFile() {
        Writer fw = null;
        try {
            File outputDir = new File(config.getOutputMap());
            String path = outputDir.getAbsolutePath() + "/metaData.json";
            fw = new FileWriter(path);
            fw.write(toJSON(aCityRepository.getAllElements()));
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void setMetaDataPropToACityElements() {
        Collection<ACityElement> aCityElements = aCityRepository.getAllElements();
        for (final ACityElement element : aCityElements) {
            //skip reference buildings (only Metropolis)
            if (element.getSourceNode() == null) {
                if (element.getType() == ACityElement.ACityType.Reference) {
                    String metaData = toMetaDataForReferenceElements(element);
                    element.setMetaData("{" + metaData + "}");
                } else {
                    continue;
                }
            } else {
                String metaData = toMetaData(element);
                element.setMetaData("{" + metaData + "}");
            }
        }
    }

    private String toJSON(Collection<ACityElement> elements) {
        StringBuilder metaDataFile = new StringBuilder();
        boolean hasElements = false;
        for (final ACityElement element: elements) {
            if (element.getSourceNode() == null) {
                if (element.getType() == ACityElement.ACityType.Reference) {
                    if (!hasElements) {
                        hasElements = true;
                        metaDataFile.append("[{");
                    } else {
                        metaDataFile.append("\n},{");
                    }
                    metaDataFile.append("\n");
                    metaDataFile.append(toMetaDataForReferenceElements(element));
                } else {
                    continue;
                }
            } else {
                if (!hasElements) {
                    hasElements = true;
                    metaDataFile.append("[{");
                } else {
                    metaDataFile.append("\n},{");
                }
                metaDataFile.append("\n");
                metaDataFile.append(toMetaData(element));
            }
        }
        if (hasElements) {
            metaDataFile.append("}]");
        }
        return metaDataFile.toString();
    }

    private String toMetaData(ACityElement element) {
        StringBuilder builder = new StringBuilder();

        // Add element hash
        builder.append("\""+ Maps.getMetaDataProperty(JavaNodeProperties.element_id.name()) + "\": \"" + element.getHash() + "\"," +"\n");
        // Add qualifiedName
        builder.append("\"qualifiedName\": \"" + getQualifiedName(element) + "\",\n");
        // Add node information
        builder.append(getNodeMetaInfo(element));
        // Add relations
        builder.append(getRelationsMetaInfo(element));
        // Add additional meta
        builder.append(getAdditionalMetaInfo(element));

        // Make sure we have the right syntax -> no commas at the end
        char lastChar = builder.charAt(builder.length() - 1);
        if (Character.compare(lastChar, '\n') == 0) {
            lastChar = builder.charAt(builder.length() - 2);

            if (Character.compare(lastChar, ',') == 0) {
                builder.deleteCharAt(builder.length() - 1); // Delete '\n'
                builder.deleteCharAt(builder.length() - 1); // Delete ,
            }
        }

        return builder.toString();
    }

    private String toMetaDataForReferenceElements(ACityElement element) {
        StringBuilder builder = new StringBuilder();

        // Add element hash
        builder.append("\"id\": \"" + element.getHash() + "\"," +"\n");
        // Add Belongs to
        builder.append("\"belongsTo\": \"" + element.getParentElement().getHash() + "\",\n");
        // Add Name
        builder.append("\"type\": \"" + element.getType() + "\",\n");
        // Add Type
        builder.append("\"name\": \"" + element.getSubType() + "\",\n");

        // Make sure we have the right syntax -> no commas at the end
        char lastChar = builder.charAt(builder.length() - 1);
        if (Character.compare(lastChar, '\n') == 0) {
            lastChar = builder.charAt(builder.length() - 2);

            if (Character.compare(lastChar, ',') == 0) {
                builder.deleteCharAt(builder.length() - 1); // Delete '\n'
                builder.deleteCharAt(builder.length() - 1); // Delete ,
            }
        }

        return builder.toString();
    }

    private String getNodeMetaInfo(ACityElement element) {
        StringBuilder builder = new StringBuilder();
        Node node = element.getSourceNode();
        // For some accessory elements there is no source node
        if (node == null) {
            return "";
        }
        Arrays.asList(JavaNodeProperties.values()).forEach(prop -> {
            if (prop == JavaNodeProperties.element_id) {
                return; // already added as first prop by toMetaData()
            }

            // Don't write properties  with NULL value
            if (node.get(prop.toString()).isNull()) {
                return;
            }

            // Remove extra "" (written by Neo4j)
            String propValue = node.get(prop.toString()).toString().replaceAll("\"", "");

            // Write strings with quotation marks and numbers without
            if (NumberUtils.isCreatable(propValue)) {
                builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": " + propValue + "," + "\n");
            } else {
                builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": \"" + propValue + "\"," + "\n");
            }
        });

        return builder.toString();
    }

    private String getRelationsMetaInfo(ACityElement element) {
        StringBuilder builder = new StringBuilder();
        Node node = element.getSourceNode();

        // For some accessory elements there is no source node
        if (node == null) {
            return "";
        }

        if (element.getParentElement() != null) {
            builder.append("\"belongsTo\": \"" + element.getParentElement().getHash() + "\",\n");
        }

        // Add INVOKES and EXTENDS relations
        String nodeType = node.get("type_name").asString();
        if (Maps.getNodesWithReferencesRelationByType().contains(nodeType)) {
            builder.append("\"calls\": \"" + getRelations(node, JavaRelationLabels.INVOKES, true) + "\",\n");
            builder.append("\"calledBy\": \"" + getRelations(node, JavaRelationLabels.INVOKES, false) + "\",\n");
        }

        if (Maps.getNodesWithExtendsRelationByType().contains(nodeType)) {
            builder.append("\"subClassOf\": \"" + getRelations(node, JavaRelationLabels.EXTENDS, true) + "\",\n");
            builder.append("\"superClassOf\": \"" + getRelations(node, JavaRelationLabels.EXTENDS, false) + "\",\n");
        }

        return builder.toString();
    }

    private String getAdditionalMetaInfo(ACityElement element) {
        StringBuilder builder = new StringBuilder();
        Node node = element.getSourceNode();
        String nodeType = node.get("type_name").asString();
        //signature for methods
        if (nodeType.equals(JavaNodeTypes.Method.name())) {
            builder.append("\"signature\": \"" + "" + "\",\n");
        }

        return builder.toString();
    }

    private String getQualifiedName(ACityElement element) {
        StringBuilder qualifiedNameBuilder = new StringBuilder();
        Node node = element.getSourceNode();
        String type = element.getSourceNodeType();
        if (type.equals(JavaNodeTypes.Package.name())) {
            qualifiedNameBuilder.append(node.get("fileName").asString());
        } else if (type.equals(JavaNodeTypes.Class.name()) || type.equals(JavaNodeTypes.Interface.name())) {
            qualifiedNameBuilder.append(node.get("fileName").asString().split("\\.")[0]);
        } else if (type.equals(JavaNodeTypes.Method.name()) || type.equals(JavaNodeTypes.Field.name())){
            Collection<Node> parentNodes = nodeRepository.getRelatedNodes(node, JavaRelationLabels.DECLARES, false);
            qualifiedNameBuilder.append(parentNodes.iterator().next().get("fileName").asString().split("\\.")[0]);
            qualifiedNameBuilder.append(".");
            qualifiedNameBuilder.append(node.get("name").asString());
        }

        return qualifiedNameBuilder.toString();
    }

    private String getContainerHash(Node node) {
        Collection<Node> parentNodes = nodeRepository.getRelatedNodes(node, JavaRelationLabels.CONTAINS, false);
        if (parentNodes.isEmpty()) {
            parentNodes = nodeRepository.getRelatedNodes(node, JavaRelationLabels.DECLARES, false);
            if (parentNodes.isEmpty()) {
                return "";
            }
        }

        Node parentNode = parentNodes.iterator().next();

        ACityElement parentElement = aCityRepository.getElementBySourceID(parentNode.id());
        if (parentElement == null) {  // Some SAP standard packages may not included
            return "";
        }
        return parentElement.getHash();
    }

    private String getRelations(Node node, JavaRelationLabels label, Boolean direction) {
        Collection<Node> nodes = nodeRepository.getRelatedNodes(node, label, direction);
        if (nodes.isEmpty()) {
            return "";
        }

        List<String> nodesHashes = getNodesHashes(nodes);
        return String.join(", ", nodesHashes); //returns "hash, hash_2, hash*"
    }

    private List<String> getNodesHashes(Collection<Node> nodes) {
        List<String> nodesHashes = new ArrayList<>();
        for (Node node : nodes) {
            Long nodeId = node.id();
            ACityElement aCityElement = aCityRepository.getElementBySourceID(nodeId);
            if (aCityElement == null) {
                continue;
            }

            nodesHashes.add(aCityElement.getHash());
        }
        return nodesHashes;
    }
}
