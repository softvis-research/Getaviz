package org.getaviz.generator.abapMetropolis;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.steps.ACityCreator;
import org.getaviz.generator.abap.city.steps.ACityDesigner;
import org.getaviz.generator.abap.common.steps.AFrameExporter;
import org.getaviz.generator.abap.common.steps.MetaDataExporter;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.metropolis.steps.MetropolisCreator;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.mockups.ABAPmock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MetropolisCreatorTest {

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
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);

        aCityRepository = new ACityRepository();

        MetropolisCreator creator = new MetropolisCreator(aCityRepository, nodeRepository, config);
        creator.createRepositoryFromNodeRepository();

        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        aCityRepository.writeRepositoryToNeo4j();
    }

    @AfterAll
    static void close() { connector.close(); }

    @Test
    void numberOfDistricts() {
        Collection<ACityElement> districts = aCityRepository.getElementsByType(ACityElement.ACityType.District);
        assertEquals(87, districts.size());
    }

    @Test
    void numberOfBuildings() {
        Collection<ACityElement> buildings = aCityRepository.getElementsByType(ACityElement.ACityType.Building);
        assertEquals(181, buildings.size());
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
        assertEquals(33, subDistricts.size());


        //second district
        districts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.object_name, "/GISA/BWBCI");
        assertEquals(1, districts.size());

        ACityElement thirdDistrict = districts.iterator().next();

        subDistricts = thirdDistrict.getSubElements();
        assertEquals(6, subDistricts.size());
    }

}
