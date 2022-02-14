//package org.getaviz.generator.abapCommonSteps;
//
//import org.getaviz.generator.SettingsConfiguration;
//import org.getaviz.generator.common_steps.AFrameExporter;
//import org.getaviz.generator.abap.enums.SAPNodeProperties;
//import org.getaviz.generator.abap.enums.SAPNodeTypes;
//import org.getaviz.generator.abap.enums.SAPRelationLabels;
//import org.getaviz.generator.abap.city.steps.ACityCreator;
//import org.getaviz.generator.abap.city.steps.ACityDesigner;
//import org.getaviz.generator.abap.city.steps.ACityLayouter;
//import org.getaviz.generator.abap.repository.ACityRepository;
//import org.getaviz.generator.repository.SourceNodeRepository;
//import org.getaviz.generator.mockups.ABAPmock;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//
//public class AFrameExporterTest_ACity {
//
//    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
//
//    private static ABAPmock mockUp = new ABAPmock();
//    private static SourceNodeRepository nodeRepository;
//    private static ACityRepository aCityRepository;
//
//    private static String exportString;
//
//    @BeforeAll
//    static void setup() {
//
//        mockUp.setupDatabase("./test/databases/CityBankTest.db", "SAPExportCreateNodes.cypher");
//
//        mockUp.runCypherScript("SAPExportCreateContainsRelation.cypher");
//        mockUp.runCypherScript("SAPExportCreateTypeOfRelation.cypher");
//
//        mockUp.loadProperties("Generator.properties");
//
//        nodeRepository = new SourceNodeRepository();
//        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
//        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
//        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
//
//        aCityRepository = new ACityRepository();
//
//        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
//        aCityCreator.createRepositoryFromNodeRepository();
//
//        ACityLayouter aCityLayouter = new ACityLayouter(aCityRepository, nodeRepository, config);
//        aCityLayouter.layoutRepository();
//
//        ACityDesigner aCityDesigner = new ACityDesigner(aCityRepository, nodeRepository, config);
//        aCityDesigner.designRepository();
//
//        AFrameExporter aFrameExporter = new AFrameExporter(aCityRepository, config, "acity_AFrame");
//        exportString = aFrameExporter.createAFrameExportString();
//    }
//
//    @AfterAll
//    static void close() {
//        mockUp.close();
//    }
//
//    @Test
//    public void export(){
//        assertNotEquals("", exportString);
//
//        System.out.println(exportString);
//    }
//
//}
