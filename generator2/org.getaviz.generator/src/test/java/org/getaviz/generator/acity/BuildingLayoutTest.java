package org.getaviz.generator.acity;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.layouts.ACityBuildingLayout;
import org.getaviz.generator.abap.repository.ACityElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuildingLayoutTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();

    private static ACityElement building;
    private static List<ACityElement> floors;
    private static List<ACityElement> chimneys;

    @BeforeAll
    static void setup() {
        building = new ACityElement(ACityElement.ACityType.Building);
        floors = new ArrayList<>();
        chimneys = new ArrayList<>();

        //8 floors
        floors.add(new ACityElement(ACityElement.ACityType.Floor));
        floors.add(new ACityElement(ACityElement.ACityType.Floor));
        floors.add(new ACityElement(ACityElement.ACityType.Floor));
        floors.add(new ACityElement(ACityElement.ACityType.Floor));

        floors.add(new ACityElement(ACityElement.ACityType.Floor));
        floors.add(new ACityElement(ACityElement.ACityType.Floor));
        floors.add(new ACityElement(ACityElement.ACityType.Floor));
        floors.add(new ACityElement(ACityElement.ACityType.Floor));

        //10 chimneys
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));

        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));
        chimneys.add(new ACityElement((ACityElement.ACityType.Chimney)));


        ACityBuildingLayout buildingLayout = new ACityBuildingLayout(building, floors, chimneys, config);
        buildingLayout.calculate();
    }

    @Test
    void floorSize() {
        //groundArea = Ceil(Sqrt(10)) = 4
        for(ACityElement floor :  floors){
            assertEquals(4, floor.getWidth());
            assertEquals(4, floor.getLength());
            assertEquals(0.5, floor.getHeight());
        }
    }

    @Test
    void chimneySize(){
        for(ACityElement chimney : chimneys){
            assertEquals(0.5, chimney.getWidth());
            assertEquals(0.5, chimney.getLength());
            assertEquals(0.5, chimney.getHeight());
        }
    }

    @Test
    void floorPosition(){
        int floorCounter = 0;
        for(ACityElement floor :  floors){
            floorCounter++;

            if(floorCounter == 1){
                // floor.setYPosition = ((floor.getHeight() / 2) + floorSizeSum + floorGapSum);
                // --> floor.getHeight = 0.5
                // --> floorSizeSum = (floorCounter - 1) x floor.height
                // --> floorGapSum = floorCounter x 0.2
                // 1 x 0.25 + 0 x 0.5 + 1 x 0.2 = 0.45
                assertEquals(0.45, floor.getYPosition());
            }
            if(floorCounter == 5){
                //1 x 0.25 + 4 x 0.5 + 5 x 0.2 = 0.25 + 2 + 1 = 3.25
                assertEquals(3.25, floor.getYPosition());
            }
            if(floorCounter == 8){
                // 1 x 0.25 + 7 x 0.5 + 8 x 0.2 = 2 + 3.5 + 1.6 = 7.1
                assertEquals(5.35, floor.getYPosition());
            }

            assertEquals(0.0, floor.getXPosition());
            assertEquals(0.0, floor.getZPosition());
        }
    }

    @Test
    void chimneyPosition(){
        //groundAreaLength = roundUp( Sqrt( chimney.size ))
        //groundAreaLength = roundUp( Sqrt( 10 ))
        //groundAreaLength = 4

        int chimneyCounter = 0;
        for(ACityElement chimney :  chimneys){
            chimneyCounter++;

            /*  X---
                ----
                ----
                ---- */
            if(chimneyCounter == 1){
                // (groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX
                // chimney.width = 0.5
                // cornerX = -1
                // (2 - 0.25) x -1 = -1.75
                assertEquals(-1.75, chimney.getXPosition());

                // (groundAreaLength / 2 - chimney.getLength() / 2) * cornerZ
                // chimney.length = 0,5
                // cornerZ = 1
                assertEquals(1.75, chimney.getZPosition());
            }
            /*  O--X
                ----
                ----
                ---- */
            if(chimneyCounter == 2){ //default 1.5
                // (groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX
                // chimney.width = 0.5
                // cornerX = 1 -> 0.5?
                assertEquals(1.75, chimney.getXPosition());

                // (groundAreaLength / 2 - chimney.getLength() / 2) * cornerZ
                // chimney.length = 0,5
                // cornerZ = 1
                assertEquals(1.75, chimney.getZPosition());
            }
            /*  O--O
                ----
                ----
                ---X */
            if(chimneyCounter == 3){
                // (groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX
                // chimney.width = 0.5
                // cornerX = 1
                assertEquals(1.75, chimney.getXPosition());

                // (groundAreaLength / 2 - chimney.getLength() / 2) * cornerZ
                // chimney.length = 0,5
                // cornerZ = -1
                assertEquals(-1.75, chimney.getZPosition());
            }
            /*  O--O
                ----
                ----
                X--O */
            if(chimneyCounter == 4){
                // (groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX
                // chimney.width = 0.5
                // cornerX = -1
                assertEquals(-1.75, chimney.getXPosition());

                // (groundAreaLength / 2 - chimney.getLength() / 2) * cornerZ
                // chimney.length = 0,5
                // cornerZ = -1
                assertEquals(-1.75, chimney.getZPosition());
            }

            /*  OX-O
                ----
                ----
                O--O */
            if(chimneyCounter == 5){
                // ((groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX) - chimney.getWidth
                // chimney.width = 0.5
                // cornerX = -1
                assertEquals(-1.25, chimney.getXPosition());

                // (groundAreaLength / 2 - chimney.getLength() / 2) * cornerZ
                // chimney.length = 0,5
                // cornerZ = 1
                assertEquals(1.75, chimney.getZPosition());
            }
            /*  OO-O
                ---X
                ----
                O--O */
            if(chimneyCounter == 6){
                // (groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX
                // chimney.width = 0.5
                // cornerX = 1
                assertEquals(1.75, chimney.getXPosition());

                // ((groundAreaLength / 2 - chimney.getWidth() / 2) * cornerZ) - chimney.getLenght
                // chimney.width = 0.5
                // cornerZ = 1
                assertEquals(1.25, chimney.getZPosition());
            }
            /*  OO-O
                ---O
                ----
                O-XO */
            if(chimneyCounter == 7){
                // ((groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX) - chimneygetWidth
                // cornerX = 1
                assertEquals(1.25, chimney.getXPosition());

                //(groundAreaLength / 2 - chimney.getWidth() / 2) * cornerZ
                //cornerZ = -1
                assertEquals(-1.75, chimney.getZPosition());
            }
            /*  OO-O
                ---O
                X---
                O-OO */
            if(chimneyCounter == 8){
                assertEquals(-1.75, chimney.getXPosition());
                assertEquals(-1.25, chimney.getZPosition());
            }

            if(chimneyCounter == 9){
                // ((groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX) - 2 x chimneygetWidth
                assertEquals(-0.75, chimney.getXPosition());
                assertEquals(1.75, chimney.getZPosition());
            }

            // floorHeightSum + chimney.getHeight() / 2;
            // floorHeightSum = floor.getYPosition() + ( floor.getHeight() / 2);
            // 5.35 + 0.25 + 0.25 = 5.85
            assertEquals(5.85, chimney.getYPosition());
        }
    }

    @Test
    void buildingSize(){
        //groundAreaLength = roundUp( Sqrt( chimney.size ))
        //groundAreaLength = roundUp( Sqrt( 10 ))
        //groundAreaLength = 4

        assertEquals(4.0, building.getWidth());
        assertEquals(4.0, building.getLength());

        //8 x floor.height  + 8 x floor.gap + chimney.height
        //8 x 1             + 8 x 0.5       + 1
        //8 x 0.5           + 8 x 0.2       + 0.5 --> ohne chimney height?????
        assertEquals(5.6, building.getHeight());
    }

    @Test
    void buildingPosition(){

        assertEquals(0.0, building.getXPosition());

        // building.getYPosition = building.setYPosition(building.getHeight() / 2)
        // --> building.getHeight = 8 x floor.height  + 8 x floor.gap + chimney.height
        // 8 x 0.5           + 8 x 0.2       + 0.5 --> ohne chimney height????? ---> 5.6
        // 5.6 / 2 = 2.8
        assertEquals(2.8, building.getYPosition());
        assertEquals(0.0, building.getZPosition());
    }


}
