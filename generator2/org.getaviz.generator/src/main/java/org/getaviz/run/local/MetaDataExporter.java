package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.types.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class MetaDataExporter {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;

    public static void main(String[] args) {
        boolean isSilentMode = true;

        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);

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
        builder.append("\"id\": " + node.get("element_id").toString().replace("\"", "") + "," +"\n");
        builder.append("\"name\": " + node.get("object_name").toString() + "," + "\n");

        // Print only IF NOT NULL
        if (!node.get("creator").toString().replace("\"", "").equals("NULL")) {
            builder.append("\"creator\": " + node.get("creator").toString() + "," + "\n");
        }
        if (!node.get("created").toString().replace("\"", "").equals("0")) {
            builder.append("\"created\": " + node.get("created").toString() + "," + "\n");
        }
        if (!node.get("changed_by").toString().replace("\"", "").equals("NULL")) {
            builder.append("\"changed_by\": " + node.get("changed_by").toString() + "," + "\n");
        }
        if (!node.get("changed").toString().replace("\"", "").equals("0")) {
            builder.append("\"changed\": " + node.get("changed").toString() + "," + "\n");
        }

        builder.append("\"type\": " + node.get("type").toString() + "," + "\n");
        builder.append("\"type_name\": " + node.get("type_name").toString() + "," + "\n");

        // Print only IF NOT NULL
        if (!node.get("datatype").toString().replace("\"", "").equals("NULL")) {
            builder.append("\"datatype\": " + node.get("datatype").toString() + "," + "\n");
        }
        if (!node.get("modifiers").toString().replace("\"", "").equals("NULL")) {
            builder.append("\"modifiers\": " + node.get("modifiers").toString() + "," + "\n");
        }

        // By some standard packages there is no container_id
        if (!node.get("container_id").toString().replace("\"", "").equals("NULL")) {
            builder.append("\"container_id\": "+ node.get("container_id").toString().replace("\"", ""));
        }

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
