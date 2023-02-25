package org.getaviz.generator.abap.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.neo4j.driver.types.Node;

import java.text.CollationElementIterator;
import java.util.*;

public class MetropolisCreator {


    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

    public MetropolisCreator(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
        nodeRepository = sourceNodeRepository;

        log.info("*****************************************************************************************************************************************");
        log.info("created");
    }


    public void createRepositoryFromNodeRepository(){

        log.info("Create City Elements");
        createAllMetropolisElements(nodeRepository);

        log.info("Create City Relations");
        createAllMetropolisRelations(nodeRepository);

        log.info("Create ReferenceBuildings");
        createReferenceBuildingRelations();

        log.info("Delete empty Districts");
        deleteEmptyDistricts();

    }

    private Collection<ACityElement> getUsesElementsBySourceNode(SourceNodeRepository nodeRepository, Node node) {
        Collection<Node> usesNodes = nodeRepository.getRelatedNodes(node, SAPRelationLabels.USES, true);
        if( usesNodes.isEmpty()){
            return new TreeSet<>();
        }

        List<ACityElement> usesElements = new ArrayList<>();
        for (Node usesNode: usesNodes ) {
            Long usesNodeID = usesNode.id();
            ACityElement usesElement = repository.getElementBySourceID(usesNodeID);
            if(usesElement == null){
                continue;
            }
            usesElements.add(usesElement);
        }
        return usesElements;

    }

