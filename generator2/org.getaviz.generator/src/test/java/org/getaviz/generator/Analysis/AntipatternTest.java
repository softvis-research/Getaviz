package org.getaviz.generator.Analysis;

import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.StepFactory;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.Bank;
import org.getaviz.generator.mockups.BankAntipattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AntipatternTest {

    private static DatabaseConnector connector;
    private static BankAntipattern mockup = new BankAntipattern();


    @BeforeAll
    static void setup() {
        mockup.setupDatabase("./test/databases/AntipatternTest.db");
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

//    @Test
//    void godclassTest() {
//        Record result = connector
//                .executeRead("MATCH (c:Class) " +
//                        "WHERE c.cyclo >= 47 AND c.atfd > 4 AND c.nom > 0 " +
//                        "WITH toFloat(c.maa) / toFLoat(c.nom) AS tcc, c AS c " +
//                        "WHERE tcc < 0.33 " +
//                        "return c.name").single();
//        String godclass = result.get("c.name").asString();
//        assertEquals(godclass, "Bank");
//    }
//
//    @Test
//    void brainmethodTest() {
//        List<String> actual = new ArrayList<>();
//        connector.executeRead("MATCH (m:Method) " +
//                "WHERE m.effectiveLineCount > 65 AND m.cyclomaticComplexity > 31 AND m.noav >= 7 " +
//                "return m.name").stream().forEach(record -> actual.add(record.get("m.name").asString()));
//        ArrayList<String> correct = new ArrayList<>(Arrays.asList("getBusinessCustomers", "getPrivateCustomers"));
//        assertEquals(actual, correct);
//    }
//
//    @Test
//    void brainclassTest() {
//        Record result = connector
//                .executeRead("MATCH (c:Class)-[:DECLARES]->(m:Method) " +
//                        "WITH c AS c, m AS m, COUNT(m.brainmethod) AS nobm " +
//                        "WHERE nobm = 1 AND c.loc > 390 AND c.cyclo > 92 OR nobm > 1 AND c.loc > 195 AND c.cyclo > 47 " +
//                        "WITH  toFloat(c.maa) / toFLoat(c.nom) AS tcc, c AS c " +
//                        "WHERE tcc < 0.5 " +
//                        "return distinct c.name").single();
//        String brainclass = result.get("c.name").asString();
//        assertEquals(brainclass, "Bank");
//    }
//
//    @Test
//    void featureEnvyTest() {
//        Record result = connector
//                .executeRead("MATCH (m:Method) " +
//                        "WITH m AS m " +
//                        "WHERE (m.atad - m.atod) > 4 AND m.atad > 0 " +
//                        "WITH toFloat(m.atod) / toFLoat(m.atad) AS laa, m AS m " +
//                        "WHERE laa < 0.33 " +
//                        "return m.name").single();
//        String fe = result.get("m.name").asString();
//        assertEquals(fe, "getAccounts");
//    }
//
//    @Test
//    void dataclassTest() {
//        Record result = connector
//                .executeRead("MATCH (c:Class) " +
//                        "WITH c AS c " +
//                        "WHERE c.cyclo < 31 AND (c.noam + c.nopa) > 4 OR c.cyclo < 47 AND (c.noam + c.nopa) > 7 " +
//                        "WITH toFloat(c.nom - c.noam) / toFloat(c.nopa + c.noam) AS woc, c AS c " +
//                        "WHERE woc < 0.33 " +
//                        "return c.name").single();
//        String fe = result.get("c.name").asString();
//        assertEquals(fe, "PrivateCustomer");
//    }

}