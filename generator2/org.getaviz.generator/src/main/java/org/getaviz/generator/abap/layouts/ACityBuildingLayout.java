package org.getaviz.generator.abap.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;

import java.util.Collection;

public class ACityBuildingLayout {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement building;
    private Collection<ACityElement> floors;
    private Collection<ACityElement> chimneys;

    public ACityBuildingLayout(ACityElement building, Collection<ACityElement> floors, Collection<ACityElement> chimneys, SettingsConfiguration config) {
        this.config = config;

        this.building = building;
        this.floors = floors;
        this.chimneys = chimneys;
    }



    public void calculate(){

        ACityBuildingSegmentLayout buildingSegmentLayout = new ACityBuildingSegmentLayout(building, floors, chimneys, config);
        buildingSegmentLayout.calculate();

        setSizeOfBuilding();
        setPositionOfBuilding();

    }

    private void setPositionOfBuilding() {
        building.setXPosition(0.0);
        building.setYPosition(building.getHeight() / 2);
        building.setZPosition(0.0);
    }

    private void setSizeOfBuilding() {
        Double floorHeightSum = calculateFloorHeightSum();
        Double biggestChimneyHeight = getBiggestChimneyHeight();
        Double groundAreaLength = calculateGroundAreaByChimneyAmount();

        building.setWidth(groundAreaLength);
        building.setLength(groundAreaLength);
        building.setHeight(floorHeightSum);

      }

    private Double getBiggestChimneyHeight() {
        double biggestChimneyHeight = 0.0;
        for(ACityElement chimney : chimneys){
            double chimneyHeight = chimney.getHeight();
            if(chimneyHeight > biggestChimneyHeight){
                biggestChimneyHeight = chimneyHeight;
            }
        }
        return biggestChimneyHeight;
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
