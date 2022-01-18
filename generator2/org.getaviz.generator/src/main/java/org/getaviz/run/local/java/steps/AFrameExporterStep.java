package org.getaviz.run.local.java.steps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.AFrameOutput;
import org.getaviz.generator.java.common.steps.AFrameExporter;
import org.getaviz.run.local.java.MetropolisStep;

import java.util.Scanner;

public class AFrameExporterStep extends MetropolisStep {
    public static void main(String[] args) {
        SettingsConfiguration.getInstance(metropolisProperties);
        Scanner userInput = new Scanner(System.in);

        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();
        boolean isSilentMode = !input.equals("n");

        if (!isSilentMode) {
            System.out.print("\nLoader step to be processed. Press any key to continue...");
            userInput.nextLine();
        }

        LoaderStep loaderStep = new LoaderStep();
        loaderStep.init();
        System.out.println("Loader Step finished.");

        if (!isSilentMode) {
            System.out.print("\nCreator step to be processed. Press any key to continue...");
            userInput.nextLine();
        }

        CreatorStep creatorStep = new CreatorStep();
        creatorStep.loadNodesAndRelations();
        creatorStep.init();
        System.out.println("Creator Step finished.");

        if (!isSilentMode) {
            System.out.print("\nLayouter step to be processed. Press any key to continue...");
            userInput.nextLine();
        }

        LayouterStep layouterStep = new LayouterStep();
        layouterStep.init();
        System.out.println("Layouter Step finished.");

        if (!isSilentMode) {
            System.out.print("\nDesigner step to be processed. Press any key to continue...");
            userInput.nextLine();
        }

        DesignerStep designerStep = new DesignerStep();
        designerStep.init();
        System.out.println("Designer Step finished.");

        if (!isSilentMode) {
            System.out.println("\nMetadata Exporter Step to be processed. Press any key to continue...");
            userInput.nextLine();
        }

        MetaDataExporterStep metaDataExporterStep = new MetaDataExporterStep();
        metaDataExporterStep.init();
        System.out.println("Metadata Exporter Step finished.");

        if (!isSilentMode) {
            System.out.println("\nAFrame Exporter Step to be processed. Press any key to continue...");
            userInput.nextLine();
        }

        AFrameExporterStep aFrameExporterStep = new AFrameExporterStep();
        aFrameExporterStep.init();
        System.out.println("AFrame Exporter Step finished.");
        aFrameExporterStep.wrapUp();
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
