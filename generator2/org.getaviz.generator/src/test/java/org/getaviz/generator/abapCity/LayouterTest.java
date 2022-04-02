package org.getaviz.generator.abapCity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.city.steps.ACityLayouter;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.ABAPmock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LayouterTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ABAPmock mockUp = new ABAPmock();
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;
    static DatabaseConnector connector;

    @BeforeAll
    static void setup() {
        mockUp.setupDatabase("./test/databases/CityBankTest.db", "SAPExportCreateNodes.cypher");
        mockUp.runCypherScript("SAPExportCreateContainsRelation.cypher");
        mockUp.runCypherScript("SAPExportCreateTypeOfRelation.cypher");
        mockUp.loadProperties("ABAPCityTest.properties");
        connector = mockUp.getConnector();

        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesWithRelation(SAPRelationLabels.TYPEOF);

        aCityRepository = new ACityRepository();

        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        ACityLayouter aCityLayouter = new ACityLayouter(aCityRepository, nodeRepository, config);
        aCityLayouter.layoutRepository();
    }

    @AfterAll
    static void close() {
        mockUp.close();
    }

    @Test
    void layoutTableTypesHeight() {
        Collection<ACityElement> ttBuildings = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.Building, SAPNodeProperties.type_name, "TableType");

        for (ACityElement ttBuilding: ttBuildings) {
            Node tableTypeSourceNode = ttBuilding.getSourceNode();
            Collection<Node> typeOfNodes = nodeRepository.getRelatedNodes(tableTypeSourceNode, SAPRelationLabels.TYPEOF, true);
            assertEquals(1, typeOfNodes.size());

            Node typeOfNode = typeOfNodes.iterator().next();

            Value propertyValue = typeOfNode.get(SAPNodeProperties.type_name.name());
            String typeOfNodeTypeProperty = propertyValue.asString();

            switch (typeOfNodeTypeProperty) {
                case "Structure":
                    assertEquals(2, ttBuilding.getHeight()); break;
                case "Table":
                case "TableType":
                    assertEquals(4, ttBuilding.getHeight()); break;
                case "Class":
                case "Interface":
                    assertEquals(5, ttBuilding.getHeight()); break;
                case "DataElement":
                    assertEquals(1, ttBuilding.getHeight()); break;
            }
        }
    }

}