    private void createAllMetropolisElements(SourceNodeRepository nodeRepository) {
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Namespace);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.FunctionGroup);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.FunctionModule);
        //createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.FunctionModule);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Report);
        //createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Report);
       // createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.FormRoutine);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Class);

       // createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Interface);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Method);
        //createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Method);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Attribute);
    }

    private void createReferenceBuildingRelations() {

        Collection<ACityElement> packageDistricts = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Namespace.toString());
        log.info(packageDistricts.size() + "  districts loaded");

        long mountainCounter = 0;
        long seaCounter = 0;
        long cloudCounter = 0;

        for (ACityElement packageDistrict: packageDistricts){

            // nur für Hauptpaket (Iteration 0)
            String iterationString = packageDistrict.getSourceNodeProperty(SAPNodeProperties.iteration);
            int iterationInt = Integer.parseInt(iterationString);

            Collection<ACityElement> subElements = packageDistrict.getSubElements();

            if(iterationInt == 0) {

                if (!subElements.isEmpty()) {

                    if (config.showMountainReferenceBuilding()) {
                        createRefBuilding(packageDistrict, ACityElement.ACitySubType.Mountain);
                        mountainCounter++;
                    }

                    if (config.showSeaReferenceBuilding()) {
                        createRefBuilding(packageDistrict, ACityElement.ACitySubType.Sea);
                        seaCounter++;
                    }
                }
            }

            if (config.showCloudReferenceBuilding()) {
                for (ACityElement subElement : subElements) { //SubElements = Class/Repo/FuGr-District

                    if (subElement.getType().equals(ACityElement.ACityType.District)) {
                        String migrationFindingsString = subElement.getSourceNodeProperty(SAPNodeProperties.migration_findings);
                        if (migrationFindingsString.equals("true")) {
                            createRefBuilding(subElement, ACityElement.ACitySubType.Cloud);
                            cloudCounter++;

                        }
                    }
                }
            }
        }

        log.info(mountainCounter + " refBuildings of type mountain created");
        log.info(seaCounter + " refBuildings of type sea created");
        log.info(cloudCounter + " refBuildings of type cloud created");
    }


    private ACityElement createRefBuilding(ACityElement packageDistrict, ACityElement.ACitySubType refBuildingType) {
        ACityElement refBuilding = new ACityElement(ACityElement.ACityType.Reference);
        refBuilding.setSubType(refBuildingType);

        repository.addElement(refBuilding);

        packageDistrict.addSubElement(refBuilding);
        refBuilding.setParentElement(packageDistrict);

        return refBuilding;
    }

    private void createAllMetropolisRelations(SourceNodeRepository nodeRepository) {
        createMetropolisRelations(nodeRepository, ACityElement.ACityType.District);
        createMetropolisRelations(nodeRepository, ACityElement.ACityType.Building);
    }

    private void createMetropolisRelations(SourceNodeRepository nodeRepository, ACityElement.ACityType aCityType ){
        Collection<ACityElement> aCityElements = repository.getElementsByType(aCityType);
        log.info(aCityElements.size() + " ACityElement with type \"" + aCityType.name() + "\" loaded");

        long relationCounter = 0;
        long relationCounterUsesRelation = 0;
        for (ACityElement element: aCityElements){

            Node sourceNode = element.getSourceNode();

            if(element.getSourceNodeType() == SAPNodeTypes.Report) {
                if(element.getType() == ACityElement.ACityType.Building){
                    continue;
                }

                createMetropolisRelationsForIdenticalNodes(nodeRepository, sourceNode, element);
                relationCounter++;
            }

            Collection<ACityElement> childElements = getChildElementsBySourceNode(nodeRepository, sourceNode);

            for (ACityElement childElement: childElements) {

                //No nesting of packages
                if (childElement.getType() == ACityElement.ACityType.District && childElement.getSourceNodeType() == SAPNodeTypes.Namespace ) {
                    continue;
                }
//DEbugger
                if (childElement.getType() == ACityElement.ACityType.Building && childElement.getSourceNodeType() == SAPNodeTypes.Report) {
                    continue;
                }

                if (childElement.getType() == ACityElement.ACityType.Building && childElement.getSourceNodeType() == SAPNodeTypes.Interface) {
                    continue;
                }

                element.addSubElement(childElement);
                childElement.setParentElement(element);
                relationCounter++;

            }

            // for uses-relation
            Node sourceNodeDistrict = element.getSourceNode();
            Collection<ACityElement> usesElements = getUsesElementsBySourceNode(nodeRepository, sourceNodeDistrict);

            for(ACityElement usesElement: usesElements) {

                if (usesElement.getSourceNodeProperty(SAPNodeProperties.local_class).equals("true")) {

                    String elementID = element.getSourceNodeProperty(SAPNodeProperties.element_id);
                    String usesID = usesElement.getSourceNodeProperty(SAPNodeProperties.uses_id);

                    if (elementID.equals(usesID)) {
                        element.addSubElement(usesElement);
                        usesElement.setParentElement(element);
                        relationCounterUsesRelation++;
                    }
                } else if (usesElement.getSourceNodeType() == SAPNodeTypes.Attribute){

                    String elementID = element.getSourceNodeProperty(SAPNodeProperties.element_id);
                    String usesID = usesElement.getSourceNodeProperty(SAPNodeProperties.uses_id);

                    if(element.getSourceNodeType() == SAPNodeTypes.Report){

                        if (elementID.equals(usesID)) {
                            element.addSubElement(usesElement);
                            usesElement.setParentElement(element);
                            relationCounterUsesRelation++;
                        }
                    }
                } else {
                    repository.deleteElement(usesElement); //atm only for local classes, attributes are deleted
                }
            }
        }

        log.info(relationCounter + " childRelations for relation \"CONTAINS\" created");
        log.info(relationCounterUsesRelation + " usesRelations for relation \"USES\" created");

    }

    private void createMetropolisRelationsForIdenticalNodes(SourceNodeRepository nodeRepository, Node sourceNode, ACityElement element) {

        ACityElement buildingParentElements = getParentElementBySourceNode(nodeRepository, sourceNode);

        element.setParentElement(buildingParentElements);
        buildingParentElements.addSubElement(element);

        String buildingElementTypeName = element.getSourceNodeType().name();

        Collection<ACityElement> BuildingElements = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.Building, SAPNodeProperties.type_name, buildingElementTypeName);
        for (ACityElement buildingElement: BuildingElements) {

            String districtTypename = element.getSourceNodeProperty(SAPNodeProperties.element_id);
            String buildingTypeName = buildingElement.getSourceNodeProperty(SAPNodeProperties.element_id);

            if(buildingTypeName.equals(districtTypename)){

                element.addSubElement(buildingElement);
                buildingElement.setParentElement(element);

            }
        }
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
            if(childElement == null){
                continue;
            }
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
