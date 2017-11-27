package org.svis.generator.plant.m2t;

import org.eclipse.xtext.generator.IGenerator2;
import org.svis.xtext.RDRuntimeModule;
import org.svis.xtext.RDStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Plant2X3DStandaloneSetup extends RDStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new RDRuntimeModule() {

			@Override
			public Class<? extends IGenerator2> bindIGenerator2() {
				return Plant2X3D.class;
			}
		});
	}
}
