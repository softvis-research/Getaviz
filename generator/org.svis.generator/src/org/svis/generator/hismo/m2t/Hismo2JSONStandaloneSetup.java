package org.svis.generator.hismo.m2t;

import org.eclipse.xtext.generator.IGenerator2;
import org.svis.generator.hismo.m2t.Hismo2JSON;
import org.svis.xtext.HismoRuntimeModule;
import org.svis.xtext.HismoStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Hismo2JSONStandaloneSetup extends HismoStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new HismoRuntimeModule() {

			@Override
			public Class<? extends IGenerator2> bindIGenerator2() {
				return Hismo2JSON.class;
			}
		});
	}
}
