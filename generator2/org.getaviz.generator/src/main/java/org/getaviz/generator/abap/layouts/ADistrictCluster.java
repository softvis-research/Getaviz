package org.getaviz.generator.abap.layouts;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityReferenceMapper;

public class ADistrictCluster {

	private ACityReferenceMapper refMapper;	
	private List<List<ACityElement>> clusterList;	
	private Map<ACityElement, SortedSet<Entry<ACityElement, Integer>>> referenceMatrix;
	
	private int referenceThreshold;
	
	public ADistrictCluster(ACityReferenceMapper refMapper, Collection<ACityElement> districts, int referenceThreshold) {
		this.refMapper = refMapper;
		this.clusterList = new ArrayList<List<ACityElement>>();
		this.referenceThreshold = referenceThreshold;
		this.referenceMatrix = new HashMap<ACityElement, SortedSet<Entry<ACityElement,Integer>>>();
		
		initializeReferenceMatrix(districts);
	}
	
	public List<List<ACityElement>> getClusters() {
		return clusterList;
	}
	
	public void calculateClusters() {
		
		while (true) {
			ACityElement[] nearestNeighbours = getNearestNeighbours();
			if (nearestNeighbours[0] == null) {
				break;
			}
			
			List<ACityElement> modifiedCluster = addToCluster(nearestNeighbours[0], nearestNeighbours[1]);
			ACityElement newlyAddedElement = modifiedCluster.get(modifiedCluster.size() - 1);
			
			// remove all distances from newly added cluster element to the previous cluster elements and vice versa
			for (Entry<ACityElement, Integer> entry : referenceMatrix.get(newlyAddedElement)) {
				if (modifiedCluster.contains(entry.getKey())) {
					
					referenceMatrix.get(newlyAddedElement).remove(entry);
					
					for (Entry<ACityElement, Integer> otherEntry : referenceMatrix.get(entry.getKey())) {
						if (otherEntry.getKey() == newlyAddedElement) {
							referenceMatrix.get(entry.getKey()).remove(otherEntry);
							break;
						}
					}
				}
			}			
		}
		
		int refMatrixCardinality = referenceMatrix.keySet().size();
		List<ACityElement> isolationCluster = new ArrayList<ACityElement>();
		
		// add all elements which are not in any clusters to the isolation cluster
		for (Entry<ACityElement, SortedSet<Entry<ACityElement, Integer>>> refMatrixLine : referenceMatrix.entrySet()) {
			if (refMatrixLine.getValue().size() == refMatrixCardinality - 1) {
				isolationCluster.add(refMatrixLine.getKey());
			}
		}
		
		clusterList.add(isolationCluster);		
	}
	
	private void initializeReferenceMatrix(Collection<ACityElement> districts) {		
    	for (ACityElement district : districts) {
			referenceMatrix.putIfAbsent(district, new ConcurrentSkipListSet<Map.Entry<ACityElement, Integer>>((Entry<ACityElement, Integer> e1, Entry<ACityElement, Integer> e2) -> {
	        	if (e1.getValue().compareTo(e2.getValue()) == 0) {
	        		ACityElement elem1 = e1.getKey();
	        		ACityElement elem2 = e2.getKey();
	        		
					return Integer.compare(Integer.parseInt(elem1.getSourceNodeProperty(SAPNodeProperties.element_id)), Integer.parseInt(elem2.getSourceNodeProperty(SAPNodeProperties.element_id)));
				} else {
					return -1 * e1.getValue().compareTo(e2.getValue());
				}
			}));
			
			for (ACityElement otherDistrict : districts) {
				if (district == otherDistrict) {
					continue;
				}
				
				if (referenceMatrix.containsKey(otherDistrict)) {
					for (Entry<ACityElement, Integer> entry : referenceMatrix.get(otherDistrict)) {
						if (entry.getKey() == district) {
							referenceMatrix.get(district).add(new AbstractMap.SimpleEntry<ACityElement, Integer>(otherDistrict, entry.getValue()));
							break;
						}
					}
				} else {
					int referencesBetweenDistricts = refMapper.getAmountOfRelationsToACityElement(district, otherDistrict, false)
														+ refMapper.getAmountOfRelationsToACityElement(district, otherDistrict, true);
					
					referenceMatrix.get(district).add(new AbstractMap.SimpleEntry<ACityElement, Integer>(otherDistrict, referencesBetweenDistricts));
				}
			}
		}		
	}
	
	private ACityElement[] getNearestNeighbours() {
		ACityElement[] nearestNeighbours = new ACityElement[2];
		int nearestNeighboursMax = 0;
		
		for (Entry<ACityElement, SortedSet<Map.Entry<ACityElement, Integer>>> matrixLine : referenceMatrix.entrySet()) {
			try {
				int distance = matrixLine.getValue().first().getValue();
				
				if (distance >= referenceThreshold && distance > nearestNeighboursMax) {
					nearestNeighbours[0] = matrixLine.getKey();
					nearestNeighbours[1] = matrixLine.getValue().first().getKey();
					nearestNeighboursMax = distance;
				}		
			} catch (NoSuchElementException e) {
				return nearestNeighbours;
			}
		}		
		return nearestNeighbours;
	}
	
	private List<ACityElement> addToCluster(ACityElement element1, ACityElement element2) {
		
		// check if element1 is already in cluster list
		for (List<ACityElement> cluster : clusterList) {
			if (cluster.contains(element1)) {
				cluster.add(element2);
				return cluster;
			} else if (cluster.contains(element2)) {
				cluster.add(element1);
				return cluster;
			}			
		}
		
		List<ACityElement> newCluster = new ArrayList<ACityElement>() {{ add(element1); add(element2); }};
		clusterList.add(newCluster);
		return newCluster;
	}

}
