package org.getaviz.generator.abap.repository;

import java.util.Collection;

public interface IACityRelationMapper {
	
	public Collection<ACityElement> getRelatedACityElements(ACityElement element, boolean reverse);
	
	public Collection<ACityElement> getAggregatedRelatedACityElements(ACityElement element, RelationAggregationLevel aggregationLevel, boolean reverse);
	
	public int getAmountOfRelatedACityElements(ACityElement element, boolean reverse);
	
	public int getAmountOfRelationsToACityElement(ACityElement source, ACityElement target, boolean reverse);
	
	public enum RelationAggregationLevel {
		 BUILDING,
		 SOURCE_CODE_DISTRICT,
		 PACKAGE_DISTRICT
	}
	
}
