package org.getaviz.generator.acity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.*;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class AFrameExporterStepTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;

    @BeforeAll
    static void setup() {
        SettingsConfiguration.getInstance("ABAPCityTest.properties");
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);

        aCityRepository = new ACityRepository();

        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        ACityLayouter aCityLayouter = new ACityLayouter(aCityRepository, nodeRepository, config);
        aCityLayouter.layoutRepository();

        ACityDesigner designer = new ACityDesigner(aCityRepository, nodeRepository, config);
        designer.designRepository();

        // Delete old ACityRepository Nodes
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        // Update Neo4j with new nodes
        aCityRepository.writeRepositoryToNeo4j();

        // Create metaData.json
        ACityMetaDataExporter aCityMetaDataExporter = new ACityMetaDataExporter(aCityRepository, nodeRepository);
        aCityMetaDataExporter.exportMetaDataFile();
        aCityMetaDataExporter.setMetaDataPropToACityElements();

        // Create A-Frame
        ACityAFrameExporter aCityAFrameExporter = new ACityAFrameExporter(aCityRepository, config, "acity_AFrame_UI");
        aCityAFrameExporter.exportAFrame();
        aCityAFrameExporter.setAframePropToACityElements();
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");
        aCityRepository.writeRepositoryToNeo4j();
    }

    @AfterAll
    static void close() { connector.close(); }

    @Test
    void checkIfExportFilesWereCreated() {
        File currentDir = new File(config.getOutputMap());
        String helper = currentDir.getAbsolutePath();
        boolean metaDataFileExists = false;
        boolean aframeFileExists = false;
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
            } else if (p.toString().endsWith("model.html")) {
                aframeFileExists = p.toFile().length() > 0;
            }
        }

        assertEquals(true, metaDataFileExists);
        assertEquals(true, aframeFileExists);
    }

    @Test
    void checkNeo4jAFrameProperty() {
        Record record = connector
                .executeRead("MATCH (n:ACityRep) WHERE EXISTS (n.aframeProperty)\n" +
                        "RETURN n.aframeProperty AS aframeProperty \n" +
                        "LIMIT 1")
                .single();
        String aframeProperty = record.get("aframeProperty").asString();
        assertNotSame("null", aframeProperty);

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(aframeProperty);
            JSONObject jsonObject = (JSONObject) obj;
            assertNotSame("", jsonObject.get("id"));
            assertNotSame("", jsonObject.get("shape"));
            assertNotSame("", jsonObject.get("position"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
