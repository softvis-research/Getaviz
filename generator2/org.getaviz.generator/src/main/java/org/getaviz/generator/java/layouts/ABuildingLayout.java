package org.getaviz.generator.java.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeProperties;
import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.generator.java.repository.ACityElement;

import java.util.Collection;
import java.util.Objects;

public class ABuildingLayout {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement building;
    private Collection<ACityElement> floors;
    private Collection<ACityElement> chimneys;

    public ABuildingLayout(ACityElement building, Collection<ACityElement> floors, Collection<ACityElement> chimneys, SettingsConfiguration config) {
        this.config = config;

        this.building = building;
        this.floors = floors;
        this.chimneys = chimneys;
    }

    public void calculate(){
        ABuildingSegmentLayout buildingSegmentLayout = new ABuildingSegmentLayout(building, floors, chimneys, config);
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

        ACityElement.ACitySubType referenceBuildingType = building.getSubType();

        if(referenceBuildingType != null) {
            switch (referenceBuildingType) {
                case Sea:
                    building.setHeight(config.getMetropolisReferenceBuildingHeigth("seaReferenceBuilding"));
                    building.setWidth(config.getMetropolisReferenceBuildingWidth("seaReferenceBuilding"));
                    building.setLength(config.getMetropolisReferenceBuildingLength("seaReferenceBuilding"));
                    break;

                case Mountain:
                    building.setHeight(config.getMetropolisReferenceBuildingHeigth("mountainReferenceBuilding"));
                    building.setWidth(config.getMetropolisReferenceBuildingWidth("mountainReferenceBuilding"));
                    building.setLength(config.getMetropolisReferenceBuildingLength("mountainReferenceBuilding"));
                    break;

                case Cloud:
                    building.setHeight(config.getMetropolisReferenceBuildingHeigth("cloudReferenceBuilding"));
                    building.setWidth(config.getMetropolisReferenceBuildingWidth("cloudReferenceBuilding"));
                    building.setLength(config.getMetropolisReferenceBuildingLength("cloudReferenceBuilding"));
                    break;
            }
        } else {
            Double floorHeightSum = calculateFloorHeightSum();
            Double biggestChimneyHeight = getBiggestChimneyHeight();
            Double groundAreaLength = calculateGroundAreaByChimneyAmount();

            building.setWidth(groundAreaLength);
            building.setLength(groundAreaLength);
            building.setHeight(floorHeightSum);
        }
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
        // no floors & no effectiveLineCount => default
        double floorHeightSum = config.getFloorHeightSum();

        //no floors, but effectiveLineCount
        if (floors.isEmpty()){
            if(building.getSourceNode() == null){
                return floorHeightSum;
            }
            String effectiveLineCount = building.getSourceNodeProperty(JavaNodeProperties.effectiveLineCount);

            if(!Objects.equals(effectiveLineCount, "NULL")) {
                Double elc = Double.valueOf(effectiveLineCount);

                //numberOfStatements = 0 --> concerns SAP Standard objects
                if (elc == 0){
                    String type_name = building.getSourceNodeProperty(JavaNodeProperties.type_name);

                    if (!type_name.equals(JavaNodeTypes.Field)) {
                        floorHeightSum = config.getAbapStandardCodeHeight();
                    }
                } else {
                    floorHeightSum = getScaledHeightNew(elc);
                }
            }
        }

        //floors, but no numberOfStatements
        for (ACityElement floor : floors) {
            double floorTopEdge = floor.getYPosition() + (floor.getHeight() / 2);
            if (floorTopEdge > floorHeightSum) {
                floorHeightSum = floorTopEdge;
            }
        }

        return floorHeightSum;
    }

//    private double getScaledHeight(double unscaledHeight) {
//        if (unscaledHeight < config.getAbapScoMinHeight()) {
//            return config.getAbapScoMinHeight();
//        } else if (unscaledHeight > config.getAbapScoMaxHeight()) {
//            return config.getAbapScoMaxHeight();
//        } else {
//            return unscaledHeight;
//        }
//    }

    private double getScaledHeightNew(double unscaledHeight) {

        unscaledHeight = unscaledHeight / config.getACityFactorMaxHeight();

        if (unscaledHeight < config.getAbapScoMinHeight()) {
            return config.getAbapScoMinHeight();
        } else if (unscaledHeight > config.getAbapScoMaxHeight()) {
            return config.getAbapScoMaxHeight();
        } else {
            return unscaledHeight;
        }
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
