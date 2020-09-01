package org.getaviz.generator.abap.city.steps;

import org.apache.commons.lang3.math.NumberUtils;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.run.local.common.Maps;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ACityMetaDataExporter {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private SourceNodeRepository nodeRepository;
    private ACityRepository aCityRepository;

    public ACityMetaDataExporter(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository) {
        this.nodeRepository = sourceNodeRepository;
        this.aCityRepository = aCityRepository;
    }

    public void exportMetaDataFile() {
        Writer fw = null;
        try {
            File outputDir = new File(config.getOutputMap());
            String path = outputDir.getAbsolutePath() + "/metaData.json";
            fw = new FileWriter(path);
            fw.write(toJSON(nodeRepository.getNodes()));
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
        Collection<Node> nodes = nodeRepository.getNodes();
        for (final Node node : nodes) {
            String metaData = toMetaData(node);
            ACityElement aCityElement = aCityRepository.getElementBySourceID(node.id());
            if (aCityElement == null) {
                continue;
            }

            aCityElement.setMetaData("{" + metaData + "}");
        }
    }

    private String toJSON(Collection<Node> nodes) {
        StringBuilder metaDataFile = new StringBuilder();
        boolean hasElements = false;
        for (final Node node : nodes) {
            // Some elements are not in aCityRep; Example - standard SAP-packages
            ACityElement element = aCityRepository.getElementBySourceID(node.id());
            if (element == null) {
                continue;
            }

            if (!hasElements) {
                hasElements = true;
                metaDataFile.append("[{");
            } else {
                metaDataFile.append("\n},{");
            }
            metaDataFile.append("\n");
            metaDataFile.append(toMetaData(node));
        }
        if (hasElements) {
            metaDataFile.append("}]");
        }
        return metaDataFile.toString();
    }

    private String toMetaData(Node node) {
        StringBuilder builder = new StringBuilder();
        ACityElement element = aCityRepository.getElementBySourceID(node.id());

        Arrays.asList(SAPNodeProperties.values()).forEach(prop -> {
            if (prop == SAPNodeProperties.element_id) {
                if (element != null) {
                    builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": \"" + element.getHash() + "\"," +"\n");
                }

                return; // Jump to the next prop
            }

            // If there is no value for property, then don't write
            if (node.get(prop.toString()).isNull()) {
                return;
            }

            // Write strings with "" and numbers without
            String propValue = node.get(prop.toString()).toString().replaceAll("\"", "");

            // Belongs to - must be hash value of a parent container
            if (prop == SAPNodeProperties.container_id) {
                propValue = getContainerHash(node, prop.toString());
            }

            if (NumberUtils.isCreatable(propValue)) {
                builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": " + propValue + "," + "\n");
            } else {
                builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": \"" + propValue + "\"," + "\n");
            }
        });

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

    public String getContainerHash(Node node, String prop) {
        Long container_id = Long.parseLong(node.get(prop).asString());
        StatementResult results = connector.executeRead("MATCH (aCityNode)-[r:SOURCE]->(sourceNode) " +
                "WHERE sourceNode.element_id = "+ "\'" + container_id + "\'" +
                " RETURN aCityNode.hash");

        if (results.hasNext()) {
            Map<String,Object> row = results.next().asMap();
            return row.get("aCityNode.hash").toString();
        }

        // If no hash was found, for example default SAP packages, no container_id will be written.
        return "";
    }
}
