package org.svis.xtext
import org.svis.xtext.parser.MSESTRINGConverter;
import org.eclipse.xtext.conversion.IValueConverterService;

/*
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class FamixRuntimeModule extends AbstractFamixRuntimeModule {

   override public Class<? extends IValueConverterService> bindIValueConverterService() {
        return MSESTRINGConverter
	}
}
