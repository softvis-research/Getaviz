package org.getaviz.run.local.java.steps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.AFrameOutput;
import org.getaviz.generator.java.common.steps.AFrameExporter;
import org.getaviz.run.local.java.MetropolisStep;

public class AFrameExporterStep extends MetropolisStep {
    public static void main(String[] args) {
        SettingsConfiguration.getInstance(metropolisProperties);
//        boolean isNotOverlapped = true;
//        while(isNotOverlapped) {
    //        Scanner userInput = new Scanner(System.in);
    //
    //        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
    //        String input = userInput.nextLine();

    //        if (input.equals("n")) {
    //            isSilentMode = false;
    //        }

            LoaderStep loaderStep = new LoaderStep();
            loaderStep.init();

    //        if (!isSilentMode) {
    //            System.out.print("Loader step to be processed. Press any key to continue...");
    //            userInput.nextLine();
    //        }

            CreatorStep creatorStep = new CreatorStep();
            creatorStep.loadNodesAndRelations();

    //        if (!isSilentMode) {
    //            System.out.print("Creator step to be processed. Press any key to continue...");
    //            userInput.nextLine();
    //        }

            creatorStep.init();

    //        if (!isSilentMode) {
    //            System.out.print("Layouter step to be processed. Press any key to continue...");
    //            userInput.nextLine();
    //        }

            LayouterStep layouterStep = new LayouterStep();
            layouterStep.init();

    //        if (!isSilentMode) {
    //            System.out.print("\nDesigner step to be processed. Press any key to continue...");
    //            userInput.nextLine();
    //        }

            DesignerStep designerStep = new DesignerStep();
            designerStep.init();

    //        if (!isSilentMode) {
    //            System.out.println("Metadata Exporter Step to be processed. Press any key to continue...");
    //            userInput.nextLine();
    //        }

            MetaDataExporterStep metaDataExporterStep = new MetaDataExporterStep();
            metaDataExporterStep.init();

            AFrameExporterStep aFrameExporterStep = new AFrameExporterStep();
            aFrameExporterStep.init();
            aFrameExporterStep.wrapUp();
            // Delete old ACityRepository Nodes
//            connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");
//
//            // Update Neo4j with new nodes
//            aCityRepository.writeRepositoryToNeo4j();
//            System.out.println("\nA-Frame Exporter step was completed");
//
//            long values = connector.executeRead("MATCH (a:ACityRep)" +
//                            " MATCH (b:ACityRep)" +
//                            " WHERE id(a) < id(b)" +
//                            " AND a.xPosition = b.xPosition" +
//                            " AND a.zPosition = b.zPosition" +
//                            " RETURN a.hash, b.hash, a.xPosition, a.zPosition, b.xPosition, b.zPosition")
//                    .stream().count();
//            System.out.println("OVERLAPPED NODES: " + values);
//            if(values  < 3) {
//                isNotOverlapped = false;
//                connector.close();
//            }
//        }
    }

    public void init() {
        AFrameExporter aFrameExporter = new AFrameExporter(aCityRepository, config, "metropolis_AFrame_UI");
        AFrameOutput aFrameOutput = config.getAframeOutput();

        if (aFrameOutput == AFrameOutput.FILE || aFrameOutput == AFrameOutput.BOTH ) {
            aFrameExporter.exportAFrame();
        }

        if (aFrameOutput == AFrameOutput.NODEPROP || aFrameOutput == AFrameOutput.BOTH ) {
            aFrameExporter.setAframePropToACityElements();
        }
    }
}
