package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.io.File;


public class Loader {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance("bolt://localhost:7687");

    public static void main(String[] args) {
        boolean isSilentMode = true;
        File nodesCsvFile = new File("src/test/neo4jexport/integration/20200214_Test.csv");
        File TypeOfRelationsCsvFile = new File("src/test/neo4jexport/20200214_Test_TypeOf.csv");
        String pathToNodesCsv = nodesCsvFile.getAbsolutePath();
        String pathToTypeOfRelationsCsv = TypeOfRelationsCsvFile.getAbsolutePath();

        Scanner userInput = new Scanner(System.in);
        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();

        if (input.equals("n")) {
            isSilentMode = false;
            // Get list of files and ask which of them to import
            List<Path> files = config.getInputCSVFiles();
            System.out.println("Files found:");
            for(Path p : files) {
                System.out.print(files.indexOf(p) + ") ");
                System.out.println(p);
            }

            System.out.print("\nChoose index of the file for nodes (SAPExportCreateNodes): ");
            input = userInput.nextLine();
            pathToNodesCsv = files.get(Integer.parseInt(input)).toString();

            System.out.print("Choose index of the file for typeOfRelations (SAPExportCreateTypeOfRelations): ");
            input = userInput.nextLine();
            pathToTypeOfRelationsCsv = files.get(Integer.parseInt(input)).toString();
        }

        // Make sure the graph is empty
        connector.executeWrite("MATCH (n) DETACH DELETE n;");

        // 1. Upload nodes
        System.out.println("SAPExportCreateNodes: " + pathToNodesCsv);
        if (!isSilentMode) {
            System.out.print("\nLoading nodes in Neo4j. Press any key to continue...");
            userInput.nextLine();
        }
        pathToNodesCsv = pathToNodesCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToNodesCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "CREATE (n:Elements)\n" +
                        "SET n = row"
        );

        // 2. Upload relations
        if (!isSilentMode) {
            System.out.print("\nCreating 'CONTAINS' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        connector.executeWrite("MATCH (a:Elements), (b:Elements) WHERE a.element_id = b.container_id CREATE (a)-[r:CONTAINS]->(b)");

        // 3. Upload TypeOfRelations
        System.out.println("SAPExportCreateTypeOfRelations: " + pathToTypeOfRelationsCsv);
        if (!isSilentMode) {
            System.out.print("\nCreating 'TYPEOF' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        pathToTypeOfRelationsCsv = pathToTypeOfRelationsCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToTypeOfRelationsCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "MATCH (a:Elements {element_id: row.element_id}), (b:Elements {element_id: row.type_of_id})\n" +
                        "CREATE (a)-[r:TYPEOF]->(b)"
        );

        connector.close();
        System.out.println("Loader step was completed");
    }
}
