package org.getaviz.generator.abap.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.layouts.ADistrictRoadNetwork;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;

public class MetropolisRoadNetworkLayouter {
	
	private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private SourceNodeRepository nodeRepository;
    private ACityRepository repository;

	public MetropolisRoadNetworkLayouter(ACityRepository aCityRepository, SourceNodeRepository sourceNodeRepository, SettingsConfiguration config) {
		this.config = config;
		this.repository = aCityRepository;
		this.nodeRepository = sourceNodeRepository;
		
        log.info("*****************************************************************************************************************************************");
        log.info("created");
	}
	
	public void createRoadNetworks() {
		for (ACityElement namespaceDistrict : this.repository.getNamespaceDistrictsOfOriginSet()) {
			ADistrictRoadNetwork roadNetwork = new ADistrictRoadNetwork(this.nodeRepository, this.repository, namespaceDistrict, this.config);
			roadNetwork.calculate();
		}
	}

}
