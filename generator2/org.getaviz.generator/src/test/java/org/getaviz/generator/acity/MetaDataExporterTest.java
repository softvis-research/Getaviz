package org.getaviz.generator.acity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.*;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaDataExporterTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;

    @BeforeAll
    static void setup() {
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
        aCityRepository = new ACityRepository();

        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        // Delete old ACityRepository Nodes
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        // Update Neo4j with new nodes
        aCityRepository.writeRepositoryToNeo4j();

        ACityMetaDataExporter aCityMetaDataExporter = new ACityMetaDataExporter(aCityRepository, nodeRepository);
        aCityMetaDataExporter.exportMetaData();
    }

    @AfterAll
    static void close() { connector.close(); }

    @Test
    void checkIfElementsAreAddedToNeo4j() {
        Record record = connector
                .executeRead("MATCH (n:Elements) RETURN count(n) AS result")
                .single();
        int numberOfElements = record.get("result").asInt();
        assertEquals(657, numberOfElements);
    }

    @Test
    void checkIfACityElementsAreAddedToNeo4j() {
        Record record = connector
                .executeRead("MATCH (n:ACityRep) RETURN count(n) AS result")
                .single();
        int numberOfElements = record.get("result").asInt();
        assertEquals(317, numberOfElements);
    }

    @Test
    void checkIfSourceRelationsExist() {
        Record record = connector
                .executeRead("MATCH p=()-[:SOURCE]->() RETURN count(p) AS result")
                .single();
        int numberOfRelations = record.get("result").asInt();
        assertEquals(292, numberOfRelations);
    }

    @Test
    void checkIfTypeOfRelationsExist() {
        Record record = connector
                .executeRead("MATCH p=()-[:TYPEOF]->() RETURN count(p) AS result")
                .single();
        int numberOfRelations = record.get("result").asInt();
        assertEquals(108, numberOfRelations);
    }

    @Test
    void checkIfContainsRelationsExist() {
        Record record = connector
                .executeRead("MATCH p=()-[:CONTAINS]->() RETURN count(p) AS result")
                .single();
        int numberOfRelations = record.get("result").asInt();
        assertEquals(296, numberOfRelations);
    }

    @Test
    void checkIfChildRelationsExist() {
        Record record = connector
                .executeRead("MATCH p=()-[:CHILD]->() RETURN count(p) AS result")
                .single();
        int numberOfRelations = record.get("result").asInt();
        assertEquals(302, numberOfRelations);
    }

    @Test
    void checkIfExportFilesWereCreated() {
        File currentDir = new File("src/test/neo4jexport/");
        String helper = currentDir.getAbsolutePath();
        boolean metaDataFileExists = false;
        List<Path> files = new ArrayList<>();
        try {
            files = Files.walk(Paths.get(helper), 1)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Path p : files) {
            if (p.toString().endsWith("metaData.json")) {
                metaDataFileExists = p.toFile().length() > 0;
                break;
            }
        }

        assertEquals(true, metaDataFileExists);
    }
}
