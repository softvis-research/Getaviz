package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.StatementResult;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RDElementFactoryTest {

    private static Model model;
    private static RDElementsFactory factory;
    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static StatementResult methods;
    private static StatementResult fields;

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/RDElementFactoryTest.db");
        connector = mockup.getConnector();
        createObjectsForTest();
    }

    @Test
    void methodToRDElementTest() {
        factory.create(methods, 1.0, 0.5, 1.5, 1.0, 1.5,
                0.5, "#000000", "#000000");
        ArrayList<RDElement> list = model.getRDElementsList();
        assertTrue(list.get(1) instanceof DiskSegment);
    }

    @Test
    void fieldToRDElementTest() {
        factory.create(fields, 1.0, 0.5, 1.5, "#000000");
        ArrayList<RDElement> list = model.getRDElementsList();
        assertTrue(list.get(0) instanceof Disk);
    }

    private static void createObjectsForTest() {
        model = new Model(false, false,true);
        factory = new RDElementsFactory(model);
        long typeID = connector.addNode(("CREATE (t:Type)"), "t").id();
        long methodID = connector.addNode(("CREATE (m:Method)"), "m").id();
        long fieldID = connector.addNode(("CREATE (f:Field)"), "f").id();
        connector.executeWrite("MATCH (t:Type), (m:Method) WHERE ID(t)= " + typeID + " AND ID(m)= " + methodID +
                " CREATE (t)-[:DECLARES]->(m)");
        connector.executeWrite("MATCH (t:Type), (f:Field) WHERE ID(t)= " + typeID + " AND ID(f)= " + fieldID +
                " CREATE (t)-[:DECLARES]->(f)");
        methods = connector.executeRead("MATCH (t:Type)-[:DECLARES]->(m:Method) WHERE ID(m)= " + methodID +
                " RETURN m AS node, m.effectiveLineCount AS line, ID(t) AS tID");
        fields = connector.executeRead("MATCH (t:Type)-[:DECLARES]->(f:Field) WHERE ID(f)= " + fieldID +
                " RETURN f AS node, ID(t) AS tID");
    }
}
