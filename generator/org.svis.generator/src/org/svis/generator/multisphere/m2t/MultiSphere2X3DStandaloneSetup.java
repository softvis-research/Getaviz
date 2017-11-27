package org.svis.generator.multisphere.m2t;

import org.eclipse.xtext.generator.IGenerator2;
import org.svis.xtext.GraphRuntimeModule;
import org.svis.xtext.GraphStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MultiSphere2X3DStandaloneSetup extends GraphStandaloneSetup {

	public Injector createInjector() {
		return Guice.createInjector(new GraphRuntimeModule() {

			@Override
			public Class<? extends IGenerator2> bindIGenerator2() {
				return MultiSphere2X3D.class;
			}
		});
	}
}
