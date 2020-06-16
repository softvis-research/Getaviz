package org.getaviz.generator.abap.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;

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

        designMetropolisElementsByType(ACityElement.ACityType.Floor);

      //  designMetropolisElementsByType(ACityElement.ACityType.Chimney);
    }

    private void designMetropolisElementsByType(ACityElement.ACityType aCityType){
        log.info("Design " + aCityType.name());

        Map<String, AtomicInteger> counterMap = new HashMap<>();

        Collection<ACityElement> aCityElements = repository.getElementsByType(aCityType);
        log.info(aCityElements.size() + " " + aCityType.name() + " loaded");

        for (ACityElement aCityElement: aCityElements) {

            switch (aCityType) {
                case District: designDistrict(aCityElement); break;
                case Building: designBuilding(aCityElement); break;
                case Floor: designFloor(aCityElement); break;
               // case Chimney: designChimney(aCityElement); break;
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
            return aCityElement.getSubType().name() + "-TypeDistrict";
        }
        return aCityElement.getSourceNodeProperty(SAPNodeProperties.type_name);
    }


    private void designDistrict(ACityElement district) {

        district.setShape(config.getACityDistrictShape());

            String propertyTypeName = district.getSourceNodeProperty(SAPNodeProperties.type_name);

            switch (SAPNodeTypes.valueOf(propertyTypeName)) {
                case Namespace:     district.setColor(config.getMetropolisDistrictColorHex("packageDistrict")); break;
                case Class:         district.setColor(config.getMetropolisDistrictColorHex("classDistrict"));
                                    district.setHeight(config.getACityDistrictHeight()); break;
                case Interface:     district.setColor(config.getMetropolisDistrictColorHex("interfaceDistrict"));
                                    district.setHeight(config.getACityDistrictHeight()); break;
                case Report:        district.setColor(config.getMetropolisDistrictColorHex("reportDistrict"));
                                    district.setHeight(config.getACityDistrictHeight());
                    break;
                case FunctionGroup: district.setColor(config.getMetropolisDistrictColorHex("functionGroupDistrict"));
                                    district.setHeight(config.getACityDistrictHeight()); break;
                case Table:
                case TableType:     district.setColor(config.getMetropolisDistrictColorHex("tableDistrict"));
                                    district.setHeight(config.getACityDistrictHeight()); break;
                case Structure:     district.setColor(config.getMetropolisDistrictColorHex("structureDistrict"));
                                    district.setHeight(config.getACityDistrictHeight()); break;
                case DataElement:   district.setColor(config.getMetropolisDistrictColorHex("dataElementDistrict"));
                                    district.setHeight(config.getACityDistrictHeight()); break;
                default:            district.setColor(config.getMetropolisDistrictColorHex("defaultValue"));
                    log.error(district.getSubType().name() + " is not a valid type for \"district\"");
                    district.setHeight(config.getACityDistrictHeight()); break;
            }
    }

    private void designBuilding(ACityElement building) {
        String propertyTypeName = building.getSourceNodeProperty(SAPNodeProperties.type_name);

        switch (SAPNodeTypes.valueOf(propertyTypeName)) {

            case Class:
                building.setColor(config.getMetropolisBuildingColorHex("classBuilding"));
                building.setShape(config.getMetropolisBuildingShape("classBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Interface:
                building.setColor(config.getMetropolisBuildingColorHex("interfaceBuilding"));
                building.setShape(config.getMetropolisBuildingShape("interfaceBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Method:
                building.setColor(config.getMetropolisBuildingColorHex("methodBuilding"));
                building.setShape(config.getMetropolisBuildingShape("methodBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Report:
                building.setColor(config.getMetropolisBuildingColorHex("reportBuilding"));
                building.setShape(config.getMetropolisBuildingShape("reportBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case FormRoutine:
                building.setColor(config.getMetropolisBuildingColorHex("formRoutineBuilding"));
                building.setShape(config.getMetropolisBuildingShape("formRoutineBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case FunctionGroup:
                building.setColor(config.getMetropolisBuildingColorHex("functionGroupBuilding"));
                building.setShape(config.getMetropolisBuildingShape("functionGroupBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case FunctionModule:
                building.setColor(config.getMetropolisBuildingColorHex("functionModuleBuilding"));
                building.setShape(config.getMetropolisBuildingShape("functionModuleBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Table:
                building.setColor(config.getMetropolisBuildingColorHex("tableBuilding"));
                building.setShape(config.getMetropolisBuildingShape("tableBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case DataElement:
                building.setColor(config.getMetropolisBuildingColorHex("dataElementBuilding"));
                building.setShape(config.getMetropolisBuildingShape("dataElementBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Domain:
                building.setColor(config.getMetropolisBuildingColorHex("domainBuilding"));
                building.setShape(config.getMetropolisBuildingShape("domainBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case StructureElement:
                building.setColor(config.getMetropolisBuildingColorHex("structureBuilding"));
                building.setShape(config.getMetropolisBuildingShape("structureBuilding"));
                building.setWidth(config.getACityBuildingWidth("structureBuilding"));
                building.setLength(config.getACityBuildingLength("structureBuilding"));
                break;
            case TableType:
                building.setColor(config.getMetropolisBuildingColorHex("tableTypeBuilding"));
                building.setShape(config.getMetropolisBuildingShape("tableTypeBuilding"));
                building.setWidth(config.getACityBuildingWidth("tableTypeBuilding"));
                building.setLength(config.getACityBuildingLength("tableTypeBuilding"));
                break;
            default:
                building.setColor(config.getMetropolisDistrictColorHex("defaultValue"));
                building.setShape(config.getMetropolisBuildingShape("defaultValue"));
                building.setWidth(config.getACityBuildingWidth("defaultValue"));
                building.setLength(config.getACityBuildingLength("defaultValue"));
                log.error(propertyTypeName + " is not a valid type for \"building\"");
                break;
        }
    }


    private void designChimney(ACityElement chimney) {

        chimney.setColor(config.getACityChimneyColorHex("attributeColor"));
        chimney.setShape(config.getACityChimneyShape("attributeChimney"));
    }


    private void designFloor(ACityElement floor) {

        String propertyTypeName = floor.getSourceNodeProperty(SAPNodeProperties.type_name);

        switch (SAPNodeTypes.valueOf(propertyTypeName)) {
            /*case Method:
                floor.setColor(config.getACityFloorColorHex("methodFloor"));
                floor.setShape(config.getACityFloorShape("methodFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
            case FormRoutine:
                floor.setColor(config.getACityFloorColorHex("formroutineFloor"));
                floor.setShape(config.getACityFloorShape("formroutineFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
            case FunctionModule:
                floor.setColor(config.getACityFloorColorHex("functionModuleFloor"));
                floor.setShape(config.getACityFloorShape("functionModuleFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
             */
            case TableElement:
                floor.setColor(config.getACityFloorColorHex("tableElementFloor"));
                floor.setShape(config.getACityFloorShape("tableElementFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
            case StructureElement:
                floor.setColor(config.getACityFloorColorHex("structureElementFloor"));
                floor.setShape(config.getACityFloorShape("structureElementFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
            case DataElement:
                floor.setColor(config.getACityFloorColorHex("dataElementFloor"));
                floor.setShape(config.getACityFloorShape("dataElementFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                floor.setWidth(floor.getWidth() - config.adjustACityFloorWidth());
                floor.setLength(floor.getLength() - config.adjustACityFloorLength());
                break;
            default:
                floor.setColor(config.getACityFloorColorHex("dataElementFloor"));
                floor.setShape(config.getACityFloorShape("dataElementFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                floor.setWidth(floor.getWidth() - config.adjustACityFloorWidth());
                floor.setLength(floor.getLength() - config.adjustACityFloorLength());
                log.error(propertyTypeName + " is not a valid type for \"floor\"");
                break;
        }
    }


}
