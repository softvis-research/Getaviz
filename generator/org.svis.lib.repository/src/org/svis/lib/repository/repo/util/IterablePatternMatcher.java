package org.svis.lib.repository.repo.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class IterablePatternMatcher {

	public Iterable<Pattern> transformToPattern(Iterable<String> filters) {
		Set<Pattern> returnable = new HashSet<Pattern>();
		for(String filter : filters){
			returnable.add(Pattern.compile(filter));
		}
		return returnable;
	}

	public boolean isIncluded(Iterable<Pattern> filters, String currentFilePath) {
		boolean isIncluded = true;
		for (Iterator<Pattern> iterator = filters.iterator(); iterator.hasNext() && isIncluded;) {
			Pattern filter = iterator.next();
			isIncluded &= filter.matcher(currentFilePath).matches(); 
		}
		return isIncluded;
	}
}
