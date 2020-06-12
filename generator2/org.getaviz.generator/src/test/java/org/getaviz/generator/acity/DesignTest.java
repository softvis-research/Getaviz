package org.getaviz.generator.acity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.city.steps.ACityDesigner;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.ABAPmock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DesignTest{

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ABAPmock mockUp = new ABAPmock();
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;
    static DatabaseConnector connector;

    @BeforeAll
    static void setup() {

        mockUp.setupDatabase("./test/databases/CityBankTest.db", "SAPExportCreateNodes.cypher");
        mockUp.runCypherScript("SAPExportCreateContainsRelation.cypher");
        mockUp.runCypherScript("SAPExportCreateUsesRelation.cypher");
        mockUp.loadProperties("ABAPCityTest.properties");

        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesWithRelation(SAPRelationLabels.CONTAINS);
        nodeRepository.loadNodesWithRelation(SAPRelationLabels.USES);

        aCityRepository = new ACityRepository();

        ACityCreator aCityCreator = new ACityCreator(aCityRepository, nodeRepository, config);
        aCityCreator.createRepositoryFromNodeRepository();

        ACityDesigner designer = new ACityDesigner(aCityRepository, nodeRepository, config);
        designer.designRepository();
    }

    @AfterAll
    static void close() {
        mockUp.close();
    }

    @Test
    public void districtDesign(){
        Collection<ACityElement> classDistricts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, "Table");
        assertNotEquals(0, classDistricts.size());

        for( ACityElement district : classDistricts){
             assertEquals("#1A5276", district.getColor());
             assertEquals(ACityElement.ACityShape.Box, district.getShape());
        }
    }


    @Test
    public void buildingDesign(){
        Collection<ACityElement> classBuildings =
                aCityRepository.getElementsByTypeAndSourceProperty(
            ACityElement.ACityType.Building, SAPNodeProperties.type_name, "TableType");

        assertNotEquals(0, classBuildings.size());

        for( ACityElement building : classBuildings) {
                assertEquals("#ffb48f", building.getColor());
                assertEquals(ACityElement.ACityShape.Box, building.getShape());
                assertEquals(0.8, building.getWidth());
        }
    }

    @Test
    public void floorDesign(){
        Collection<ACityElement> floors = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.Floor, SAPNodeProperties.type_name, "TableElement");
        assertNotEquals(0, floors.size());

        for( ACityElement floor : floors){
            assertEquals("#ffffff", floor.getColor());
            assertEquals(ACityElement.ACityShape.Cylinder, floor.getShape());
        }
    }

    @Test
    public void chimneyDesign(){
        Collection<ACityElement> chimneys = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.Chimney, SAPNodeProperties.type_name, "Attribute");
        assertNotEquals(0, chimneys.size());

        for( ACityElement chimney : chimneys){
            assertEquals("#FFFF00", chimney.getColor());
            assertEquals(ACityElement.ACityShape.Cylinder, chimney.getShape());
        }
    }


}
