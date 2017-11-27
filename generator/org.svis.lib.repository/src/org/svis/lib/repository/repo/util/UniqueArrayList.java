package org.svis.lib.repository.repo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class UniqueArrayList<T> extends ArrayList<T> {
	
	private static final long serialVersionUID = 8088920045881693683L;
	
	private HashSet<Integer> uniqueSet = new HashSet<Integer>();

	@Override
	public boolean add(T obj) {
		return addWhenNotAlreadyPresent(obj);
	}

	private boolean addWhenNotAlreadyPresent(T obj) {
		if (uniqueSet.add(obj.hashCode())) {
			super.add(obj);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean hasAllInserted = true;
		for(T current : c){
			hasAllInserted &= addWhenNotAlreadyPresent(current);
		}
		return hasAllInserted;
	}
	
}
