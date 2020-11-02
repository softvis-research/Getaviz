package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.database.DatabaseConnector;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class LoaderStep {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    public static void main(String[] args) {
        SettingsConfiguration.getInstance("src/test/resources/ABAPCityTest.properties");
        boolean isSilentMode = true;
        String pathToNodesCsv = "";
        String pathToReferenceCsv = "";
        String pathToInheritanceCsv = "";
        String pathToMigrationFindingsCsv = "";

        Scanner userInput = new Scanner(System.in);
        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();
        if (input.equals("n")) {
            isSilentMode = false;
        }

        // Get files for nodes and relations
        List<Path> files = config.getInputCSVFiles();
        for(Path p : files) {
            if (p.toString().endsWith("_Test.csv")) {
                pathToNodesCsv = p.toString();
            } else if (p.toString().endsWith("_Reference.csv")) {
                pathToReferenceCsv = p.toString();
            } else if (p.toString().endsWith("_Inheritance.csv")) {
                pathToInheritanceCsv = p.toString();
            } else if (p.toString().endsWith("_export.csv")) {
                pathToMigrationFindingsCsv = p.toString();
            }
        }

        if (pathToNodesCsv.isEmpty() || pathToInheritanceCsv.isEmpty() || pathToReferenceCsv.isEmpty()
                || (config.addMigrationFindings() && pathToMigrationFindingsCsv.isEmpty())) {
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


        // 2. Upload contains relations
        if (!isSilentMode) {
            System.out.print("Creating 'CONTAINS' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        connector.executeWrite("MATCH (a:Elements), (b:Elements) " +
                "WHERE a.element_id = b.container_id " +
                "CREATE (a)-[r:" + SAPRelationLabels.CONTAINS + "]->(b)"
        );

        // 3. Upload uses relations
        if (!isSilentMode) {
            System.out.print("Creating 'USES' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        connector.executeWrite("MATCH (a:Elements), (b:Elements) " +
                "WHERE a.element_id = b.uses_id " +
                "CREATE (a)-[r:" + SAPRelationLabels.USES + "]->(b)"
        );

        // 4. Upload References
        System.out.println("Path to Reference CSV: " + pathToReferenceCsv);
        if (!isSilentMode) {
            System.out.print("Creating 'REFERENCE' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        pathToReferenceCsv = pathToReferenceCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToReferenceCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "MATCH (a:Elements {element_id: row.source_id}), (b:Elements {element_id: row.target_id})\n" +
                        "CREATE (a)-[r:"+ SAPRelationLabels.REFERENCES +"]->(b)"

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

        // 6. Upload Migration Findings
        if(config.addMigrationFindings()) {
            System.out.println("Path to Migration Findings CSV : " + pathToMigrationFindingsCsv);
            if (!isSilentMode) {
                System.out.print("ADDING 'MIGRATION_FINDINGS' to Element-Nodes. Press any key to continue...");
                userInput.nextLine();
            }
            pathToMigrationFindingsCsv = pathToMigrationFindingsCsv.replace("\\", "/");
            connector.executeWrite(
                    "LOAD CSV WITH HEADERS FROM \"file:///" + pathToMigrationFindingsCsv + "\"\n" +
                            "AS row FIELDTERMINATOR ';'\n" +
                            "MATCH (a:Elements {object_name: row.Objektname})\n" +
                            "SET a.migration_findings = \"true\""
            );
        }

        connector.close();
        System.out.println("Loader step was completed");
    }
}
