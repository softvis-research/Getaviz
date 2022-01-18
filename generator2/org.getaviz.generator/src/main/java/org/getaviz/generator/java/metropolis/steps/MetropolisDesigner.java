package org.getaviz.generator.java.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeProperties;
import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.generator.java.repository.ACityElement;
import org.getaviz.generator.java.repository.ACityRepository;
import org.getaviz.generator.java.repository.SourceNodeRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MetropolisDesigner {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;


    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public MetropolisDesigner(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("*****************************************************************************************************************************************");
        log.info("created");
    }

    public void designRepository(){
        designMetropolisElementsByType(ACityElement.ACityType.District);

        designMetropolisElementsByType(ACityElement.ACityType.Building);

        designMetropolisElementsByType(ACityElement.ACityType.Reference);

        designMetropolisElementsByType(ACityElement.ACityType.Floor);
    }

//    private void designMetropolisElementsByMigrationFindings() {
//
//       Collection<ACityElement> migrationElements = repository.getElementsBySourceProperty(SAPNodeProperties.migration_findings, "true");
//
//        for ( ACityElement migrationElement: migrationElements) {
//
//            if (!migrationElement.getSourceNodeType().equals(SAPNodeTypes.Namespace)) {
//                    migrationElement.setColor("#FF8C00");
//
//                    SAPNodeTypes typeParent = migrationElement.getParentElement().getSourceNodeType();
//                    if(typeParent.equals(SAPNodeTypes.Report)){
//                        migrationElement.getParentElement().setColor("#FF8C00");
//                }
//            }
//        }
//    }

    private void designMetropolisElementsByType(ACityElement.ACityType aCityType){
        log.info("Design " + aCityType.name());

        Map<String, AtomicInteger> counterMap = new HashMap<>();

        Collection<ACityElement> aCityElements = repository.getElementsByType(aCityType);
        log.info(aCityElements.size() + " " + aCityType.name() + " loaded");

        for (ACityElement aCityElement: aCityElements) {
            switch (aCityType) {
                case District:
                    designDistrict(aCityElement);
                    break;
                case Building:
                    designBuilding(aCityElement);
                    break;
                case Reference:
                    designReference(aCityElement);
                    break;
                case Floor:
                    designFloor(aCityElement);
                    break;
                case Chimney:
                    designChimney(aCityElement);
                    break;
                default:
                    designBuilding(aCityElement);
                    log.error(aCityType.name() + "is not a valid cityType");
                    break;
            }
            countACityElementByType(counterMap, aCityElement);
        }

        counterMap.forEach( (propertyTypeName, counter) -> {
            log.info(counter + " " + aCityType.name() + "s of type " + propertyTypeName + " designed" );
        });
    }

    private void countACityElementByType(Map<String, AtomicInteger> counterMap, ACityElement aCityElement){

        String propertyTypeName = getPropertyTypeName(aCityElement);

        if(!counterMap.containsKey(propertyTypeName)){
            counterMap.put(propertyTypeName, new AtomicInteger(0));
        }
        AtomicInteger counterValue = counterMap.get(propertyTypeName);
        counterValue.addAndGet(1);
    }

    private String getPropertyTypeName(ACityElement aCityElement){
        if(aCityElement.getSubType() != null){
            return aCityElement.getSubType().name() + "-ReferenceBuilding";
        }
        return aCityElement.getSourceNodeProperty(JavaNodeProperties.type_name);
    }

    private void designDistrict(ACityElement district) {
        district.setShape(config.getACityDistrictShapeForJava());
        String propertyTypeName = district.getSourceNodePropertyForDesigner(JavaNodeProperties.type_name);
        switch (JavaNodeTypes.valueOf(propertyTypeName)) {
            case Package:
                district.setColor(config.getMetropolisDistrictColorHex("packageDistrict"));
                break;
            case Class:
                district.setColor(config.getMetropolisDistrictColorHex("classDistrict"));
                break;
            case Interface:
                district.setColor(config.getMetropolisDistrictColorHex("interfaceDistrict"));
                break;
            default:
                district.setColor(config.getMetropolisDistrictColorHex("defaultValue"));
                log.error(district.getSubType().name() + " is not a valid type for \"district\"");
                district.setHeight(config.getACityDistrictHeight());
                break;
        }
    }

    private void designBuilding(ACityElement building) {
        //building.setYPosition(building.getYPosition() + 10);

        ACityElement.ACitySubType refBuildingType = building.getSubType();

        if (building.getSourceNode() == null && refBuildingType == null) {
            return;
        } else if ( refBuildingType != null) {
            switch (refBuildingType) {
                case Sea:
                    building.setColor(config.getMetropolisBuildingColorHex("seaReferenceBuilding"));
                    building.setShape(ACityElement.ACityShape.Circle);
                    building.setTextureSource("#sea");
                    building.setRotation(config.getMetropolisBuildingRotation());
                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    //building.setYPosition(building.getYPosition() + 1);
                    break;
                case Mountain:
                    building.setColor(config.getMetropolisBuildingColorHex("mountainReferenceBuilding"));
                    building.setShape(ACityElement.ACityShape.Entity);
                    building.setModel("#mountain");
                    building.setModelScale(config.getMetropolisReferenceBuildingModelScale());
                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    break;
                case Cloud:
                    building.setShape(ACityElement.ACityShape.Entity);
                    building.setModel("#cloud_black");
                    building.setModelScale("0.3 0.3 0.3");
                    building.setYPosition(55);
                    break;
            }
        } else {
            String propertyTypeName = building.getSourceNodePropertyForDesigner(JavaNodeProperties.type_name);
            switch (JavaNodeTypes.valueOf(propertyTypeName)) {
                case Interface:
                    building.setColor(config.getMetropolisBuildingColorHex("interfaceBuilding"));
                    building.setShape(config.getMetropolisBuildingShapeForJava("interfaceBuilding"));
                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    break;
                case Method:
                    building.setColor(config.getMetropolisBuildingColorHex("methodBuilding"));
                    building.setShape(config.getMetropolisBuildingShapeForJava("methodBuilding"));
                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    break;
                case Field:
                    building.setColor(config.getMetropolisBuildingColorHex("attributeBuilding"));
                    building.setShape(config.getMetropolisBuildingShapeForJava("attributeBuilding"));
//                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
//                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    break;
                default:
                    building.setColor(config.getMetropolisDistrictColorHex("defaultValue"));
                    building.setShape(config.getMetropolisBuildingShapeForJava("defaultValue"));
                    building.setWidth(config.getACityBuildingWidth("defaultValue"));
                    building.setLength(config.getACityBuildingLength("defaultValue"));
                    log.error(propertyTypeName + " is not a valid type for \"building\"");
                    break;
            }
        }
    }

    private void designReference(ACityElement building) {

        ACityElement.ACitySubType refBuildingType = building.getSubType();

        if (building.getSourceNode() == null && refBuildingType == null) {
            return;
        } else if ( refBuildingType != null) {
            switch (refBuildingType) {
                case Sea:
                    building.setColor(config.getMetropolisBuildingColorHex("seaReferenceBuilding"));
                    building.setShape(ACityElement.ACityShape.Circle);
                    building.setTextureSource("#sea");
                    building.setRotation(config.getMetropolisBuildingRotation());
                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    //building.setYPosition(building.getYPosition() + 1);
                    break;
                case Mountain:
                    building.setColor(config.getMetropolisBuildingColorHex("mountainReferenceBuilding"));
                    building.setShape(ACityElement.ACityShape.Entity);
                    building.setModel("#mountain");
                    building.setModelScale(config.getMetropolisReferenceBuildingModelScale());
                    building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                    building.setLength(building.getLength() - config.adjustACityBuildingLength());
                    building.setYPosition(building.getYPosition() + config.adjustReferenceYPosition());
                    break;
                case Cloud:
                    building.setShape(ACityElement.ACityShape.Entity);
                    building.setModel("#cloud_black");
                    building.setModelScale("0.3 0.3 0.3");
                    building.setYPosition(55);
                    break;
            }
        }
    }

    private void designChimney(ACityElement chimney) {
        chimney.setColor(config.getACityChimneyColorHex("attributeColor"));
        chimney.setShape(config.getACityChimneyShapeForJava("attributeChimney"));
    }

    private void designFloor(ACityElement floor) {

        String propertyTypeName = floor.getSourceNodeProperty(JavaNodeProperties.type_name);

        switch (JavaNodeTypes.valueOf(propertyTypeName)) {
            /*case Method:
                floor.setColor(config.getACityFloorColorHex("methodFloor"));
                floor.setShape(config.getACityFloorShape("methodFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
             */
            default:
                floor.setColor(config.getACityFloorColorHex("dataElementFloor"));
                floor.setShape(config.getACityFloorShapeForJava("dataElementFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                floor.setWidth(floor.getWidth() - config.adjustACityFloorWidth());
                floor.setLength(floor.getLength() - config.adjustACityFloorLength());
                log.error(propertyTypeName + " is not a valid type for \"floor\"");
                break;
        }
    }
}
