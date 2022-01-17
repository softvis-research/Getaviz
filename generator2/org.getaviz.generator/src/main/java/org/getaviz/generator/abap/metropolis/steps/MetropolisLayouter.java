package org.getaviz.generator.abap.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.layouts.ABuildingLayout;
import org.getaviz.generator.abap.layouts.ADistrictCircluarLayout;
import org.getaviz.generator.abap.layouts.AStackLayout;
import org.getaviz.generator.abap.layouts.kdtree.ACityRectangle;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.city.kotlin.*;

import java.util.*;
import java.util.stream.Collectors;

public class MetropolisLayouter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public enum MetropolisSection {
        Origin, Custom, Standard
    }

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
        for (ACityElement referenceElement : referenceElements) {
            layoutReference(referenceElement);
        }
    }

    private void layoutBuildings(Collection<ACityElement> buildings) {
        for (ACityElement building : buildings) {
           layoutBuilding(building);
        }
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
        // split root districts into sections that are laid out separately
        Map<MetropolisSection, List<ACityElement>> sectionMap = districtElements.stream()
                .collect(Collectors.groupingBy(this::getMetropolisSection));

        LightMapLayouterConfig layouterConfig = new LightMapLayouterConfig();
        layouterConfig.setBuildingHorizontalGap(config.getBuildingHorizontalGap());
        layouterConfig.setEmptyDistrictSize(config.getMetropolisEmptyDistrictWidth());

        // arrange origin nodes (and their children)
        LightMapLayouter lightMapLayouter = new LightMapLayouter(layouterConfig);
        ACityRectangle covrec = layoutAllWithLightMap(lightMapLayouter, sectionMap.get(MetropolisSection.Origin));

        // arrange the sub-elements of the peripheral districts
        List<ACityElement> customDistricts = sectionMap.get(MetropolisSection.Custom);
        List<ACityElement> standardDistricts = sectionMap.get(MetropolisSection.Standard);
        layoutChildrenWithLightMap(lightMapLayouter, customDistricts);
        layoutChildrenWithLightMap(lightMapLayouter, standardDistricts);

        // arrange the custom root nodes in a first arc (will expand covrec)
        ACityElement customRootDistrict = new ACityElement(ACityElement.ACityType.District);
        ADistrictCircluarLayout circularLayout = new ADistrictCircluarLayout(customRootDistrict, customDistricts, config, covrec);
        circularLayout.calculate();

        // arrange the standard root nodes in a second arc
        ACityElement standardRootDistrict = new ACityElement(ACityElement.ACityType.District);
        circularLayout = new ADistrictCircluarLayout(standardRootDistrict, standardDistricts, config, covrec);
        circularLayout.calculate();
    }

    private MetropolisSection getMetropolisSection(ACityElement element) {
        if (element.getSourceNodeProperty(SAPNodeProperties.creator).equals("SAP")) {
            return MetropolisSection.Standard;
        } else if (element.getSourceNodeProperty(SAPNodeProperties.iteration).equals("0")) {
            return MetropolisSection.Origin;
        } else {
            return MetropolisSection.Custom;
        }
    }

    private ACityRectangle layoutAllWithLightMap(LightMapLayouter layouter, Collection<ACityElement> elements) {
        List<Node> layouterNodes = mapToLayouterNodes(elements);
        CityRectangle covrec = layouter.calculateWithVirtualRoot(layouterNodes);
        transferPositionData(layouterNodes);

        return new ACityRectangle(covrec.getX(), covrec.getY(), covrec.getMaxX(), covrec.getMaxY());
    }

    private void layoutChildrenWithLightMap(LightMapLayouter layouter, Collection<ACityElement> elements) {
        List<Node> layouterNodes = mapToLayouterNodes(elements);
        for (Node node : layouterNodes) {
            layouter.calculate(node);
        }
        transferPositionData(layouterNodes);
    }

    private List<Node> mapToLayouterNodes(Collection<ACityElement> elements) {
        // get rid of all cloud reference objects during the transformation: they are laid out separately and would get in the way
        return elements.stream()
            .filter(element -> element.getSubType() != ACityElement.ACitySubType.Cloud)
            .map(element -> {
                // fall back on type name for e.g. reference buildings
                String nodeName = element.getSourceNode() != null
                        ? element.getSourceNodeProperty(SAPNodeProperties.object_name) : element.getSubType().name();

                return new Node(
                        element.getHash(),
                        nodeName,
                        element.getXPosition(),
                        element.getZPosition(),
                        element.getWidth(),
                        element.getLength(),
                        element.getSubElements().isEmpty() ? new ArrayList<>() : mapToLayouterNodes(element.getSubElements()),
                        element.getType() == ACityElement.ACityType.District
                );
            }).collect(Collectors.toList());
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

        for (ACityElement districtWithFinding : districtsWithFindings) {

            Collection<ACityElement> cloudSubElements = districtWithFinding.getSubElements();

            for (ACityElement cloudSubElement : cloudSubElements) {

                if (cloudSubElement.getType().equals(ACityElement.ACityType.Reference) &&
                        cloudSubElement.getSubType().equals(ACityElement.ACitySubType.Cloud)) {

                    double parentDistrictXPosition = cloudSubElement.getParentElement().getXPosition();
                    double parentDistrictZPosition = cloudSubElement.getParentElement().getZPosition();

                    cloudSubElement.setXPosition(parentDistrictXPosition);
                    cloudSubElement.setYPosition(55);
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

}