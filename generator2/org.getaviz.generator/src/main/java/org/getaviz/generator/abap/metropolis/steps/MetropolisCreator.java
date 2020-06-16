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
import org.neo4j.driver.v1.types.Node;

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

    }


    private void createAllMetropolisElements(SourceNodeRepository nodeRepository) {
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Namespace);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.FunctionGroup);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.FunctionModule);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Report);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Report);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.FormRoutine);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Class);
        //TODO
        //createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Interface);
        //createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Interface);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Method);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Attribute);





        //TODO
        /*
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Table);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Table);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Floor, SAPNodeProperties.type_name, SAPNodeTypes.TableElement);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.TableType);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.Structure);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.StructureElement);

        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.District, SAPNodeProperties.type_name, SAPNodeTypes.DataElement);
        //createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.DataElement);
        createACityElementsFromSourceNodes(nodeRepository, ACityElement.ACityType.Building, SAPNodeProperties.type_name, SAPNodeTypes.Domain);
         */
    }


    private void createAllMetropolisRelations(SourceNodeRepository nodeRepository) {
        createMetropolisRelations(nodeRepository, ACityElement.ACityType.District);
        createMetropolisRelations(nodeRepository, ACityElement.ACityType.Building);
    }

    private void createMetropolisRelations(SourceNodeRepository nodeRepository, ACityElement.ACityType aCityType ){
        Collection<ACityElement> aCityElements = repository.getElementsByType(aCityType);
        log.info(aCityElements.size() + " ACityElement with type \"" + aCityType.name() + "\" loaded");

        long relationCounter = 0;
        for (ACityElement element: aCityElements){

            Node sourceNode = element.getSourceNode();
            Collection<ACityElement> childElements = getChildElementsBySourceNode(nodeRepository, sourceNode);

            for (ACityElement childElement: childElements) {

                //No nesting of packages
                if (childElement.getType() == ACityElement.ACityType.District && childElement.getSourceNodeType() == SAPNodeTypes.Namespace ) {
                    continue;
                }

                element.addSubElement(childElement);
                childElement.setParentElement(element);
                relationCounter++;

            }
        }

        log.info(relationCounter + " childRelations for relation \"CONTAINS\" created");
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
