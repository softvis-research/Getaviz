package org.getaviz.generator.abap.metropolis.steps;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.layouts.ADistrictRoadNetwork;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.abap.repository.ACityElement.ACityType;

public class MetropolisRoadNetworkBuilder {
	
	private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

	public MetropolisRoadNetworkBuilder(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
		this.config = config;
		this.repository = aCityRepository;
		this.nodeRepository = sourceNodeRepository;
		
        log.info("*****************************************************************************************************************************************");
        log.info("created");
	}
	
	public void createRoadNetworks() {
//		ACityElement virtualRootDistrict = this.createVirtualRootDistrict();
//		ADistrictRoadNetwork rootRoadNetwork = new ADistrictRoadNetwork(this.nodeRepository, this.repository, virtualRootDistrict, this.config);
//		List<ACityElement> mainRoads = rootRoadNetwork.calculate();
		
		// TODO
		// An welches Element sollen die Straﬂen gehangen werden?
		// Eigentlich brauchen wir hier ein Root-Element
//		this.saveRoads(mainRoads, virtualRootDistrict);
		
		int counter = 0;
		
		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {
			ADistrictRoadNetwork roadNetwork = new ADistrictRoadNetwork(this.nodeRepository, this.repository, namespaceDistrict, this.config);
			List<ACityElement> roads = roadNetwork.calculate();
			this.saveRoads(roads, namespaceDistrict);
			
			// TODO
			// Workaround
//			if (counter == 0) {
//				this.saveRoads(mainRoads, namespaceDistrict);
//				counter++;
//			}
			
		}
	}
	
	private void saveRoads(List<ACityElement> roads, ACityElement containingDistrict) {
		for (ACityElement road : roads) {
			containingDistrict.addSubElement(road);
			this.repository.addElement(road);
		}
	}
	
	private ACityElement createVirtualRootDistrict() {
		ACityElement virtualRootDistrict = new ACityElement(ACityType.District);
		
		virtualRootDistrict.setYPosition(0);
		
		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {
			virtualRootDistrict.addSubElement(namespaceDistrict);
		}
		
//		double lowerLeftX = Double.POSITIVE_INFINITY, lowerLeftY = Double.POSITIVE_INFINITY,
//			   lowerRightX = Double.NEGATIVE_INFINITY, lowerRightY = Double.POSITIVE_INFINITY,
//			   upperRightX = Double.NEGATIVE_INFINITY, upperRightY = Double.NEGATIVE_INFINITY,
//			   upperLeftX = Double.POSITIVE_INFINITY, upperLeftY = Double.NEGATIVE_INFINITY;
//		
//		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {
//
//			double rightX = namespaceDistrict.getXPosition() + namespaceDistrict.getWidth() / 2.0;			
//			double leftX = namespaceDistrict.getXPosition() - namespaceDistrict.getWidth() / 2.0;
//			double upperY = namespaceDistrict.getZPosition() + namespaceDistrict.getLength() / 2.0;			
//			double lowerY = namespaceDistrict.getZPosition() - namespaceDistrict.getLength() / 2.0;
//			
//			if (leftX < lowerLeftX && lowerY < lowerLeftY) {
//				lowerLeftX = leftX;
//				lowerLeftY = lowerY;
//			}
//			
//			if (lowerRightX < rightX && lowerY < lowerRightY) {
//				lowerRightX = rightX;
//				lowerRightY = lowerY;
//			}
//			
//			if (upperRightX < rightX && upperRightY < upperY) {
//				upperRightX = rightX;
//				upperRightY = upperY;
//			}
//			
//			if (leftX < upperLeftX && upperLeftY < upperY) {
//				upperLeftX = leftX;
//				upperLeftY = upperY;
//			}
//			
//		}
		
		return virtualRootDistrict;
	}

}
