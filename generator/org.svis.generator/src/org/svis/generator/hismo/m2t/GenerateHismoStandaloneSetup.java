package org.svis.generator.hismo.m2t;

import org.eclipse.xtext.generator.IGenerator2;
import org.svis.generator.hismo.m2t.GenerateHismo;
import org.svis.xtext.HismoRuntimeModule;
import org.svis.xtext.HismoStandaloneSetup;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GenerateHismoStandaloneSetup extends HismoStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new HismoRuntimeModule() {
			@Override
			public Class<? extends IGenerator2> bindIGenerator2() {
				return GenerateHismo.class;
			}
		});
	}
}
