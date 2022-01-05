package org.getaviz.generator.abap.layouts;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;

public class ADistrictRoadNetwork {

	private ACityRepository repository;
	private ACityElement district;
    private SettingsConfiguration config;
	
	public ADistrictRoadNetwork(ACityRepository repository, ACityElement district, SettingsConfiguration config) {
		this.repository = repository;
		this.district = district;
		this.config = config;
	}
	
	public void calculate() {
		
	}

}
