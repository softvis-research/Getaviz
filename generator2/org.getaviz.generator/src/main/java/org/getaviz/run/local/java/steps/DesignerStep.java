package org.getaviz.run.local.java.steps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.metropolis.steps.MetropolisDesigner;
import org.getaviz.run.local.java.MetropolisStep;

public class DesignerStep extends MetropolisStep {
    public static void main(String[] args) {
        SettingsConfiguration.getInstance(metropolisProperties);

        LoaderStep loaderStep = new LoaderStep();
        loaderStep.init();

        CreatorStep creatorStep = new CreatorStep();
        creatorStep.loadNodesAndRelations();
        creatorStep.init();

        LayouterStep layouterStep = new LayouterStep();
        layouterStep.init();

        DesignerStep designerStep = new DesignerStep();
        designerStep.init();
        designerStep.wrapUp();

        System.out.println("\nDesigner step was completed");
    }

    public void init() {
        MetropolisDesigner designer = new MetropolisDesigner(aCityRepository, nodeRepository, config);
        designer.designRepository();
    }
}
