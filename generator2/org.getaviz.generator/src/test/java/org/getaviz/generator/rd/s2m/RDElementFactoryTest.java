package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.DiskSegment;
import org.getaviz.generator.rd.RDElement;
import org.getaviz.generator.rd.RDElementsFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RDElementFactoryTest {

    private static RDElementsFactory factory;
    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();
    private static Record method;
    private static Record field;

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/RDElementFactoryTest.db");
        connector = mockup.getConnector();
        createObjectsForTest();
    }

    @AfterAll
    static void close() {
        mockup.close();
    }

    @Test
    void methodToRDElementTest() {
        RDElement element = factory.createFromMethod(method, 1.0, 1.5, 1.0, 0.5, "#000000");
        assertTrue(element instanceof DiskSegment);
    }

    @Test
    void fieldToRDElementTest() {
        RDElement element = factory.createFromField(field, 1.0, 0.5, 1.5, "#000000");
        assertTrue(element instanceof Disk);
    }

    private static void createObjectsForTest() {
        factory = new RDElementsFactory(false, false, true, 1);
        long typeID = connector.addNode(("CREATE (t:Type)"), "t").id();
        long methodID = connector.addNode(("CREATE (m:Method)"), "m").id();
        long fieldID = connector.addNode(("CREATE (f:Field)"), "f").id();
        connector.executeWrite("MATCH (t:Type), (m:Method) WHERE ID(t)= " + typeID + " AND ID(m)= " + methodID +
                " CREATE (t)-[:DECLARES]->(m)");
        connector.executeWrite("MATCH (t:Type), (f:Field) WHERE ID(t)= " + typeID + " AND ID(f)= " + fieldID +
                " CREATE (t)-[:DECLARES]->(f)");
        method = connector.executeRead("MATCH (t:Type)-[:DECLARES]->(m:Method) WHERE ID(m)= " + methodID +
                " RETURN m AS node, m.effectiveLineCount AS line, ID(t) AS tID").single();
        field = connector.executeRead("MATCH (t:Type)-[:DECLARES]->(f:Field) WHERE ID(f)= " + fieldID +
                " RETURN f AS node, ID(t) AS tID").single();
    }
}
