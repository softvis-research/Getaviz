package org.svis.xtext.parser;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;
import org.apache.commons.lang3.StringUtils;

public class MSESTRINGConverter extends DefaultTerminalConverters {

	@ValueConverter(rule = "MSESTRING")
	public IValueConverter<String> MSESTRING() {
		return new IValueConverter<String>() {
			public String toValue(String string, INode node) {
				if (Strings.isEmpty(string))
					throw new ValueConverterException(
							"Couldn't convert empty MSESTRING to String.",
							node, null);
				else {
					//return "\'" + string + "\'";
//					return string.replaceAll("'", "");
					return StringUtils.remove(string, "'");
				}

			}

			public String toString(String value) {
				//return value.replaceAll("'", "");
				return "\'" + value + "\'";
			}

		};

	}
}