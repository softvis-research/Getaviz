package org.getaviz.generator.acity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
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

public class CreatorTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ABAPmock mockUp = new ABAPmock();
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;
    private static ACityElement aCityElementTest;
    static DatabaseConnector connector;

    @BeforeAll
    static void setup() {
        mockUp.setupDatabase("./test/databases/CityBankTest.db", "SAPExportCreateNodes.cypher");
        mockUp.runCypherScript("SAPExportCreateContainsRelation.cypher");
        mockUp.loadProperties("ABAPCityTest.properties");
        connector = mockUp.getConnector();

        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);

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
    void numberOfDistricts() {
        Collection<ACityElement> districts = aCityRepository.getElementsByType(ACityElement.ACityType.District);
        assertEquals(42, districts.size());
    }

    @Test
    void numberOfBuildings() {
        Collection<ACityElement> buildings = aCityRepository.getElementsByType(ACityElement.ACityType.Building);
        assertEquals(117, buildings.size());
    }

    @Test
    void buildingParentElements() {
        Collection<ACityElement> buildings = aCityRepository.getElementsByType(ACityElement.ACityType.Building);

        for (ACityElement building : buildings) {
            assertNotEquals(null, building.getParentElement());

            ACityElement.ACityType parentType = building.getParentElement().getType();
            assertEquals(ACityElement.ACityType.District, parentType);
        }
    }

    @Test
    void buildingSubElements(){
        Collection<ACityElement> buildings = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.Building, SAPNodeProperties.object_name, "/GSA/CL_VISAP_T_TEST_CLASS");

        ACityElement firstBuilding = buildings.iterator().next();
        assertEquals(9, firstBuilding.getSubElements().size());
    }

    @Test
    void floorParentElements(){
        Collection<ACityElement> floors = aCityRepository.getElementsByType(ACityElement.ACityType.Floor);

        for (ACityElement floor : floors) {
            assertNotEquals(null, floor.getParentElement());

            ACityElement.ACityType parentType = floor.getParentElement().getType();
            assertEquals(ACityElement.ACityType.Building, parentType);
        }
    }

    @Test
    void chimneyParentElements(){
        Collection<ACityElement> chimneys = aCityRepository.getElementsByType(ACityElement.ACityType.Chimney);

        for (ACityElement chimney : chimneys) {
            assertNotEquals(null, chimney.getParentElement());

            ACityElement.ACityType parentType = chimney.getParentElement().getType();
            assertEquals(ACityElement.ACityType.Building, parentType);
        }
    }


    @Test
    void districtSubElements(){

        //first district
        Collection<ACityElement> districts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.object_name, "/GSA/VISAP_T_TEST");
        assertEquals(1, districts.size());

        ACityElement firstDistrict = districts.iterator().next();

        Collection<ACityElement> subDistricts = firstDistrict.getSubElements();
        assertEquals(5, subDistricts.size());


        //second district
        districts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.object_name, "/GSA/VISAP_T");
        assertEquals(1, districts.size());

        ACityElement secondDistrict = districts.iterator().next();

        subDistricts = secondDistrict.getSubElements();
        assertEquals(1, subDistricts.size());

        //third district
        districts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.object_name, "/GISA/BWBCI");
        assertEquals(1, districts.size());

        ACityElement thirdDistrict = districts.iterator().next();

        subDistricts = thirdDistrict.getSubElements();
        assertEquals(4, subDistricts.size());
    }


}
