package org.getaviz.generator.abap.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.codec.language.bm.NameType;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.repository.ACityElement.ACitySubType;
import org.getaviz.generator.abap.repository.ACityElement.ACityType;
import org.neo4j.driver.v1.types.Node;

public class ACityReferenceMapper implements IACityRelationMapper {
	
	private SourceNodeRepository nodeRepository;
	private ACityRepository repository;
	
	public ACityReferenceMapper(SourceNodeRepository nodeRepository, ACityRepository repository) {
		this.nodeRepository = nodeRepository;
		this.repository = repository;
	}

	private List<ACityElement> getReferencedACityElements(ACityElement building, boolean reverse) {
		if (building.getSourceNode() == null) {
			return null;
		}
		
		List<ACityElement> referencedElements = new ArrayList<ACityElement>();
		
		Collection<Node> referencedNodes = this.nodeRepository.getRelatedNodes(building.getSourceNode(), SAPRelationLabels.REFERENCES, !reverse);
		
		// map source nodes to concrete acity elements
		for (Node referencedNode : referencedNodes) {
			ACityElement correspondingElement = this.repository.getElementBySourceID(referencedNode.id());
			referencedElements.add(correspondingElement);
		}
		
		return referencedElements;
	}
	
	@Override
	public Collection<ACityElement> getRelatedACityElements(ACityElement element, boolean reverse) {
		
		// eliminate duplicates				
		return new HashSet<ACityElement>(this.getRelatedACityElementsWithDuplicates(element, reverse));
	}
	
	private List<ACityElement> getRelatedACityElementsWithDuplicates(ACityElement element, boolean reverse) {
		if (element.getSourceNode() == null) {
			return null;
		}
		
		List<ACityElement> referencedElements = new ArrayList<ACityElement>();
		
		if (element.getSourceNodeType() == SAPNodeTypes.Namespace) {
			for (ACityElement packageChild : element.getSubElements()) {
				if (packageChild.getType() == ACityType.Reference) {
					continue;
				}
				
				referencedElements.addAll(this.getRelatedACityElementsWithDuplicates(packageChild, reverse));
			}			
			return referencedElements;
		}
		
		switch (element.getSourceNodeType()) {
		case Report:
						
			referencedElements.addAll(this.getReferencedACityElements(element, reverse));
			
			// special case: only get relations of report itself, not of his sub objects
			if (element.getType() == ACityType.Building) {
				return referencedElements;
			}
		
		case FunctionGroup:
		case Class:
			
			// get related entities for local classes
			for (ACityElement localClass : element.getSubElementsOfSourceNodeType(SAPNodeTypes.Class)) {
				referencedElements.addAll(this.getRelatedACityElementsWithDuplicates(localClass, reverse));
			}
			
		case Interface:
			
			switch (element.getSourceNodeType()) {
			case Class:
			case Interface:
				for (ACityElement method : element.getSubElementsOfSourceNodeType(SAPNodeTypes.Method)) {
					referencedElements.addAll(this.getRelatedACityElementsWithDuplicates(method, reverse));
				}
				break;

			case FunctionGroup:
				for (ACityElement functionModule : element.getSubElementsOfSourceNodeType(SAPNodeTypes.FunctionModule)) {
					referencedElements.addAll(this.getRelatedACityElementsWithDuplicates(functionModule, reverse));
				}
				// no break because both function groups and reports may contain formroutines
			case Report:
				for (ACityElement formRoutine : element.getSubElementsOfSourceNodeType(SAPNodeTypes.FormRoutine)) {
					referencedElements.addAll(this.getRelatedACityElementsWithDuplicates(formRoutine, reverse));
				}
				
			default:
				break;
			}
			
			break;
			
		
		case Method:
		case FunctionModule:
		case FormRoutine:
	
			// they don't contain any further sub elements
			return this.getReferencedACityElements(element, reverse);

		default:
			break;
		}
				
		return referencedElements;
	}

	@Override
	public Collection<ACityElement> getAggregatedRelatedACityElements(ACityElement element, RelationAggregationLevel aggregationLevel, boolean reverse) {
		Collection<ACityElement> aggregatedReferencedACityElements = new HashSet<ACityElement>();
		
		for (ACityElement referencedElement : this.getRelatedACityElementsWithDuplicates(element, reverse)) {
			aggregatedReferencedACityElements.add(this.getAggregatedObject(referencedElement, aggregationLevel));
		}
				
		return aggregatedReferencedACityElements;
	}

	@Override
	public int getAmountOfRelatedACityElements(ACityElement element, boolean reverse) {
		return this.getRelatedACityElements(element, reverse).size();
	}

	@Override
	public int getAmountOfRelationsToACityElement(ACityElement source, ACityElement target, boolean reverse) {
		
		int amountOfRelations = 0;
		RelationAggregationLevel targetAggregationLevel = this.mapToAggregationLevel(target);
		
		List<ACityElement> referencedElements = this.getRelatedACityElementsWithDuplicates(source, reverse);
		
		for (ACityElement referencedElement : referencedElements) {
			if (target == this.getAggregatedObject(referencedElement, targetAggregationLevel)) {
				amountOfRelations++;
			}
		}
		
		return amountOfRelations;
	}
	
	public RelationAggregationLevel mapToAggregationLevel(ACityElement element) {
		switch (element.getType()) {
		case Building:
		case Reference:
			return RelationAggregationLevel.BUILDING;
			
		case District:
			
			switch (element.getSourceNodeType()) {
			case Namespace:
				return RelationAggregationLevel.PACKAGE_DISTRICT;
				
			case FunctionGroup:
			case Report:
				return RelationAggregationLevel.SOURCE_CODE_DISTRICT;
				
			case Class:
			case Interface:
				// treat local classes and local interfaces as buildings
				if (element.getParentElement().getSourceNodeType() == SAPNodeTypes.Class 
						|| element.getParentElement().getSourceNodeType() == SAPNodeTypes.FunctionGroup
						|| element.getParentElement().getSourceNodeType() == SAPNodeTypes.Report) {
					return RelationAggregationLevel.BUILDING;
				} else {
					return RelationAggregationLevel.SOURCE_CODE_DISTRICT;
				}
				
			default:
				return null;
			}
			
		default:
			return null;
		}
	}
	
	private ACityElement getAggregatedObject(ACityElement element, RelationAggregationLevel aggregationLevel) {
		if (this.mapToAggregationLevel(element) == aggregationLevel) {
			return element;
		}
		
		if (element.getParentElement() == null) {
			return null;
		}
				
		return getAggregatedObject(element.getParentElement(), aggregationLevel);
	}

}
