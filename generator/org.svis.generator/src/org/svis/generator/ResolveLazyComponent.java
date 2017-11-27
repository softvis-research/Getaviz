/**
 * 
 */
package org.svis.generator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
 
/**
 * Resolves lazy linking resources by replacing it with it content.
 * loads all resources not already being loaded.
 * 
 * @author Joerg Henss
 * 
 */
public class ResolveLazyComponent implements IWorkflowComponent {
 
//	@Override
	public void invoke(IWorkflowContext ctx) {
		Set<String> names = ctx.getSlotNames();
		for (String slotName : names) {
 
			Object slotContent = ctx.get(slotName);
			if (slotContent instanceof Iterable) {
				Iterator<?> iter = ((Iterable<?>) slotContent).iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					if (o instanceof Resource) {
						Resource r = ((Resource) o);
						if(!r.isLoaded())
							try {
								r.load(null);
							} catch (IOException e) {
								throw new RuntimeException("Error loading slot "+ slotName, e);
							} 
 
						if(r instanceof LazyLinkingResource)
							ctx.put(slotName, r.getContents());
					}
				}
			}
 
		}
 
	}
 
	//@Override
	public void postInvoke() {
		// TODO Auto-generated method stub
 
	}
 
	//@Override
	public void preInvoke() {
		// TODO Auto-generated method stub
 
	}
 
}
