package org.getaviz.generator.abapCity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.ABAPmock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.types.Node;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeRepositoryPropertyRelationLoadTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ABAPmock mockUp = new ABAPmock();
    private static SourceNodeRepository nodeRepository;

    static DatabaseConnector connector;

    @BeforeAll
    static void setup() {
        mockUp.setupDatabase("./test/databases/CityBankTest.db", "SAPExportCreateNodes.cypher");
        mockUp.runCypherScript("SAPExportCreateContainsRelation.cypher");
        mockUp.runCypherScript("SAPExportCreateUsesRelation.cypher");
        mockUp.runCypherScript("SAPExportCreateTypeOfRelation.cypher");
        mockUp.loadProperties("CityBankTest.properties");
        connector = mockUp.getConnector();
    }

    @AfterAll
    static void close() {
        mockUp.close();
    }


    @Test
    void loadDataElementsTest(){

        nodeRepository = new SourceNodeRepository();

        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.DataElement.name());

        int nodeAmount = nodeRepository.getNodes().size();

        assertEquals(19, nodeAmount);
    }


    @Test
    void NodesByContainsRelation(){
        nodeRepository = new SourceNodeRepository();

        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Class.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS);

        Collection<Node> classSourceNodes = nodeRepository.getNodesByProperty(SAPNodeProperties.type_name, SAPNodeTypes.Class.name());
        assertEquals(21, classSourceNodes.size());

        Collection<Node> methodSourceNodes = nodeRepository.getNodesByProperty(SAPNodeProperties.type_name, SAPNodeTypes.Method.name());
        assertEquals(35, methodSourceNodes.size());

        Collection<Node> attributeSourceNodes = nodeRepository.getNodesByProperty(SAPNodeProperties.type_name, SAPNodeTypes.Attribute.name());
        assertEquals(14, attributeSourceNodes.size());
    }

    @Test
    void NodesByContainsRelationRecursive(){
        nodeRepository = new SourceNodeRepository();

        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);

        //21 classes in total - 1 local class that has a USES relation
        Collection<Node> classSourceNodes = nodeRepository.getNodesByProperty(SAPNodeProperties.type_name, SAPNodeTypes.Class.name());
        assertEquals(20, classSourceNodes.size());

        Collection<Node> methodSourceNodes = nodeRepository.getNodesByProperty(SAPNodeProperties.type_name, SAPNodeTypes.Method.name());
        assertEquals(37, methodSourceNodes.size());

        Collection<Node> attributeSourceNodes = nodeRepository.getNodesByProperty(SAPNodeProperties.type_name, SAPNodeTypes.Attribute.name());
        assertEquals(61, attributeSourceNodes.size());

    }
}
