package org.getaviz.generator.java.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeProperties;
import org.getaviz.generator.java.layouts.ABuildingLayout;
import org.getaviz.generator.layouts.ADistrictCircluarLayout;
import org.getaviz.generator.layouts.ADistrictLightMapLayout;
import org.getaviz.generator.layouts.AStackLayout;
import org.getaviz.generator.repository.ACityElement;
import org.getaviz.generator.repository.ACityRepository;
import org.getaviz.generator.repository.SourceNodeRepository;

import java.util.Collection;

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
        Collection<ACityElement> packageDistricts = repository.getRootDistricts(JavaNodeProperties.iteration);
        layoutDistrics(packageDistricts);
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

    private void layoutEmptyDistrict(ACityElement district) {
        district.setHeight(config.getMetropolisEmptyDistrictHeight());
        district.setLength(config.getMetropolisEmptyDistrictLength());
        district.setWidth(config.getMetropolisEmptyDistrictWidth());
    }

    private void layoutDistrics(Collection<ACityElement> districtElements) {
        log.info(districtElements.size() + " districts loaded");

        for (ACityElement districtElement : districtElements) {
            layoutDistrict(districtElement);
        }

        layoutVirtualRootDistrict(districtElements);
    }

    private void layoutVirtualRootDistrict(Collection<ACityElement> districts){
        log.info(districts.size() + " districts for virtual root district loaded");

        ACityElement virtualRootDistrict = new ACityElement(ACityElement.ACityType.District);

        if (config.getAbapNotInOrigin_layout() == SettingsConfiguration.NotInOriginLayout.DEFAULT) {

            ADistrictLightMapLayout aDistrictLightMapLayout = new ADistrictLightMapLayout(virtualRootDistrict, districts, config);
            aDistrictLightMapLayout.calculate();

        } else if (config.getAbapNotInOrigin_layout() == SettingsConfiguration.NotInOriginLayout.CIRCULAR) {

            ADistrictCircluarLayout aDistrictLayout = new ADistrictCircluarLayout(virtualRootDistrict, districts, config);
            aDistrictLayout.calculate();
        }

    }

    private void layoutBuilding(ACityElement building) {
        Collection<ACityElement> floors = building.getSubElementsOfType(ACityElement.ACityType.Floor);
        Collection<ACityElement> chimneys = building.getSubElementsOfType(ACityElement.ACityType.Chimney);

        ABuildingLayout buildingLayout = new ABuildingLayout(building, floors, chimneys, config);
        buildingLayout.calculate();

        if (floors.size() != 0) {
            log.info(building.getSourceNodeType() + " " + "\"" + building.getSourceNodeProperty(JavaNodeProperties.name) + "\"" + " with " + floors.size() + " floors");
        }
        if (chimneys.size() != 0) {
            log.info(building.getSourceNodeType() + " " + "\"" + building.getSourceNodeProperty(JavaNodeProperties.name) + "\"" + " with " + chimneys.size() + " chimneys");
        }
    }

    private void layoutDistrict(ACityElement district) {
        if (isDistrictEmpty(district)) {
            layoutEmptyDistrict(district);

            log.info("Empty district \"" + district.getSourceNodeProperty(JavaNodeProperties.name) + "\" layouted");
        } else {
            Collection<ACityElement> subElements = district.getSubElements();

            //layout sub-districts
            for (ACityElement subElement : subElements) {
                if (subElement.getType() == ACityElement.ACityType.District) {
                    layoutDistrict(subElement);
                }
            }

            //layout district
            ADistrictLightMapLayout aBAPDistrictLightMapLayout = new ADistrictLightMapLayout(district, subElements, config);
            aBAPDistrictLightMapLayout.calculate();

            //stack district sub elements
            AStackLayout stackLayout = new AStackLayout(district, subElements, config);
            stackLayout.calculate();

            log.info("\"" + district.getSourceNodeProperty(JavaNodeProperties.name) + "\"" + "-District with " + subElements.size() + " subElements layouted");
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
        Collection<ACityElement> subElements = district.getSubElements();
        boolean isEmpty = true;

        for (ACityElement subElement: subElements) {
            if(!subElement.getType().equals(ACityElement.ACityType.Reference)){
                isEmpty = false;
                break;
            }
        }

        return isEmpty;
    }
}