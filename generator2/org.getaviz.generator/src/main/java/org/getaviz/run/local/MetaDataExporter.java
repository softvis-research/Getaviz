package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.run.local.common.Maps;
import org.neo4j.driver.v1.types.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

public class MetaDataExporter {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;

    public static void main(String[] args) {
        boolean isSilentMode = true;
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
        aCityRepository = new ACityRepository();


        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        Writer fw = null;
        try {
            File currentDir = new File("src/test/neo4jexport");
            String path = currentDir.getAbsolutePath() + "/metaData.json";
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

        System.out.println("\nMetaDataExporter step was completed\"");
        connector.close();
    }

    private static String toJSON(Collection<Node> nodes) {
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

            // write data to Neo4j as property
            StringBuilder metaDataNeo = new StringBuilder();
            metaDataNeo.append("\"{");
            metaDataNeo.append(toMetaData(node).replaceAll("\"", "\'")); // "- are not allowed
            metaDataNeo.append("}\"");

            connector.executeWrite(
                    "MATCH (n:Elements) WHERE ID(n) = " + node.id()
                            + " SET n.metaData = " + metaDataNeo.toString()
            );
        }
        if (hasElements) {
            metaDataFile.append("}]");
        }
        return metaDataFile.toString();
    }

    private static String toMetaData(Node node) {
        StringBuilder builder = new StringBuilder();
        ACityElement element = aCityRepository.getElementBySourceID(node.id());

        Arrays.asList(SAPNodeProperties.values()).forEach(prop -> {
            if (prop.toString().equals("element_id")) {
                if (element != null) {
                    builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": \"" + element.getHash() + "\"," +"\n");
                }

                return; // Jump to the next prop
            }

            if (!node.get(prop.toString()).toString().replace("\"", "").equals("NULL")) {
                builder.append("\""+ Maps.getMetaDataProperty(prop.toString()) + "\": " + node.get(prop.toString()).toString() + "," + "\n");
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
}
