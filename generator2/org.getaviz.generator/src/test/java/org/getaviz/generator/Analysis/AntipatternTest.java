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

    @Test
    void godclassTest() {
        Record result = connector
                .executeRead("MATCH (c:Class) " +
                        "WHERE c.godclass = true " +
                        "return c.name").single();
        String godclass = result.get("c.name").asString();
        assertEquals(godclass, "Bank");
    }

    @Test
    void brainmethodTest() {
        List<String> actual = new ArrayList<>();
        connector.executeRead("MATCH (m:Method) " +
                "WHERE m.brainmethod = true " +
                "return m.name").stream().forEach(record -> actual.add(record.get("m.name").asString()));
        ArrayList<String> correct = new ArrayList<>(Arrays.asList("getBusinessCustomers", "getPrivateCustomers"));
        assertEquals(actual, correct);
    }

    @Test
    void brainclassTest() {
        Record result = connector
                .executeRead("MATCH (c:Class) " +
                        "where c.brainclass = true " +
                        "return c.name").single();
        String brainclass = result.get("c.name").asString();
        assertEquals(brainclass, "Bank");
    }

    @Test
    void featureEnvyTest() {
        List<String> actual = new ArrayList<>();
        connector.executeRead("MATCH (m:Method) " +
                "where m.featureEnvy = true " +
                "return m.name").stream().forEach(record -> actual.add(record.get("m.name").asString()));
        ArrayList<String> correct = new ArrayList<>(Arrays.asList("getAccounts"));
        assertEquals(actual, correct);
    }

    @Test
    void dataclassTest() {
        Record result = connector
                .executeRead("MATCH (c:Class) " +
                        "where c.dataclass = true " +
                        "return c.name").single();
        String fe = result.get("c.name").asString();
        assertEquals(fe, "PrivateCustomer");
    }

}