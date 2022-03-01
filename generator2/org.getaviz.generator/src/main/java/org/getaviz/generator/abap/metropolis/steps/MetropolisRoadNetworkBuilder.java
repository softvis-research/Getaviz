package org.getaviz.generator.abap.metropolis.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.layouts.ADistrictRoadNetwork;
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
		ACityElement virtualRootDistrict = this.createVirtualRootDistrict();
		ADistrictRoadNetwork rootRoadNetwork = new ADistrictRoadNetwork(this.nodeRepository, this.repository, virtualRootDistrict, new HashMap<>(), this.config);
		List<ACityElement> mainRoads = rootRoadNetwork.calculate();
		
		// TODO
		// An welches Element sollen die Straﬂen gehangen werden?
		// Eigentlich brauchen wir hier ein Root-Element
//		this.saveRoads(mainRoads, virtualRootDistrict);
		
		int counter = 0;
		
		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {
			ADistrictRoadNetwork roadNetwork = new ADistrictRoadNetwork(this.nodeRepository, this.repository, namespaceDistrict, rootRoadNetwork.getSubElementConnectors(namespaceDistrict), this.config);
			List<ACityElement> roads = roadNetwork.calculate();
			this.saveRoads(roads, namespaceDistrict);
			
			// TODO
			// Workaround
			if (counter == 0) {
				this.saveRoads(mainRoads, namespaceDistrict);
				counter++;
			}
			
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
		
		
		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {
			virtualRootDistrict.addSubElement(namespaceDistrict);
		}
		
		double minX = Double.POSITIVE_INFINITY,
			   maxX = Double.NEGATIVE_INFINITY,
			   minY = Double.POSITIVE_INFINITY,
			   maxY = Double.NEGATIVE_INFINITY;
		
		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {

			double rightX = namespaceDistrict.getXPosition() + namespaceDistrict.getWidth() / 2.0;			
			double leftX = namespaceDistrict.getXPosition() - namespaceDistrict.getWidth() / 2.0;
			double upperY = namespaceDistrict.getZPosition() + namespaceDistrict.getLength() / 2.0;			
			double lowerY = namespaceDistrict.getZPosition() - namespaceDistrict.getLength() / 2.0;
			
			if (leftX < minX) {
				minX = leftX;
			}
			
			if (lowerY < minY) {
				minY = lowerY;
			}
			
			if (maxX < rightX) {
				maxX = rightX;
			}
			
			if (maxY < upperY) {
				maxY = upperY;
			}
			
		}
		
		virtualRootDistrict.setXPosition((maxX - minX) / 2.0);
		virtualRootDistrict.setYPosition(0);
		virtualRootDistrict.setZPosition((maxY - minY) / 2.0);
		
		virtualRootDistrict.setWidth(maxX - minX + 2 * config.getACityDistrictHorizontalMargin());
		virtualRootDistrict.setLength(maxY - minY + 2 * config.getACityDistrictHorizontalMargin());
		virtualRootDistrict.setHeight(0.0);
		
		return virtualRootDistrict;
	}

}
