package org.getaviz.generator;

//import org.apache.commons.logging.LogFactory
import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.city.m2m.RGBColor;
import java.util.List;

public class Helper {
//	val log = LogFactory::getLog(class)

	public static String removeBrackets(List<String> list) {
		return removeBrackets(list.toString());
	}
	
	
	public static String removeBrackets(String string) {
		return StringUtils.remove(StringUtils.remove(string, "["), "]");
	}
}
