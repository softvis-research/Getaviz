package org.svis.generator.famix;

import org.eclipse.xtext.generator.IGenerator2;
import org.svis.xtext.FamixRuntimeModule;
import org.svis.xtext.FamixStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Famix2JSONStandaloneSetup extends FamixStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new FamixRuntimeModule() {

			@Override
			public Class<? extends IGenerator2> bindIGenerator2() {
				return Famix2JSON.class;
			}
		});
	}
}
