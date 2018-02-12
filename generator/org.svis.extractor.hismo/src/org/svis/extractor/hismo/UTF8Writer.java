package org.svis.extractor.hismo;

	import java.io.FileNotFoundException;
import java.io.FileReader;
/*******************************************************************************
	 * Copyright (c) 2005, 2009 committers of openArchitectureWare and others.
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html
	 *
	 * Contributors:
	 *     committers of openArchitectureWare - initial API and implementation
	 *******************************************************************************/
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.WorkflowInterruptedException;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.emf.mwe.utils.Writer;
import org.svis.generator.Configuration;

public class UTF8Writer extends Writer {

	private static final String COMPONENT_NAME = "Writer";

	private boolean OPTION_SCHEMA_LOCATION = true;

	private boolean OPTION_SCHEMA_LOCATION_IMPLEMENTATION = true;

	private final String encoding = "UTF-8";

	private boolean multipleResourcesInCaseOfList = false;

	private boolean cloneSlotContents = false;
	
	private boolean ignoreEmptySlot = false;
	
	private Configuration config;

	@SuppressWarnings("unchecked")
	@Override
	public void invokeInternal(final WorkflowContext ctx, final ProgressMonitor monitor, final Issues issues) {
		Object slotContent = ctx.get(getModelSlot());
		if (slotContent == null) {
			if (ignoreEmptySlot) {
				issues.addWarning(this, "slot '" + getModelSlot() + "' is empty. Not writing anything.");
			}
			else {
				issues.addError(this, "slot '" + getModelSlot() + "' is empty.");
			}
			return;
		}
		if (!((slotContent instanceof Collection<?>) || (slotContent instanceof EObject))) {
			issues.addError(this, "slot '" + getModelSlot() + "' neither contains an EList nor an EObject",
					slotContent, null, null);
			return;
		}

		if (slotContent instanceof EObject) {
			final EObject sc = (EObject) slotContent;
			if (cloneSlotContents) {
				if (sc.eResource() == null) {
					issues.addWarning(this, "model in slot '" + getModelSlot()
							+ "' is not yet associated with a resource; cloning it is most likely an error!");
				}
				else {
					final EcoreUtil.Copier copier = new EcoreUtil.Copier();
					final EObject copy = copier.copy(sc);
					copier.copyReferences();
					slotContent = copy;
				}
			}
			else {
				if (sc.eResource() != null) {
					issues.addWarning(this, "the element in slot '" + getModelSlot()
							+ "' is already contained in a resource and will be taken out of that resource!");
				}
			}
		}

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

		if (!multipleResourcesInCaseOfList) {
			final Resource r = getResourceSet().createResource(URI.createURI(getUri()));
			if (slotContent instanceof Collection<?>) {
				r.getContents().addAll((Collection<EObject>) slotContent);
			}
			else {
				r.getContents().add((EObject) slotContent);
			}
			write(r);
		}
		else {
			if (slotContent instanceof Collection<?>) {
				final Collection<?> coll = (Collection<?>) slotContent;
				final Collection<Resource> resources = new ArrayList<Resource>();
				for (final Object object : coll) {
					final EObject eo = (EObject) object;
					final Resource r = getResourceSet().createResource(URI.createURI(createResourceName(eo)));
					r.getContents().add(eo);
					resources.add(r);
				}
				for (final Resource r : resources) {
					write(r);
				}
			}
			else {
				final Resource r = getResourceSet().createResource(URI.createURI(getUri()));
				r.getContents().add((EObject) slotContent);
				write(r);
			}
		}
	}

	private String createResourceName(final EObject eo) {
		return getUri() + (getUri().endsWith("/") ? "" : "/") + getName(eo) + ".ecore";
	}

	private String getName(final EObject model) {
		return (String) model.eGet(model.eClass().getEStructuralFeature("name"));
	}

	private void write(final Resource r) {
		try {
			final Map<String, Object> options = new HashMap<String, Object>();
			if (OPTION_SCHEMA_LOCATION) {
				options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
			}
			if (OPTION_SCHEMA_LOCATION_IMPLEMENTATION) {
				options.put(XMLResource.OPTION_SCHEMA_LOCATION_IMPLEMENTATION, Boolean.TRUE);
			}
			if (encoding != null) {
				options.put(XMLResource.OPTION_ENCODING, encoding);
			}
			r.save(options);
		}
		catch (final IOException e) {
			throw new WorkflowInterruptedException("Problems writing xmi file to " + getUri() + " : " + e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent#getLogMessage()
	 */
	@Override
	public String getLogMessage() {
		return "Writing model to " + uri;
	}

	/**
	 * @see org.eclipse.emf.mwe.core.WorkflowComponent#getComponentName()
	 */
	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}
	

	
	public void setConfig(String path) throws FileNotFoundException {
		config = new Configuration (new FileReader(path));
		uri = "file:///"+ config.getOutputDirectorySystem() + "/hismo.xml";
	}
	
	public Configuration getConfig() {
		return config;
	}
}
