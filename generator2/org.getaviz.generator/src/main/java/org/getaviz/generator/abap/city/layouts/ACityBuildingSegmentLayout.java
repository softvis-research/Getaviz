package org.getaviz.generator.abap.city.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.repository.ACityElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ACityBuildingSegmentLayout {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement building;
    private Collection<ACityElement> floors;
    private Collection<ACityElement> chimneys;

    public ACityBuildingSegmentLayout(ACityElement building, Collection<ACityElement> floors, Collection<ACityElement> chimneys, SettingsConfiguration config) {
        this.config = config;

        this.building = building;
        this.floors = floors;
        this.chimneys = chimneys;
    }

    public void calculate(){

        setSizeOfChimneys();
        setSizeOfFloors();

        setPositionOfFloors();
        setPositionOfChimneys();

    }

    private void setPositionOfChimneys() {
        if (chimneys.size() == 0) {
            return;
        }

        Double floorHeightSum = calculateFloorHeightSum();
        Double groundAreaLength = calculateGroundAreaByChimneyAmount();

        List<ACityElement> upperLeft = new ArrayList<>();
        List<ACityElement> upperRight = new ArrayList<>();
        List<ACityElement> lowerRight = new ArrayList<>();
        List<ACityElement> lowerLeft = new ArrayList<>();

        int chimneyCounter = 0;

        for(ACityElement chimney : chimneys){
            int counterRemainder = chimneyCounter % 4;
            switch (counterRemainder){
                case 0: upperLeft.add(chimney); break;
                case 1: upperRight.add(chimney); break;
                case 2: lowerRight.add(chimney); break;
                case 3: lowerLeft.add(chimney); break;
            }
            chimneyCounter++;
        }

        setPositionOfChimneysInCorner(upperLeft, groundAreaLength, floorHeightSum, -1, 1);
        setPositionOfChimneysInCorner(upperRight, groundAreaLength, floorHeightSum, 1, 1);
        setPositionOfChimneysInCorner(lowerRight, groundAreaLength, floorHeightSum, 1, -1);
        setPositionOfChimneysInCorner(lowerLeft, groundAreaLength, floorHeightSum, -1, -1);
    }



    private void setPositionOfChimneysInCorner(List<ACityElement> corner, Double groundAreaLength, Double floorHeightSum, int cornerX, int cornerZ) {

        double chimneyXPosition = 0.0;
        double chimneyYPosition = 0.0;
        double chimneyZPosition = 0.0;

        int cornerCounter = 0;
        for(ACityElement chimney: corner){

            if(cornerCounter == 0){
                chimneyXPosition = (groundAreaLength / 2 - chimney.getWidth() / 2) * cornerX ;
                chimneyYPosition = floorHeightSum + chimney.getHeight() / 2;
                chimneyZPosition = (groundAreaLength / 2 - chimney.getLength() / 2) * cornerZ;
            }

            chimney.setXPosition(chimneyXPosition);
            chimney.setYPosition(chimneyYPosition);
            chimney.setZPosition(chimneyZPosition);

            cornerCounter++;
            //upper left corner
            if(cornerX < 0 && cornerZ > 0){
                chimneyXPosition = chimneyXPosition + chimney.getWidth() + config.getACityChimneyGap();
            }
            //upper right corner
            if(cornerX > 0 && cornerZ > 0){
                chimneyZPosition = chimneyZPosition - chimney.getLength() + config.getACityChimneyGap();
            }
            //lower right corner
            if(cornerX > 0 && cornerZ < 0){
                chimneyXPosition = chimneyXPosition - chimney.getWidth() + config.getACityChimneyGap();
            }
            //lower left corner
            if(cornerX < 0 && cornerZ < 0){
                chimneyZPosition = chimneyZPosition + chimney.getLength() + config.getACityChimneyGap();
            }
        }
    }


    private void setPositionOfFloors() {
        int floorCounter = 0;
        for(ACityElement floor : floors){
            floorCounter++;

            Double floorSizeSum = (floorCounter - 1) * floor.getHeight();
            Double floorGapSum = floorCounter * config.getACityFloorGap(); // default * 0.5
            floor.setYPosition((floor.getHeight() / 2) + floorSizeSum + floorGapSum);

            floor.setXPosition(0.0);
            floor.setZPosition(0.0);
        }
    }

    private void setSizeOfFloors() {
        Double groundAreaLength = calculateGroundAreaByChimneyAmount();

        for(ACityElement floor : floors){
            floor.setHeight(config.getACityFloorHeight()); //default: 1
            floor.setWidth(groundAreaLength);
            floor.setLength(groundAreaLength);
        }
    }



    private void setSizeOfChimneys() {
        for(ACityElement chimney : chimneys){
            chimney.setHeight(config.getACityChimneyHeight());
            chimney.setWidth(config.getACityChimneyWidth());
            chimney.setLength(config.getACityChimneyLength());
        }
    }

    private Double calculateFloorHeightSum() {
        double floorHeightSum = config.getFloorHeightSum();
        for(ACityElement floor : floors){
            double floorTopEdge = floor.getYPosition() + ( floor.getHeight() / 2);
            if(floorTopEdge > floorHeightSum){
                floorHeightSum = floorTopEdge;
            }
        }
        return floorHeightSum;
    }

    private double calculateGroundAreaByChimneyAmount() {
        if (chimneys.size() < 2){
            return config.getACityGroundAreaByChimneyAmount();
        }

        int chimneyAmount = chimneys.size();
        double chimneySurface = Math.sqrt(chimneyAmount);

        return Math.ceil(chimneySurface);
    }

}
