package org.getaviz.generator.java.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeProperties;
import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.generator.java.enums.JavaRelationLabels;
import org.getaviz.generator.repository.ACityElement;
import org.getaviz.generator.repository.ACityRepository;
import org.getaviz.generator.repository.SourceNodeRepository;
import org.neo4j.driver.v1.types.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

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

    private Collection<ACityElement> getDeclaresElementsBySourceNode(SourceNodeRepository nodeRepository, Node node) {
        Collection<Node> declaresNodes = nodeRepository.getRelatedNodes(node, JavaRelationLabels.DECLARES, true);

        if (declaresNodes.isEmpty()) {
            return new TreeSet<>();
        }

        List<ACityElement> declaresElements = new ArrayList<>();
        for (Node usesNode: declaresNodes) {
            Long usesNodeID = usesNode.id();
            ACityElement usesElement = repository.getElementBySourceID(usesNodeID);

            if (usesElement == null) {
                continue;
            }
            declaresElements.add(usesElement);
        }

        return declaresElements;
    }

    private void createAllMetropolisElements(SourceNodeRepository nodeRepository) {
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, JavaNodeProperties.type_name, JavaNodeTypes.Package);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, JavaNodeProperties.type_name, JavaNodeTypes.Class);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, JavaNodeProperties.type_name, JavaNodeTypes.Interface);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, JavaNodeProperties.type_name, JavaNodeTypes.Method);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, JavaNodeProperties.type_name, JavaNodeTypes.Field);
    }

    private void createReferenceBuildingRelations() {
        Collection<ACityElement> packageDistricts = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, JavaNodeProperties.type_name, JavaNodeTypes.Package.name());
        log.info(packageDistricts.size() + "  districts loaded");

        long mountainCounter = 0;
        long seaCounter = 0;
        long cloudCounter = 0;

        for (ACityElement packageDistrict: packageDistricts){
            // only for root folder (iteration = 0)
            String iterationString = packageDistrict.getSourceNodeProperty(JavaNodeProperties.iteration);
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
        long relationCounterDeclaresRelation = 0;

        for (ACityElement element: aCityElements) {
            Node sourceNode = element.getSourceNode();

            Collection<ACityElement> childElements = getChildElementsBySourceNode(nodeRepository, sourceNode);

            for (ACityElement childElement: childElements) {
                //No nesting of packages
//                if (childElement.getType() == ACityElement.ACityType.District && childElement.getSourceNodeType() == JavaNodeTypes.Package ) {
//                    continue;
//                }

                if (childElement.getType() == ACityElement.ACityType.Building && childElement.getSourceNodeType().equals(JavaNodeTypes.Interface.name())) {
                    continue;
                }

                element.addSubElement(childElement);
                childElement.setParentElement(element);
                relationCounter++;
            }

            // For DECLARES relation
            Node sourceNodeDistrict = element.getSourceNode();
            Collection<ACityElement> declaresElements = getDeclaresElementsBySourceNode(nodeRepository, sourceNodeDistrict);

            for (ACityElement declaresElement: declaresElements) {
                String type = declaresElement.getSourceNodeType();
                if (type.equals(JavaNodeTypes.Method.name()) || type.equals(JavaNodeTypes.Field.name())) {
                    String elementID = element.getSourceNodeProperty(JavaNodeProperties.element_id);
                    String declaresID = declaresElement.getSourceNodeProperty(JavaNodeProperties.declares_id);

                    if (elementID.equals(declaresID)) {
                        element.addSubElement(declaresElement);
                        declaresElement.setParentElement(element);
                        relationCounterDeclaresRelation++;
                    }
                } else {
                    repository.deleteElement(declaresElement);
                }
            }
        }

        log.info(relationCounter + " childRelations for relation \"CONTAINS\" created");
        log.info(relationCounterDeclaresRelation + " declaresRelations for relation \"DECLARES\" created");
    }

    private void deleteEmptyDistricts() {
        Collection<ACityElement> districtsWithoutParents = repository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, JavaNodeProperties.type_name, JavaNodeTypes.Package.name());

        for (ACityElement districtsWithoutParent : districtsWithoutParents) {

            if (districtsWithoutParent.getParentElement() == null) {

                Collection<ACityElement> districtWithoutParentAndSubElements = districtsWithoutParent.getSubElements();

                if (districtWithoutParentAndSubElements.isEmpty()) {

                    repository.deleteElement(districtsWithoutParent);

                    String districtName = districtsWithoutParent.getSourceNodeProperty(JavaNodeProperties.name);
                    log.info("district \"" + districtName + "\" deleted");
                }
            }
        }
    }

    private Collection<ACityElement> getChildElementsBySourceNode(SourceNodeRepository nodeRepository, Node node) {

        Collection<Node> childNodes = nodeRepository.getRelatedNodes(node, JavaRelationLabels.CONTAINS, true);
        if (childNodes.isEmpty()) {
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

    private void createACityElementsFromSourceNodes(SourceNodeRepository nodeRepository, ACityElement.ACityType aCityType, JavaNodeProperties property, JavaNodeTypes nodeType) {
        Collection<Node> sourceNodes = nodeRepository.getNodesByProperty(property, nodeType.name());

        log.info(sourceNodes.size() + " SourceNodes with type \"" + nodeType + "\" loaded");
        List<ACityElement> aCityElements = createACityElements(sourceNodes, aCityType, nodeType);
        repository.addElements(aCityElements);

        log.info(aCityElements.size() + " ACityElements of type \"" + aCityType + "\" created");
    }

    private List<ACityElement> createACityElements(Collection<Node> sourceNodes, ACityElement.ACityType aCityType, JavaNodeTypes sourceNodeType) {
        List<ACityElement> aCityElements = new ArrayList<>();

        for (Node sourceNode: sourceNodes) {
            ACityElement aCityElement = new ACityElement(aCityType);
            aCityElement.setSourceNode(sourceNode);
            aCityElements.add(aCityElement);
        }

        return aCityElements;
    }
}
