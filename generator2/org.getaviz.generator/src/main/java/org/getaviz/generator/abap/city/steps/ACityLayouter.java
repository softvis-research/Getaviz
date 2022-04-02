package org.getaviz.generator.abap.city.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.layouts.ABuildingLayout;
import org.getaviz.generator.abap.layouts.ADistrictCircluarLayout;
import org.getaviz.generator.abap.layouts.ADistrictLightMapLayout;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

import java.util.*;

public class ACityLayouter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;


    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public ACityLayouter(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("*****************************************************************************************************************************************");
        log.info("created");

    }


    public void layoutRepository(){

        Collection<ACityElement> buildings = repository.getElementsByType(ACityElement.ACityType.Building);
        log.info(buildings.size() + " buildings loaded");

        for (ACityElement building: buildings) {

            SAPNodeTypes sourceNodeType = building.getSourceNodeType();

            if(sourceNodeType == SAPNodeTypes.TableType) {
                SAPNodeTypes buildingSourceType = getTableTypeTypeOfType(building);

                if(buildingSourceType != null){
                    layoutTableTypeBuilding(building, buildingSourceType);
                }
            } else {
                layoutBuilding(building);
            }
        }

        layoutParentDistricts(buildings);

      //  layoutReferenceBuildings();

    }

    private void layoutReferenceBuildings() {

        Collection<ACityElement> referenceElements = repository.getElementsByType(ACityElement.ACityType.Reference);
        log.info(referenceElements.size() + " buildings loaded");

        for (ACityElement referenceElement: referenceElements) {
            layoutReferenceBuilding(referenceElement);
        }

    }

    private void layoutReferenceBuilding(ACityElement referenceElement) {

        Collection<ACityElement> floors = referenceElement.getSubElementsOfType(ACityElement.ACityType.Floor);

        Collection<ACityElement> chimneys = referenceElement.getSubElementsOfType(ACityElement.ACityType.Chimney);

        ABuildingLayout buildingLayout = new ABuildingLayout(referenceElement, floors, chimneys, config);
        buildingLayout.calculate();

        if (floors.size() != 0) {
            log.info(referenceElement.getSourceNodeType() + " " + "\"" + referenceElement.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + " with " + floors.size() + " floors");
        }
        if (chimneys.size() != 0) {
            log.info(referenceElement.getSourceNodeType() + " " + "\"" + referenceElement.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + " with " + chimneys.size() + " chimneys");
        }

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

        for (Node typeOfNode: typeOfNodes) {
            Value propertyValue = typeOfNode.get(SAPNodeProperties.type_name.name());
            String typeOfNodeTypeProperty = propertyValue.asString();

            return SAPNodeTypes.valueOf(typeOfNodeTypeProperty);
        }

        return null;
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

    private String getRowtype(ACityElement aCityElement){

            if (aCityElement.getSourceNodeProperty(SAPNodeProperties.type_name) == SAPNodeTypes.TableType.name()){
                if (aCityElement.getSourceNodeProperty(SAPNodeProperties.rowtype) == null) {
                    return "TableType doesn't have a rowType";
                }

        }
        return aCityElement.getSourceNodeProperty(SAPNodeProperties.rowtype);
    }

    private void layoutParentDistricts(Collection<ACityElement> districtElements) {

        Collection<ACityElement> parentDistricts = getParentDistricts(districtElements); //districtElements = Buildings && parentDistricts = Districts
        log.info(parentDistricts.size() + " parentDistrict loaded"); // first for buildings, then for typedistricts

        for(ACityElement parentDistrict : parentDistricts){
            layoutDistrict(parentDistrict);
        }

        if (!parentDistricts.isEmpty()) {
            layoutParentDistricts(parentDistricts);
        } else {
            layoutVirtualRootDistrict();
        }
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

        ABuildingLayout buildingLayout = new ABuildingLayout(building, floors, chimneys, config);
        buildingLayout.calculate();

        if (floors.size() != 0) {
            log.info(building.getSourceNodeType() + " " + "\"" + building.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + " with " + floors.size() + " floors");
        }
        if (chimneys.size() != 0) {
            log.info(building.getSourceNodeType() + " " + "\"" + building.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + " with " + chimneys.size() + " chimneys");
        }
    }

    private void layoutDistrict(ACityElement district) {
        Collection<ACityElement> subElements = district.getSubElements();

        ADistrictLightMapLayout aCityDistrictLayout = new ADistrictLightMapLayout(district, subElements, config);
        aCityDistrictLayout.calculate();

        if (district.getSubType() != null) {
            log.info("\"" + district.getSubType() + "\"" + "-Distritct with " + subElements.size() + " buildings layouted");
        } else {
            log.info("\"" + district.getSourceNodeProperty(SAPNodeProperties.object_name) + "\"" + "-Package with " + subElements.size() + " typeDistricts layouted");
        }

    }

    private void layoutVirtualRootDistrict() {

        Collection<ACityElement> districtWithoutParents = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, "Namespace");

        for (ACityElement districtWithoutParent : districtWithoutParents) {

            if (districtWithoutParent.getParentElement() == null) {

                ACityElement virtualRootDistrict = new ACityElement(ACityElement.ACityType.District);

                ADistrictLightMapLayout aCityDistrictLayout = new ADistrictLightMapLayout(virtualRootDistrict, districtWithoutParents, config);
                aCityDistrictLayout.calculate();
            }
        }
    }
}
