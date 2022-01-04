package org.getaviz.generator.abap.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.layouts.ADistrictLightMapLayout;
import org.getaviz.generator.abap.layouts.ABuildingLayout;
import org.getaviz.generator.abap.layouts.ADistrictCircluarLayout;
import org.getaviz.generator.abap.layouts.AStackLayout;
import org.getaviz.generator.abap.layouts.kdtree.ACityRectangle;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.neo4j.driver.v1.Value;
import org.getaviz.generator.city.kotlin.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class MetropolisLayouter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;


    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public MetropolisLayouter(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("*****************************************************************************************************************************************");
        log.info("created");
    }


    public void layoutRepository(){

        //layout buildings
        Collection<ACityElement> buildings = repository.getElementsByType(ACityElement.ACityType.Building);
        log.info(buildings.size() + " buildings loaded");
        layoutBuildings(buildings);

        //layout reference elements
        Collection<ACityElement> referenceElements = repository.getElementsByType(ACityElement.ACityType.Reference);
        log.info(referenceElements.size() + " reference elements loaded");
        layoutReferenceElements(referenceElements);

        //layout districts
        Collection<ACityElement> packageDistricts = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, "Namespace");
        layoutDistricts(packageDistricts);

        //layout cloud elements
        layoutCloudModel();
    }

    private void layoutReferenceElements(Collection<ACityElement> referenceElements) {
        for (ACityElement referenceElement: referenceElements) {
            layoutReference(referenceElement);
        }
    }

    private void layoutBuildings(Collection<ACityElement> buildings) {
        for (ACityElement building: buildings) {
           layoutBuilding(building);
        }
    }

    private void layoutEmptyDistrict( ACityElement district) {
        district.setHeight(config.getMetropolisEmptyDistrictHeight());
        district.setLength(config.getMetropolisEmptyDistrictLength());
        district.setWidth(config.getMetropolisEmptyDistrictWidth());
    }

    private void layoutDistricts(Collection<ACityElement> districtElements) {
        log.info(districtElements.size() + " districts loaded");

        layoutDistrictsVertically(districtElements);
        layoutDistrictsHorizontally(districtElements);
    }

    private void layoutDistrictsVertically(Collection<ACityElement> districtElements) {
        for (ACityElement district : districtElements) {
            setDistrictHeights(district);
        }
        // calculate the vertical displacement of elements
        for (ACityElement district : districtElements) {
            AStackLayout stackLayout = new AStackLayout(district, district.getSubElements(), config);
            stackLayout.calculate();
        }
    }

    private void setDistrictHeights(ACityElement district) {
        district.setHeight(config.getACityDistrictHeight());
        district.setYPosition(district.getHeight() / 2);
        for (ACityElement child : district.getSubElements()) {
            if (child.getType() == ACityElement.ACityType.District) {
                setDistrictHeights(child);
            }
        }
    }

    private void layoutDistrictsHorizontally(Collection<ACityElement> districtElements) {
        // split root districts into "core" and "peripheral" sets
        Map<Boolean, List<ACityElement>> mapNamespaceIsInternal = districtElements.stream().collect(
                Collectors.partitioningBy(
                        e -> (!e.getSourceNodeProperty(SAPNodeProperties.creator).equals("SAP")
                                && e.getSourceNodeProperty(SAPNodeProperties.iteration).equals("0"))));
        List<ACityElement> internalDistricts = mapNamespaceIsInternal.get(true);
        List<ACityElement> customOrStandardDistricts = mapNamespaceIsInternal.get(false);

        List<Node> coreLayouterNodes = mapToLayouterNodes(internalDistricts);
        List<Node> peripheralLayouterNodes = mapToLayouterNodes(customOrStandardDistricts);

        // arrange core nodes (and their children) within a virtual root district
        LightMapLayouter lightMapLayouter = new LightMapLayouter(config.getBuildingHorizontalGap());
        CityRectangle kotlinCoreCovrec = lightMapLayouter.calculateWithVirtualRoot(coreLayouterNodes);
        transferPositionData(coreLayouterNodes);

        // arrange the children of each peripheral node
        for (Node node : peripheralLayouterNodes) {
            lightMapLayouter.calculate(node);
        }
        transferPositionData(peripheralLayouterNodes);

        // arrange the peripheral root nodes along a circular arc
        ACityRectangle coreCovrec = new ACityRectangle(
                kotlinCoreCovrec.getX(), kotlinCoreCovrec.getY(),
                kotlinCoreCovrec.getMaxX(), kotlinCoreCovrec.getMaxY());
        ACityElement circularRootDistrict = new ACityElement(ACityElement.ACityType.District);
        ADistrictCircluarLayout circularLayout = new ADistrictCircluarLayout(circularRootDistrict, customOrStandardDistricts, config, coreCovrec);
        circularLayout.calculate();
    }

    private List<Node> mapToLayouterNodes(Collection<ACityElement> elements) {
        return elements.stream().map(element -> new Node(
                element.getHash(),
                element.getXPosition(),
                element.getZPosition(),
                element.getWidth(),
                element.getLength(),
                element.getSubElements().isEmpty() ? new ArrayList<>() : mapToLayouterNodes(element.getSubElements())
        )).collect(Collectors.toList());
    }

    private void transferPositionData(Collection<Node> nodes) {
        for (Node node : nodes) {
            ACityElement correspondingElement = repository.getElementByHash(node.getId());
            // Node uses 2D coordinates, ACityElement uses 3D, hence y -> z
            correspondingElement.setXPosition(node.getCenterX());
            correspondingElement.setZPosition(node.getCenterY());
            correspondingElement.setWidth(node.getWidth());
            correspondingElement.setLength(node.getLength());

            if (!node.getChildren().isEmpty()) {
                transferPositionData(node.getChildren());
            }
        }
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

    private void layoutCloudModel() {

        Collection<ACityElement> districtsWithFindings = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.migration_findings, "true");
        //Collection<ACityElement> buildingsWithFindings = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.Building, SAPNodeProperties.migration_findings, "true");

        /*for (ACityElement buildingsWithFinding: buildingsWithFindings) {

            Collection<ACityElement> cloudSubElements = buildingsWithFinding.getSubElements();

            for (ACityElement cloudSubElement : cloudSubElements) {

                if (cloudSubElement.getType().equals(ACityElement.ACityType.Reference) &&
                        cloudSubElement.getSubType().equals(ACityElement.ACitySubType.Cloud)) {

                    cloudSubElement.setWidth(0);
                    cloudSubElement.setLength(0);
                    cloudSubElement.setYPosition(55);

                    ACityElement parent = cloudSubElement.getParentElement();

                    double parentDistrictXPosition = parent.getParentElement().getXPosition();
                    double parentDistrictZPosition = parent.getParentElement().getZPosition();

                    cloudSubElement.setXPosition(parentDistrictXPosition);
                    cloudSubElement.setZPosition(parentDistrictZPosition);

                    cloudSubElement.setWidth(0);
                    cloudSubElement.setLength(0);
                }
            }
        }*/

        for (ACityElement districtWithFinding: districtsWithFindings) {

            Collection<ACityElement> cloudSubElements = districtWithFinding.getSubElements();

            for (ACityElement cloudSubElement : cloudSubElements) {

                if (cloudSubElement.getType().equals(ACityElement.ACityType.Reference) &&
                        cloudSubElement.getSubType().equals(ACityElement.ACitySubType.Cloud)) {

                    cloudSubElement.setWidth(0);
                    cloudSubElement.setLength(0);
                    cloudSubElement.setYPosition(55);

                    double parentDistrictXPosition = cloudSubElement.getParentElement().getXPosition();
                    double parentDistrictZPosition = cloudSubElement.getParentElement().getZPosition();

                    cloudSubElement.setXPosition(parentDistrictXPosition);
                    cloudSubElement.setZPosition(parentDistrictZPosition);

                    cloudSubElement.setWidth(0);
                    cloudSubElement.setLength(0);
                }
            }
        }
    }

    private void layoutReference(ACityElement referenceElement) {
        ACityElement.ACitySubType referenceBuildingType = referenceElement.getSubType();

            switch (referenceBuildingType) {
                case Sea:
                    referenceElement.setHeight(config.getMetropolisReferenceBuildingHeigth("seaReferenceBuilding"));
                    referenceElement.setWidth(config.getMetropolisReferenceBuildingWidth("seaReferenceBuilding"));
                    referenceElement.setLength(config.getMetropolisReferenceBuildingLength("seaReferenceBuilding"));
                    referenceElement.setYPosition(referenceElement.getHeight() / 2);
                    break;

                case Mountain:
                    referenceElement.setHeight(config.getMetropolisReferenceBuildingHeigth("mountainReferenceBuilding"));
                    referenceElement.setWidth(config.getMetropolisReferenceBuildingWidth("mountainReferenceBuilding"));
                    referenceElement.setLength(config.getMetropolisReferenceBuildingLength("mountainReferenceBuilding"));
                    break;

                case Cloud:
                    referenceElement.setHeight(config.getMetropolisReferenceBuildingHeigth("cloudReferenceBuilding"));
                    referenceElement.setWidth(config.getMetropolisReferenceBuildingWidth("cloudReferenceBuilding"));
                    referenceElement.setLength(config.getMetropolisReferenceBuildingLength("cloudReferenceBuilding"));
                    break;
            }
    }


    private boolean isDistrictEmpty(ACityElement district){
        return district.getSubElements().stream().anyMatch(
                element -> !element.getType().equals(ACityElement.ACityType.Reference)
        );
    }

}