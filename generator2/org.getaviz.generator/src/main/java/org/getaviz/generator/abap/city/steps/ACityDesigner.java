package org.getaviz.generator.abap.city.steps;

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

public class ACityDesigner {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;


    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public ACityDesigner(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("created");
    }


    public void designRepository(){

        designACityElementsByType(ACityElement.ACityType.District);

        designACityElementsByType(ACityElement.ACityType.Building);

        designACityElementsByType(ACityElement.ACityType.Floor);

        designACityElementsByType(ACityElement.ACityType.Chimney);
    }

    private void designACityElementsByType(ACityElement.ACityType aCityType){
        log.info("Design " + aCityType.name());

        Map<String, AtomicInteger> counterMap = new HashMap<>();

        Collection<ACityElement> aCityElements = repository.getElementsByType(aCityType);
        log.info(aCityElements.size() + " " + aCityType.name() + " loaded");

        for (ACityElement aCityElement: aCityElements) {

            switch (aCityType) {
                case District: designDistrict(aCityElement); break;
                case Building: designBuilding(aCityElement); break;
                case Floor: designFloor(aCityElement); break;
                case Chimney: designChimney(aCityElement); break;
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

            if (district.getSourceNode() != null) {
                //namespace district
                district.setColor(config.getACityDistrictColorHex("packageDistrict"));
            } else {
                //type district
                switch (district.getSubType()) {
                    case Class:
                        district.setColor(config.getACityDistrictColorHex("classDistrict"));
                        break;
                    case Report:
                        district.setColor(config.getACityDistrictColorHex("reportDistrict"));
                        break;
                    case FunctionGroup:
                        district.setColor(config.getACityDistrictColorHex("functionGroupDistrict"));
                        break;
                    case Table:
                        district.setColor(config.getACityDistrictColorHex("tableDistrict"));
                        break;
                    case DDIC:
                        district.setColor(config.getACityDistrictColorHex("dataDictionaryDistrict"));
                        break;
                    //Debugger
                    case FunctionModule:
                        district.setColor(config.getACityDistrictColorHex("functionModuleDistrict"));
                        break;
                    case Method:
                        district.setColor(config.getACityDistrictColorHex("methodDistrict"));
                        break;
                    default:
                        district.setColor(config.getACityDistrictColorHex("defaultValue"));
                        log.error(district.getSubType().name() + " is not a valid type for \"district\"");
                        break;
                }
            }
    }

    private void designBuilding(ACityElement building) {
        String propertyTypeName = building.getSourceNodeProperty(SAPNodeProperties.type_name);

        switch (SAPNodeTypes.valueOf(propertyTypeName)) {

            case Class:
                building.setColor(config.getACityBuildingColorHex("classBuilding"));
                building.setShape(config.getACityBuildingShape("classBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Interface:
                building.setColor(config.getACityBuildingColorHex("interfaceBuilding"));
                building.setShape(config.getACityBuildingShape("interfaceBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Report:
                building.setColor(config.getACityBuildingColorHex("reportBuilding"));
                building.setShape(config.getACityBuildingShape("reportBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case FunctionGroup:
                building.setColor(config.getACityBuildingColorHex("functionGroupBuilding"));
                building.setShape(config.getACityBuildingShape("functionGroupBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Table:
                building.setColor(config.getACityBuildingColorHex("tableBuilding"));
                building.setShape(config.getACityBuildingShape("tableBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case DataElement:
                building.setColor(config.getACityBuildingColorHex("dataElementBuilding"));
                building.setShape(config.getACityBuildingShape("dataElementBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Domain:
                building.setColor(config.getACityBuildingColorHex("domainBuilding"));
                building.setShape(config.getACityBuildingShape("domainBuilding"));
                building.setWidth(building.getWidth() - config.adjustACityBuildingWidth());
                building.setLength(building.getLength() - config.adjustACityBuildingLength());
                break;
            case Structure:
                building.setColor(config.getACityBuildingColorHex("structureBuilding"));
                building.setShape(config.getACityBuildingShape("structureBuilding"));
                building.setWidth(config.getACityBuildingWidth("structureBuilding"));
                building.setLength(config.getACityBuildingLength("structureBuilding"));
                break;
            case TableType:
                building.setColor(config.getACityBuildingColorHex("tableTypeBuilding"));
                building.setShape(config.getACityBuildingShape("tableTypeBuilding"));
                building.setWidth(config.getACityBuildingWidth("tableTypeBuilding"));
                building.setLength(config.getACityBuildingLength("tableTypeBuilding"));
                break;
            default:
                building.setColor(config.getACityDistrictColorHex("defaultValue"));
                building.setShape(config.getACityBuildingShape("defaultValue"));
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
            case Method:
                floor.setColor(config.getACityFloorColorHex("methodFloor"));
                floor.setShape(config.getACityFloorShape("methodFloor"));
                floor.setYPosition(floor.getYPosition() - config.adjustACityFloorYPosition());
                break;
            case Report:
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
