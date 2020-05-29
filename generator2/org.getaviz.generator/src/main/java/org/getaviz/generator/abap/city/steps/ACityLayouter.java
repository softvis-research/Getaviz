package org.getaviz.generator.abap.city.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.enums.SAPNodeProperties;
import org.getaviz.generator.abap.city.enums.SAPNodeTypes;
import org.getaviz.generator.abap.city.enums.SAPRelationLabels;
import org.getaviz.generator.abap.city.layouts.ACityBuildingLayout;
import org.getaviz.generator.abap.city.layouts.ACityDistrictLayout;
import org.getaviz.generator.abap.city.repository.ACityElement;
import org.getaviz.generator.abap.city.repository.ACityRepository;
import org.getaviz.generator.abap.city.repository.SourceNodeRepository;
import org.neo4j.driver.v1.types.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ACityLayouter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;


    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public ACityLayouter(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("created");

    }


    public void layoutRepository(){

        AtomicInteger counter = new AtomicInteger(0);
        Map<String, AtomicInteger> counterMap = new HashMap<>();

        Collection<ACityElement> buildings = repository.getElementsByType(ACityElement.ACityType.Building);
        log.info(buildings.size() + " buildings loaded");

        for (ACityElement building: buildings) {

            SAPNodeTypes sourceNodeType = building.getSourceNodeType();

            if(sourceNodeType == SAPNodeTypes.TableType) {
                SAPNodeTypes buildingSourceType = getTableTypeTypeOfType(building);

                if(buildingSourceType != null){
                    counter.addAndGet(1);
                    layoutTableTypeBuilding(building, buildingSourceType);
                }
                countTableTypsByRowtype(counterMap, building);
            } else {
                layoutBuilding(building);
            }
        }

        log.info(counter + " \"TableTypes\" exists ");
        counterMap.forEach( (propertyRowtype, rowtypeCounter) -> {
            log.info(rowtypeCounter + " " + SAPNodeTypes.TableType.name() + "s with rowtype \"" + propertyRowtype + "\" layouted" );
        });

        layoutParentDistricts(buildings);



    }

    private SAPNodeTypes getTableTypeTypeOfType(ACityElement building) {

        Node tableTypeSourceNode = building.getSourceNode();

        Collection<Node> typeOfNodes = nodeRepository.getRelatedNodes(tableTypeSourceNode, SAPRelationLabels.TYPEOF, true);
        if(typeOfNodes.isEmpty()){
            String tableTypeName = building.getSourceNodeProperty(SAPNodeProperties.object_name);
            log.warn("TYPEOF related nodes not found for tableType \"" + tableTypeName + "\"");
            return null;
        }
        if(typeOfNodes.size() != 1){
            String tableTypeName = building.getSourceNodeProperty(SAPNodeProperties.object_name);
            log.error("TYPEOF related nodes more than 1 for tableType \"" + tableTypeName + "\"");
            return null;
        }

        String typeOfNodeTypeProperty = building.getSourceNodeProperty(SAPNodeProperties.type_name);

        return SAPNodeTypes.valueOf(typeOfNodeTypeProperty);
    }

    private void layoutTableTypeBuilding(ACityElement building, SAPNodeTypes typeOfType) {

        switch (typeOfType){
            case Class:
            case Interface:
                building.setHeight(config.getACityTableTypeBuildingHeight("tableTypeBuilding_class"));
                break;
            case Table:
            case TableType:
                building.setHeight(config.getACityTableTypeBuildingHeight("tableTypeBuilding_table"));
                break;
            case Structure:
                building.setHeight(config.getACityTableTypeBuildingHeight("tableTypeBuilding_structure"));
                break;
            case DataElement:
                building.setHeight(config.getACityTableTypeBuildingHeight("tableTypeBuilding_dateElement"));
            default:
                building.setHeight(1);
                String tableTypeName = building.getSourceNodeProperty(SAPNodeProperties.type_name);
                log.error("Type \"" + typeOfType + "\" not allowed for tableType-Element \"" +  tableTypeName );
        }


        Double groundAreaLength = config.getACityGroundAreaByChimneyAmount();
        building.setWidth(groundAreaLength);
        building.setLength(groundAreaLength);

        building.setXPosition(0.0);
        building.setYPosition(building.getHeight() / 2);
        building.setZPosition(0.0);
    }

    private void countTableTypsByRowtype(Map<String, AtomicInteger> counterMap, ACityElement aCityElement){

        String propertyRowtype = getRowtype(aCityElement);

        if(!counterMap.containsKey(propertyRowtype)){
            counterMap.put(propertyRowtype, new AtomicInteger(0));
        }
        AtomicInteger counterValue = counterMap.get(propertyRowtype);
        counterValue.addAndGet(1);
    }

    private String getRowtype(ACityElement aCityElement){

            if (aCityElement.getSourceNodeProperty(SAPNodeProperties.type_name) == SAPNodeTypes.TableType.name()){
                if (aCityElement.getSourceNodeProperty(SAPNodeProperties.rowtype) == null) {
                    return "TableType doesn't have a rowType";
                }

        }
        return aCityElement.getSourceNodeProperty(SAPNodeProperties.rowtype);
    }

    private void layoutParentDistricts(Collection<ACityElement> districtElements) {

        Collection<ACityElement> parentDistricts = getParentDistricts(districtElements);
        log.info(parentDistricts.size() + " parentDistrict loaded"); // first for buildings, then for typedistricts

        if (parentDistricts.isEmpty()){
            layoutVirtualRootDistrict(districtElements);
            return;
        }

        for(ACityElement parentDistrict : parentDistricts){
            layoutDistrict(parentDistrict);
        }

        layoutParentDistricts(parentDistricts);
    }


    private Collection<ACityElement> getParentDistricts(Collection<ACityElement> elements) {
        Map<String, ACityElement> parentDistricts = new HashMap<>();
        for(ACityElement element : elements){

            ACityElement parentElement = element.getParentElement();
            if(parentElement == null){
                continue;
            }

            String hash = parentElement.getHash();
            if(!parentDistricts.containsKey(hash)){
                parentDistricts.put(hash, parentElement);
            }
        }
        return parentDistricts.values();
    }

    private void layoutBuilding(ACityElement building) {
        Collection<ACityElement> floors = building.getSubElementsOfType(ACityElement.ACityType.Floor);

        Collection<ACityElement> chimneys = building.getSubElementsOfType(ACityElement.ACityType.Chimney);

        ACityBuildingLayout buildingLayout = new ACityBuildingLayout(building, floors, chimneys, config);
        buildingLayout.calculate();

        //TODO too much?
        if (floors.size() != 0) {
            log.info(building.getSourceNodeType() + " " + "\"" + building.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + " with " + floors.size() + " floors");
        }
        if (chimneys.size() != 0) {
            log.info(building.getSourceNodeType() + " " + "\"" + building.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + " with " + chimneys.size() + " chimneys");
        }
    }

    private void layoutDistrict(ACityElement district) {
        Collection<ACityElement> subElements = district.getSubElements();

        ACityDistrictLayout aCityDistrictLayout = new ACityDistrictLayout(district,  subElements, config);
        aCityDistrictLayout.calculate();

        if ( district.getSubType() != null) {
            log.info("\"" + district.getSubType() + "\"" + "-Distritct with " + subElements.size() + " buildings layouted");
        } else {
            log.info("\"" + district.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + "-Package with " + subElements.size() + " typeDistricts layouted");
        }
    }

    private void layoutVirtualRootDistrict(Collection<ACityElement> districts){
        AtomicInteger virtualRootDistrictCounter = new AtomicInteger(0);

        ACityElement virtualRootDistrict = new ACityElement(ACityElement.ACityType.District);

        virtualRootDistrictCounter.addAndGet(1);
        log.info(virtualRootDistrictCounter + " virtual root districts loaded");

        ACityDistrictLayout aCityDistrictLayout = new ACityDistrictLayout(virtualRootDistrict,  districts, config);
        aCityDistrictLayout.calculate();
    }

}
