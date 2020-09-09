package org.getaviz.generator.abapCommonSteps;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.common.steps.MetaDataExporter;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
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

public class MetaDataExporterTest_common {

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
        nodeRepository.loadNodesByRelation(SAPRelationLabels.USES, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.INHERIT, true);
        aCityRepository = new ACityRepository();

        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        // Delete old ACityRepository Nodes
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        // Update Neo4j with new nodes
        aCityRepository.writeRepositoryToNeo4j();

        MetaDataExporter metaDataExporter = new MetaDataExporter(aCityRepository, nodeRepository);
        metaDataExporter.exportMetaDataFile();
        metaDataExporter.setMetaDataPropToACityElements();
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");
        aCityRepository.writeRepositoryToNeo4j();
    }

    @AfterAll
    static void close() { connector.close(); }

    @Test
    void checkIfExportFileWasCreated() {
        File currentDir = new File(config.getOutputMap());
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

    @Test
    void checkMetaDataFile() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(config.getOutputMap() + "/metaData.json"));
            JSONArray jsonArray = objectToJSONArray(obj);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);

            assertNotSame("", jsonObject.get("id"));
            assertNotSame("", jsonObject.get("name"));
            assertNotSame("", jsonObject.get("type"));
            assertNotSame("", jsonObject.get("belongsTo"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void checkNeo4jMetaDataProperty() {
        Record record = connector
                .executeRead("MATCH (n:ACityRep) RETURN n.metaData AS metaData LIMIT 1")
                .single();
        String metaData = record.get("metaData").asString();

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(metaData);
            JSONObject jsonObject = (JSONObject) obj;

            assertNotSame("", jsonObject.get("id"));
            assertNotSame("", jsonObject.get("name"));
            assertNotSame("", jsonObject.get("type"));
            assertNotSame("", jsonObject.get("belongsTo"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray objectToJSONArray(Object obj) {
        JSONArray jsonArray = new JSONArray();
        if (obj instanceof Map){
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll((Map)obj);
            jsonArray.add(jsonObject);
        }
        else if (obj instanceof List){
            jsonArray.addAll((List)obj);
        }

        return jsonArray;
    }

}
