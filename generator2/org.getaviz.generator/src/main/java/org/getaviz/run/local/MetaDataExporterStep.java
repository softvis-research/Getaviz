package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.MetaDataOutput;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.common.steps.MetaDataExporter;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.metropolis.steps.MetropolisCreator;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;

public class MetaDataExporterStep {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;
    private static MetaDataOutput metaDataOutput;

    public static void main(String[] args) {
        SettingsConfiguration.getInstance("src/test/resources/ABAPCityTest.properties");
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.USES, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.INHERIT, true);
        aCityRepository = new ACityRepository();

        MetropolisCreator aMetropolisCreator = new MetropolisCreator(aCityRepository, nodeRepository, config);
        aMetropolisCreator.createRepositoryFromNodeRepository();


        MetaDataExporter metaDataExporter = new MetaDataExporter(aCityRepository, nodeRepository);
        metaDataOutput = config.getMetaDataOutput();

        // Depending on setting, create file or write metaData as Node's property, or both actions
        if (metaDataOutput == MetaDataOutput.FILE || metaDataOutput == MetaDataOutput.BOTH ) {
            metaDataExporter.exportMetaDataFile();
        }

        if (metaDataOutput == MetaDataOutput.NODEPROP || metaDataOutput == MetaDataOutput.BOTH ) {
            metaDataExporter.setMetaDataPropToACityElements();
        }

        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");
        aCityRepository.writeRepositoryToNeo4j();

        connector.close();
        System.out.println("\nMetaDataExporter step was completed\"");
    }
}
