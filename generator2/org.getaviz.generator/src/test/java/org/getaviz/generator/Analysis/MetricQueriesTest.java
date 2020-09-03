package org.getaviz.generator.Analysis;

import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.StepFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.mockups.BankAntipattern;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


class MetricQueriesTest {

    private static DatabaseConnector connector;
    private static BankAntipattern mockup = new BankAntipattern();


    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/MetricQueriesTest.db");
        connector = mockup.getConnector();
        SettingsConfiguration config = SettingsConfiguration.getInstance();
        StepFactory factory = new StepFactory(config, Collections.singletonList(ProgrammingLanguage.JAVA));
        Step ap = factory.createStepAntipattern();
        ap.run();
    }

    @AfterAll
    static void close() {
        mockup.close();
    }

    @Test
    void countMethodsAccessingAttributesTest() {
        Record result = connector
                .executeRead("match (c:Class)-[:DECLARES]->(m:Method)-[:WRITES|READS]->(f:Field)\n" +
                        "where (not m.name contains '$')\n" +
                        "with c as c, m as m, COUNT(f.name) as maa\n" +
                        "where maa >= 2\n" +
                        "with count(m) as result\n" +
                        "return result").single();
        int maa = result.get("result").asInt();
        assertEquals(maa, 2);
    }

    @Test
    void accessToForeignDataTest() {
        List<Integer> actual = new ArrayList<>();
        connector.executeRead("MATCH (c:Class)-[:DECLARES|READS|WRITES]->(f:Field)\n" +
                "WITH f AS f, c AS c\n" +
                "WHERE (f.name CONTAINS '$') AND (NOT f.name CONTAINS \"$class$java\")\n" +
                "WITH c AS c, COUNT(f.name) AS atfd\n" +
                "RETURN atfd").stream().forEach(record -> actual.add(record.get("atfd").asInt()));
        ArrayList<Integer> correct = new ArrayList<>(Arrays.asList(4,1));
        assertEquals(actual, correct);
    }

    @Test
    void complexityAndNumbOfMethodsTest() {
        List<Integer> cyclo = new ArrayList<>();
        List<Integer> nom = new ArrayList<>();
        List<Integer> loc = new ArrayList<>();
        connector.executeRead("match (c:Class)-[:DECLARES]->(m:Method)\n" +
                "with sum(m.cyclomaticComplexity) as cyclo, count(m.fqn) as nom, sum(m.effectiveLineCount) as loc, c as c\n" +
                "return cyclo, nom, loc").stream().forEach(record -> {cyclo.add(record.get("cyclo").asInt());
                                                                      nom.add(record.get("nom").asInt());
                                                                      loc.add(record.get("loc").asInt());});
        ArrayList<Integer> cycloExpect = new ArrayList<>(Arrays.asList(48, 27, 12, 12, 24, 12, 148));
        ArrayList<Integer> nomExpect = new ArrayList<>(Arrays.asList(4,5,1,1,2,1,8));
        ArrayList<Integer> locExpect = new ArrayList<>(Arrays.asList(280,350,70,70,140,70,560));
        assertEquals(cyclo, cycloExpect);
        assertEquals(nom, nomExpect);
        assertEquals(loc, locExpect);
    }

    @Test
    void  accessToOwnDataTest() {
        List<Integer> actual = new ArrayList<>();
        connector.executeRead("MATCH (c:Class)-[:DECLARES]->(m:Method)-[:READS|WRITES]->(f:Field)\n" +
                "WITH c AS c, m AS m, f AS f\n" +
                "WHERE (NOT f.name CONTAINS '$') AND (NOT f.name CONTAINS \"$class$java\")\n" +
                "WITH m AS m, count(f.fqn) AS atod\n" +
                "return atod").stream().forEach(record -> actual.add(record.get("atod").asInt()));
        ArrayList<Integer> correct = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2));
        assertEquals(actual, correct);
    }

    @Test
    void  accesToAllDataTest() {
        List<Integer> actual = new ArrayList<>();
        connector.executeRead("MATCH (m:Method)-[:DECLARES|READS|WRITES]->(f:Field)\n" +
                "WITH m AS m, count(f.fqn) AS atad\n" +
                "return atad").stream().forEach(record -> actual.add(record.get("atad").asInt()));
        ArrayList<Integer> correct = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 6, 1, 1));
        assertEquals(actual, correct);
    }

    @Test
    void numberOfPublicAttributesTest() {
        List<Integer> actual = new ArrayList<>();
        connector.executeRead("MATCH (c:Class)-[:DECLARES]->(f:Field)\n" +
                "WITH c AS c, f AS f\n" +
                "WHERE f.visibility CONTAINS \"public\"\n" +
                "WITH COUNT(f.fqn) AS nopa, c AS c\n" +
                "return c.nopa").stream().forEach(record -> actual.add(record.get("c.nopa").asInt()));
        ArrayList<Integer> correct = new ArrayList<>(Arrays.asList(3,4,2,6));
        assertEquals(actual, correct);
    }

    @Test
    void numberOfAccessorMethodsTest() {
        List<Integer> actual = new ArrayList<>();
        connector.executeRead("MATCH (c:Class)-[:DECLARES]->(m:Method)\n" +
                "WITH c AS c, m AS m\n" +
                "WHERE m.name STARTS WITH \"get\" OR m.name STARTS WITH \"set\"\n" +
                "WITH COUNT(m.fqn) AS noam, c AS c\n" +
                "RETURN noam").stream().forEach(record -> actual.add(record.get("noam").asInt()));
        ArrayList<Integer> correct = new ArrayList<>(Arrays.asList(4,4,2,8));
        assertEquals(actual, correct);
    }

}