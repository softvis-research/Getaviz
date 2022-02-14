package org.getaviz.generator.abapCity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.layouts.ABuildingLayout;
import org.getaviz.generator.layouts.ADistrictCircluarLayout;
import org.getaviz.generator.repository.ACityElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DistrictLayoutTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ACityElement district;


    @BeforeAll
    static void setup() {
        district = new ACityElement(ACityElement.ACityType.District);

        ACityElement building = createBuilding(8, 10);
        district.addSubElement(building);

        building = createBuilding(3, 2);
        district.addSubElement(building);

        building = createBuilding(5, 9);
        district.addSubElement(building);

        building = createBuilding(14, 28);
        district.addSubElement(building);


        ADistrictCircluarLayout districtLayout = new ADistrictCircluarLayout(district, district.getSubElements(), config);
        districtLayout.calculate();
    }

    private static ACityElement createBuilding(int floorAmount, int chimneyAmount) {
        ACityElement building = new ACityElement(ACityElement.ACityType.Building);
        List<ACityElement> floors = new ArrayList<>();
        List<ACityElement> chimneys = new ArrayList<>();

        for(int i = 0; i < floorAmount; i++){
            ACityElement floor = new ACityElement(ACityElement.ACityType.Floor);
            building.addSubElement(floor);
            floors.add(floor);
        }
        for(int i = 0; i < chimneyAmount; i++){
            ACityElement chimney = new ACityElement(ACityElement.ACityType.Chimney);
            building.addSubElement(chimney);
            chimneys.add(chimney);
        }
        ABuildingLayout buildingLayout = new ABuildingLayout(building, floors, chimneys, config);
        buildingLayout.calculate();
        return building;
    }

    @Test
    void buildingPositions(){
        Collection<ACityElement> buildings = district.getSubElementsOfType(ACityElement.ACityType.Building);
        for(ACityElement building : buildings){
            assertNotEquals(0.0, building.getXPosition());
            assertNotEquals(0.0, building.getYPosition());
            assertNotEquals(0.0, building.getZPosition());
        }
    }

    @Test
    void buildingSubElementsPositions(){
        Collection<ACityElement> buildings = district.getSubElementsOfType(ACityElement.ACityType.Building);
        for(ACityElement building : buildings){

            Collection<ACityElement> subElements = building.getSubElements();
            for(ACityElement subElement : subElements){

                assertNotEquals(0.0, subElement.getXPosition());
                assertNotEquals(0.0, subElement.getYPosition());
                assertNotEquals(0.0, subElement.getZPosition());
            }
        }
    }


    @Test
    void districtSize(){
        assertNotEquals(0.0, district.getWidth());
        assertNotEquals(0.0, district.getLength());
        assertNotEquals(0.0, district.getHeight());
    }

    @Test
    void districtPosition(){
        assertNotEquals(0.0, district.getXPosition());
        assertNotEquals(0.0, district.getYPosition());
        assertNotEquals(0.0, district.getZPosition());
    }


}