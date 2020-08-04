package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.city.steps.ACityDesigner;
import org.getaviz.generator.abap.city.steps.ACityLayouter;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.abap.city.steps.ACityAFrameExporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class AFrameExporter {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;
    private static String exportString;

    public static void main(String[] args) {
        boolean isSilentMode = true;
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);

        aCityRepository = new ACityRepository();

        Scanner userInput = new Scanner(System.in);
        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();
        if (input.equals("n")) {
            isSilentMode = false;
        }

        if (!isSilentMode) {
            System.out.print("Creator step to be processed. Press any key to continue...");
            userInput.nextLine();
        }
        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        if (!isSilentMode) {
            System.out.print("Layouter step to be processed. Press any key to continue...");
            userInput.nextLine();
        }
        ACityLayouter aCityLayouter = new ACityLayouter(aCityRepository, nodeRepository, config);
        aCityLayouter.layoutRepository();

        if (!isSilentMode) {
            System.out.print("Designer step to be processed. Press any key to continue...");
            userInput.nextLine();
        }
        ACityDesigner designer = new ACityDesigner(aCityRepository, nodeRepository, config);
        designer.designRepository();

        // Delete old ACityRepository Nodes
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        // Update Neo4j with new nodes
        aCityRepository.writeRepositoryToNeo4j();


        ACityAFrameExporter aCityAFrameExporter = new ACityAFrameExporter(aCityRepository, config);
        exportString = aCityAFrameExporter.createAFrameExportFile();

        //System.out.println(exportString);

        Writer fw = null;
        try {
            File currentDir = new File("src/test/neo4jexport");
            String path = currentDir.getAbsolutePath() + "/model.html";
            fw = new FileWriter(path);
            fw.write(exportString);
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

        System.out.println("\nA-Frame Exporter step was completed\"");
        connector.close();
    }
}
