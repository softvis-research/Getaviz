package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.database.DatabaseConnector;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Loader {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    public static void main(String[] args) {
        SettingsConfiguration.getInstance("src/test/resources/ABAPCityTest.properties");
        boolean isSilentMode = true;
        String pathToNodesCsv = "";
        String pathToTypeOfRelationsCsv = "";
        String pathToReferenceCsv = "";
        String pathToInheritanceCsv = "";

        Scanner userInput = new Scanner(System.in);
        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();
        if (input.equals("n")) {
            isSilentMode = false;
        }

        // Get files for nodes and typeofs
        List<Path> files = config.getInputCSVFiles();
        for(Path p : files) {
            if (p.toString().endsWith("_Test.csv")) {
                pathToNodesCsv = p.toString();
            } else if (p.toString().endsWith("_TypeOf.csv")) {
                pathToTypeOfRelationsCsv = p.toString();
            } else if (p.toString().endsWith("_Reference.csv")) {
                pathToReferenceCsv = p.toString();
            } else if (p.toString().endsWith("_Inheritance.csv")) {
                pathToInheritanceCsv = p.toString();
            }
        }

        if (pathToNodesCsv.isEmpty() || pathToTypeOfRelationsCsv.isEmpty()
                || pathToInheritanceCsv.isEmpty() || pathToReferenceCsv.isEmpty()) {
            System.out.println("Some input file wasn't found");
            System.exit(0);
        }


        // Make sure the graph is empty
        connector.executeWrite("MATCH (n) DETACH DELETE n;");

        // 1. Upload nodes
        System.out.println("SAPExportCreateNodes: " + pathToNodesCsv);
        if (!isSilentMode) {
            System.out.print("Loading nodes in Neo4j. Press any key to continue...");
            userInput.nextLine();
        }
        pathToNodesCsv = pathToNodesCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToNodesCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "CREATE (n:Elements)\n" +
                        "SET n = row"
        );
        // Rewrite date fields (to be able to filter by date)
        connector.executeWrite("MATCH (n) " +
                "WHERE (n.created <> \"00000000\" AND n.created <> \"0\") " +
                "SET n.created = date(n.created)");
        connector.executeWrite("MATCH (n) WHERE (n.created = \"00000000\") SET n.created = \"0\"");
        connector.executeWrite("MATCH (n) " +
                "WHERE (n.changed <> \"00000000\" AND n.changed <> \"0\") " +
                "SET n.changed = date(n.changed)");
        connector.executeWrite("MATCH (n) WHERE (n.changed = \"00000000\") SET n.changed = \"0\"");

        // Convert Number_of_statements string to int
        connector.executeWrite("MATCH (n) SET n.number_of_statements = toInteger(n.number_of_statements)");


        // 2. Upload relations  
        if (!isSilentMode) {
            System.out.print("Creating 'CONTAINS' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        connector.executeWrite("MATCH (a:Elements), (b:Elements) " +
                "WHERE a.element_id = b.container_id " +
                "CREATE (a)-[r:" + SAPRelationLabels.CONTAINS + "]->(b)"
        );


        // 3. Upload TypeOfRelations
        System.out.println("SAPExportCreateTypeOfRelations: " + pathToTypeOfRelationsCsv);
        if (!isSilentMode) {
            System.out.print("Creating 'TYPEOF' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        pathToTypeOfRelationsCsv = pathToTypeOfRelationsCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToTypeOfRelationsCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "MATCH (a:Elements {element_id: row.element_id}), (b:Elements {element_id: row.type_of_id})\n" +
                        "CREATE (a)-[r:"+ SAPRelationLabels.TYPEOF +"]->(b)"
        );


        // 4. Upload References
        System.out.println("Path to References CSV: " + pathToReferenceCsv);
        if (!isSilentMode) {
            System.out.print("Creating 'USES' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        pathToReferenceCsv = pathToReferenceCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToReferenceCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "MATCH (a:Elements {element_id: row.source_id}), (b:Elements {element_id: row.target_id})\n" +
                        "CREATE (a)-[r:"+ SAPRelationLabels.USES +"]->(b)"
        );


        // 5. Upload Inheritances
        System.out.println("Path to Inheritances CSV: " + pathToInheritanceCsv);
        if (!isSilentMode) {
            System.out.print("Creating 'INHERIT' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        pathToInheritanceCsv = pathToInheritanceCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToInheritanceCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "MATCH (a:Elements {element_id: row.subclass_id}), (b:Elements {element_id: row.superclass_id})\n" +
                        "CREATE (a)-[r:"+ SAPRelationLabels.INHERIT +"]->(b)"
        );

        connector.close();
        System.out.println("Loader step was completed");
    }
}
