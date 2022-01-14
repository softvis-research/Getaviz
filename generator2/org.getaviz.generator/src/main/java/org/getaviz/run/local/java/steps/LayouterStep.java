package org.getaviz.run.local.java.steps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.metropolis.steps.MetropolisLayouter;
import org.getaviz.run.local.java.MetropolisStep;

public class LayouterStep extends MetropolisStep {
    public static void main(String[] args) {
        SettingsConfiguration.getInstance(metropolisProperties);

        LoaderStep loaderStep = new LoaderStep();
        loaderStep.init();

        CreatorStep creatorStep = new CreatorStep();
        creatorStep.loadNodesAndRelations();
        creatorStep.init();

        LayouterStep layouterStep = new LayouterStep();
        layouterStep.init();
        layouterStep.wrapUp();

        System.out.println("\nLayouter step was completed");
    }

    public void init() {
        MetropolisLayouter layouter = new MetropolisLayouter(aCityRepository, nodeRepository, config);
        layouter.layoutRepository();
    }
}
