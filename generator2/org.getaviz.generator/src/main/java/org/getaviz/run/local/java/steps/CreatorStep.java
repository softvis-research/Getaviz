package org.getaviz.run.local.java.steps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.generator.java.enums.JavaRelationLabels;
import org.getaviz.generator.java.metropolis.steps.MetropolisCreator;
import org.getaviz.generator.repository.SourceNodeRepository;
import org.getaviz.generator.repository.ACityRepository;
import org.getaviz.run.local.java.MetropolisStep;


public class CreatorStep extends MetropolisStep {
    public static void main(String[] args) {
        SettingsConfiguration.getInstance(metropolisProperties);

        LoaderStep loaderStep = new LoaderStep();
        loaderStep.init();

        CreatorStep creatorStep = new CreatorStep();
        creatorStep.loadNodesAndRelations();
        creatorStep.init();
        creatorStep.wrapUp();

        System.out.println("\nCreator step was completed");
    }

    public void init() {
        aCityRepository = new ACityRepository();
        MetropolisCreator creator = new MetropolisCreator(aCityRepository, nodeRepository, config);
        creator.createRepositoryFromNodeRepository();
    }

    public void loadNodesAndRelations() {
        nodeRepository = new SourceNodeRepository();

        for (JavaNodeTypes type: JavaNodeTypes.values()) {
            nodeRepository.loadNodesByType(type);
        }

        nodeRepository.loadNodesByRelation(JavaRelationLabels.CONTAINS, false);
        nodeRepository.loadNodesByRelation(JavaRelationLabels.DECLARES, false);
        nodeRepository.loadNodesByRelation(JavaRelationLabels.EXTENDS, false);
        nodeRepository.loadNodesByRelation(JavaRelationLabels.INVOKES, false);
    }
}
