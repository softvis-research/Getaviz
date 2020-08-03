package org.getaviz.generator.abap.city.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.*;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ACityCreator {



    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public ACityCreator(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("created");
    }


    public void createRepositoryFromNodeRepository(){

        log.info("Create City Elements");
        createAllACityElements(nodeRepository);

        log.info("Create City Relations");
        createAllACityRelations(nodeRepository);

        log.info("Create typeDistricts");
        createTypeDistrictRelations(nodeRepository);

        log.info("Delete empty Districts");
        deleteEmptyDistricts();

    }

    private void deleteEmptyDistricts() {

        Collection<ACityElement> districtsWithoutParents = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, "Namespace");

        for (ACityElement districtsWithoutParent : districtsWithoutParents) {

            if (districtsWithoutParent.getParentElement() == null) {

                Collection<ACityElement> districtWithoutParentAndSubElements = districtsWithoutParent.getSubElements();

                if (districtWithoutParentAndSubElements.isEmpty()) {

                    repository.deleteElement(districtsWithoutParent);

                    String districtName = districtsWithoutParent.getSourceNodeProperty(SAPNodeProperties.object_name);
                    log.info("district \"" + districtName + "\" deleted");
                }
            }
        }
    }


    private void createAllACityElements(SourceNodeRepository nodeRepository) {
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Namespace);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.Report);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Floor,SAPNodeProperties.type_name, SAPNodeTypes.FormRoutine);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.Class);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.Interface);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Floor,SAPNodeProperties.type_name, SAPNodeTypes.Method);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Chimney,SAPNodeProperties.type_name, SAPNodeTypes.Attribute);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.FunctionGroup);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Floor,SAPNodeProperties.type_name, SAPNodeTypes.FunctionModule);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.Table);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Floor,SAPNodeProperties.type_name, SAPNodeTypes.TableElement);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.TableType);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.Structure);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Floor,SAPNodeProperties.type_name, SAPNodeTypes.StructureElement);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.DataElement);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building,SAPNodeProperties.type_name, SAPNodeTypes.Domain);
    }



    private void createAllACityRelations(SourceNodeRepository nodeRepository) {
        createACityRelations(nodeRepository, ACityElement.ACityType.District);
        createACityRelations(nodeRepository, ACityElement.ACityType.Building);
    }

    private void createACityRelations(SourceNodeRepository nodeRepository, ACityElement.ACityType aCityType ){
        Collection<ACityElement> aCityElements = repository.getElementsByType(aCityType);
        log.info(aCityElements.size() + " ACityElement with type \"" + aCityType.name() + "\" loaded");

        long relationCounter = 0;
        for (ACityElement element: aCityElements) {

            Node sourceNode = element.getSourceNode();

            Collection<ACityElement> childElements = getChildElementsBySourceNode(nodeRepository, sourceNode);

                for (ACityElement childElement : childElements) {

                    //No nesting of packages/districts
                    if (childElement.getType() == ACityElement.ACityType.District) {
                        continue;
                    }

                    element.addSubElement(childElement);
                    childElement.setParentElement(element);
                    relationCounter++;
                }
            }

        log.info(relationCounter + " childRelations for relation \"CONTAINS\" created");
    }


    private void createTypeDistrictRelations(SourceNodeRepository nodeRepository) {

        Collection<ACityElement> districts = repository.getElementsByType(ACityElement.ACityType.District);
        log.info(districts.size() + "  districts loaded");

        for (ACityElement district: districts){
            Collection<ACityElement> subElements =  district.getSubElements();

            createTypeDistricts(district, subElements);

            removeSubElementsFromDistrict(district, subElements);

        }
    }

    private void createTypeDistricts(ACityElement parentDistrict, Collection<ACityElement> childElements) {
        Map<ACityElement.ACitySubType, ACityElement> typeDistrictMap = new HashMap<>();

        for (ACityElement childElement: childElements) {
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.Class, SAPNodeTypes.Class);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.Class, SAPNodeTypes.Interface);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.Report, SAPNodeTypes.Report);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.FunctionGroup, SAPNodeTypes.FunctionGroup);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.Table, SAPNodeTypes.Table);

            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.DDIC, SAPNodeTypes.Structure);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.DDIC, SAPNodeTypes.Domain);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.DDIC, SAPNodeTypes.DataElement);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, ACityElement.ACitySubType.DDIC, SAPNodeTypes.TableType);
        }

        String districtName = parentDistrict.getSourceNodeProperty(SAPNodeProperties.object_name);
        log.info(typeDistrictMap.size() + " typeDistricts created for district \"" + districtName + "\"");
    }

    private void addChildToTypeDistrict(ACityElement parentDistrict, ACityElement childElement, Map<ACityElement.ACitySubType, ACityElement> typeDistrictMap, ACityElement.ACitySubType districtType, SAPNodeTypes sapNodeTypes) {
        String typeNameProperty = childElement.getSourceNodeProperty(SAPNodeProperties.type_name);

        if( typeNameProperty.equals(sapNodeTypes.name())){

            if( !typeDistrictMap.containsKey(districtType)){
                createTypeDistrict(parentDistrict, typeDistrictMap, districtType);
            }
            ACityElement typeDistrict = typeDistrictMap.get(districtType);

            typeDistrict.addSubElement(childElement);
            childElement.setParentElement(typeDistrict);
        }

    }

    private void createTypeDistrict(ACityElement parentDistrict, Map<ACityElement.ACitySubType, ACityElement> typeDistrictMap, ACityElement.ACitySubType districtType) {

        ACityElement typeDistrict = new ACityElement(ACityElement.ACityType.District);
        typeDistrict.setSubType(districtType);

        typeDistrictMap.put(districtType, typeDistrict);
        repository.addElement(typeDistrict);

        parentDistrict.addSubElement(typeDistrict);
        typeDistrict.setParentElement(parentDistrict);
    }


    private void removeSubElementsFromDistrict(ACityElement district, Collection<ACityElement> subElements) {
        for (ACityElement subElement: subElements){
            if(subElement.getType() == ACityElement.ACityType.District){
                continue;
            }
            district.removeSubElement(subElement);
        }
    }








    private Collection<ACityElement> getChildElementsBySourceNode(SourceNodeRepository nodeRepository, Node node) {
        Collection<Node> childNodes = nodeRepository.getRelatedNodes(node, SAPRelationLabels.CONTAINS, true);
        if( childNodes.isEmpty()){
            return new TreeSet<>();
        }

        List<ACityElement> childElements = new ArrayList<>();
        for (Node childNode: childNodes ) {
            Long childNodeID = childNode.id();
            ACityElement childElement = repository.getElementBySourceID(childNodeID);
            childElements.add(childElement);
        }
        return childElements;
    }

    private ACityElement getParentElementBySourceNode(SourceNodeRepository nodeRepository, Node node) {
        Collection<Node> parentNodes = nodeRepository.getRelatedNodes(node, SAPRelationLabels.CONTAINS, false);
        if(parentNodes.isEmpty()) {
            return null;
        }

        Node parentNode = parentNodes.iterator().next();
        Long parentNodeId = parentNode.id();

        ACityElement parentElement = repository.getElementBySourceID(parentNodeId);
        return parentElement;
    }




    private void createACityElementsFromSourceNodes(SourceNodeRepository nodeRepository, ACityElement.ACityType aCityType, SAPNodeProperties property, SAPNodeTypes nodeType) {
        Collection<Node> sourceNodes = nodeRepository.getNodesByProperty(property, nodeType.name());

        log.info(sourceNodes.size() + " SourceNodes with property \"" + property + "\" and value \"" + nodeType.name() + "\" loaded");

        List<ACityElement> aCityElements = createACityElements(sourceNodes, aCityType);
        repository.addElements(aCityElements);

        log.info(aCityElements.size() + " ACityElements of type \"" + aCityType + "\" created");
    }

    private List<ACityElement> createACityElements(Collection<Node> sourceNodes, ACityElement.ACityType aCityType) {
        List<ACityElement> aCityElements = new ArrayList<>();

        for( Node sourceNode: sourceNodes ) {

            ACityElement aCityElement = new ACityElement(aCityType);
            aCityElement.setSourceNode(sourceNode);
            aCityElements.add(aCityElement);

        }

        return aCityElements;
    }

}
