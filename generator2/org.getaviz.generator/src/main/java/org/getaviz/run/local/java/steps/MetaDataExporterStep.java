package org.getaviz.run.local.java.steps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.MetaDataOutput;
import org.getaviz.generator.java.metropolis.steps.MetaDataExporter;
import org.getaviz.run.local.java.MetropolisStep;

public class MetaDataExporterStep extends MetropolisStep {
    public static void main(String[] args) {
        SettingsConfiguration.getInstance(metropolisProperties);

        LoaderStep loaderStep = new LoaderStep();
        loaderStep.init();

        CreatorStep creatorStep = new CreatorStep();
        creatorStep.loadNodesAndRelations();
        creatorStep.init();

        MetaDataExporterStep metaDataExporterStep = new MetaDataExporterStep();
        metaDataExporterStep.init();
        metaDataExporterStep.wrapUp();

        System.out.println("\nMetaDataExporter step was completed");
    }

    public void init() {
        MetaDataExporter metaDataExporter = new MetaDataExporter(aCityRepository, nodeRepository);
        MetaDataOutput metaDataOutput = config.getMetaDataOutput();

        // Depending on setting, create file or write metaData as Node's property, or both actions
        if (metaDataOutput == MetaDataOutput.FILE || metaDataOutput == MetaDataOutput.BOTH ) {
            metaDataExporter.exportMetaDataFile();
        }

        if (metaDataOutput == MetaDataOutput.NODEPROP || metaDataOutput == MetaDataOutput.BOTH ) {
            metaDataExporter.setMetaDataPropToACityElements();
        }
    }
}
