package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.MetaDataOutput;
import org.getaviz.generator.SettingsConfiguration.AFrameOutput;
import org.getaviz.generator.abap.city.steps.*;
import org.getaviz.generator.abap.common.steps.AFrameExporter;
import org.getaviz.generator.abap.common.steps.MetaDataExporter;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.metropolis.steps.MetropolisCreator;
import org.getaviz.generator.abap.metropolis.steps.MetropolisDesigner;
import org.getaviz.generator.abap.metropolis.steps.MetropolisLayouter;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class AFrameExporterStep {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;
    private static MetaDataOutput metaDataOutput;
    private static  AFrameOutput aFrameOutput;

    public static void main(String[] args) {
        SettingsConfiguration.getInstance("src/test/resources/ABAPCityTest.properties");
        boolean isSilentMode = true;

        Scanner userInput = new Scanner(System.in);
        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();
        if (input.equals("n")) {
            isSilentMode = false;
        }

        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.USES, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        //nodeRepository.loadNodesByRelation(SAPRelationLabels.INHERIT, true);

        aCityRepository = new ACityRepository();

        if (!isSilentMode) {
            System.out.print("Creator step to be processed. Press any key to continue...");
            userInput.nextLine();
        }
        MetropolisCreator creator = new MetropolisCreator(aCityRepository, nodeRepository, config);
        creator.createRepositoryFromNodeRepository();

        if (!isSilentMode) {
            System.out.print("Layouter step to be processed. Press any key to continue...");
            userInput.nextLine();
        }
        MetropolisLayouter layouter = new MetropolisLayouter(aCityRepository, nodeRepository, config);
        layouter.layoutRepository();

        if (!isSilentMode) {
            System.out.print("\nDesigner step to be processed. Press any key to continue...");
            userInput.nextLine();
        }
        MetropolisDesigner designer = new MetropolisDesigner(aCityRepository, nodeRepository, config);
        designer.designRepository();

        // Create metaData.json
        if (!isSilentMode) {
            System.out.println("Writing MetaData. Press any key to continue...");
            userInput.nextLine();
        }
        MetaDataExporter metaDataExporter = new MetaDataExporter(aCityRepository, nodeRepository);
        metaDataOutput = config.getMetaDataOutput();
        // Depending on setting, create file or write metaData as Node's property, or both actions
        if (metaDataOutput == MetaDataOutput.FILE || metaDataOutput == MetaDataOutput.BOTH ) {
            metaDataExporter.exportMetaDataFile();
        }

        if (metaDataOutput == MetaDataOutput.NODEPROP || metaDataOutput == MetaDataOutput.BOTH ) {
            metaDataExporter.setMetaDataPropToACityElements();
        }

        // Create A-Frame
        // Create metaData.json
        if (!isSilentMode) {
            System.out.println("Writing A-Frame. Press any key to continue...");
            userInput.nextLine();
        }
        //AFrameExporter aFrameExporter = new AFrameExporter(aCityRepository, config, "acity_AFrame_UI");
        AFrameExporter aFrameExporter = new AFrameExporter(aCityRepository, config, "metropolis_AFrame_UI");
        aFrameOutput = config.getAframeOutput();
        if (aFrameOutput == AFrameOutput.FILE || aFrameOutput == AFrameOutput.BOTH ) {
            aFrameExporter.exportAFrame();
        }

        if (aFrameOutput == AFrameOutput.NODEPROP || aFrameOutput == AFrameOutput.BOTH ) {
            aFrameExporter.setAframePropToACityElements();
        }


        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");
        aCityRepository.writeRepositoryToNeo4j();

        System.out.println("\nA-Frame Exporter step was completed\"");
        connector.close();
    }
}
