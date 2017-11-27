package org.svis.generator.city.m2t;

import org.eclipse.xtext.generator.IGenerator2;
import org.svis.xtext.CityRuntimeModule;
import org.svis.xtext.CityStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CityOutputStandaloneSetup extends CityStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new CityRuntimeModule() {

			@Override
			public Class<? extends IGenerator2> bindIGenerator2() {
				return CityOutput.class;
			}
		});
	}
}
