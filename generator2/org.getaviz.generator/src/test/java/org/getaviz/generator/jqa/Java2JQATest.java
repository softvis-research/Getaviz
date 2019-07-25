package org.getaviz.generator.tests;

import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.jqa.Java2JQA;
import org.getaviz.generator.mockups.Bank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Java2JQATest {

    private static DatabaseConnector connector;
    private static Bank mockup = new Bank();

    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/Java2JQATest.db");
        mockup.loadProperties("Java2JQATest.properties");
        connector = mockup.getConnector();
        Java2JQA java2JQA = new Java2JQA();
        java2JQA.run();
    }

    @Test
    void numberOfHashes()  {
        Record result = connector.executeRead("MATCH (n) WHERE n.hash IS NOT NULL RETURN count(n.hash) AS result")
                .single();
        int numberOfHashes = result.get("result").asInt();
        assertEquals(61, numberOfHashes);
    }

    @Test
    void numberOfPrimitives() {
        Record result = connector.executeRead("MATCH (n) WHERE n.primitive RETURN count(n) AS result").single();
        int numberOfPrimitives = result.get("result").asInt();
        assertEquals(0, numberOfPrimitives);
    }

    @Test
    void numberOfGetter() {
        Record result = connector.executeRead("MATCH (method:Getter) RETURN count(method) AS result")
                .single();
        int numberOfGetter = result.get("result").asInt();
        assertEquals(9, numberOfGetter);
    }

    @Test
    void numberOfSetter() {
        Record result = connector.executeRead("MATCH (method:Setter) RETURN count(method) AS result")
                .single();
        int numberOfSetter = result.get("result").asInt();
        assertEquals(2, numberOfSetter);
    }

    @Test
    void numberOfInnerTypes() {
        Record result = connector.executeRead("MATCH (innerType:Inner) RETURN count(innerType) AS result").
                single();
        int numberOfInnerType = result.get("result").asInt();
        assertEquals(0, numberOfInnerType);
    }

    @Test
    void numberOfAnonymousType() {
        Record result = connector.executeRead("MATCH (innerType:Anonymous) RETURN count(innerType)" +
                " AS result").single();
        int numberOfAnonymousType = result.get("result").asInt();
        assertEquals(0, numberOfAnonymousType);
    }
}
