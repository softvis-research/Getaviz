package org.svis.generator.city.m2m;

import org.svis.generator.city.CitySettings;
import org.svis.xtext.city.BuildingSegment;

public class BuildingSegmentComparator implements Comparable<BuildingSegmentComparator> {

	private BuildingSegment bs;
	private int coarseValue; // compares class elements (methods <-> attributes)
	private int fineValue; // compared after coarseValue
	private int finerValue; // compared after finevalue, if it was equal

	public BuildingSegmentComparator(final BuildingSegment bs) {
		this.bs = bs;

		setCoarseValue();

		switch (CitySettings.CLASS_ELEMENTS_SORT_MODE_FINE) {
			case ALPHABETICALLY: // names compared directly in compareTo-method
				break;
			case SCHEME:
				switch (CitySettings.SCHEME) {
					case VISIBILITY:
						fineValue = getCompValue_Visibility(bs.getModifiers());
						break;
					case TYPES:
						fineValue = getCompValue_Type(bs);
						break;
				}
				break;
			case NOS: // numberOfStatements compared directly in compareTo-method
				finerValue = getCompValue_Type(bs); // If NOS are equal, sort for types
				break;
			case UNSORTED:
				break;
			default:
				break;
		}
	}

	static int getCompValue_Visibility(final String modifier) {
		if (modifier.indexOf("private") >= 0) {
			return CitySettings.SortPriorities_Visibility.PRIVATE;
		} else if (modifier.indexOf("protected") >= 0) {
			return CitySettings.SortPriorities_Visibility.PROTECTED;
		} else if (modifier.indexOf("public") >= 0) {
			return CitySettings.SortPriorities_Visibility.PUBLIC;
		} else {
			return CitySettings.SortPriorities_Visibility.PACKAGE;
		}
		
	}

	static int getCompValue_Type(final BuildingSegment bs) {
		switch (bs.getType()) {
			case "FAMIX.Attribute":
				if (Boolean.parseBoolean(bs.getDeclaredType().getIsPrimitiveType())) {
					return CitySettings.Attributes.SortPriorities_Types.PRIMITVE;
				} else {
					return CitySettings.Attributes.SortPriorities_Types.COMPLEX;
				}
			case "FAMIX.Method":
				if (bs.getValue().equals(bs.getParent().getValue())) {
					return CitySettings.Methods.SortPriorities_Types.CONSTRUCTOR;
				} else if (bs.getSignature().startsWith("get")) {
					return CitySettings.Methods.SortPriorities_Types.GETTER;
				} else if (bs.getSignature().startsWith("set")) {
					return CitySettings.Methods.SortPriorities_Types.SETTER;
				} else if (bs.getModifiers().contains("static")) {
					return CitySettings.Methods.SortPriorities_Types.STATIC;
				} else if (bs.getModifiers().contains("abstract")) {
					return CitySettings.Methods.SortPriorities_Types.ABSTRACT;
				} else {
					return CitySettings.Methods.SortPriorities_Types.LEFTOVER;
				}
			default:
				return 0;
		}
		
	}

	@Override
	public int compareTo(final BuildingSegmentComparator comp) {
		int result;
		// Coarse sorting after attributes and methods if elements aren't the same type of class element
		if (coarseValue < comp.coarseValue)
			result = -1;
		else if (coarseValue > comp.coarseValue)
			result = 1;
		else
			result = 0;

		if (result != 0)
			return result;

		// Sorting after fine sort mode between equal class elements types (e.g. method compared to method)
		switch (CitySettings.CLASS_ELEMENTS_SORT_MODE_FINE) {
			case UNSORTED:
				return 0;
			case ALPHABETICALLY:
				result = bs.getValue().compareTo(comp.bs.getValue());
				break;
			case SCHEME:
				if (fineValue < comp.fineValue)
					result = -1;
				else if (fineValue > comp.fineValue)
					result = 1;
				else
					return compareNOS(comp); // Largest methods are always at the bottom in SCHEME-mode
				break;
			case NOS:
				result = compareNOS(comp);
				if (result == 0)
					if (finerValue < comp.finerValue)
						result = -1;
					else if (finerValue > comp.finerValue)
						result = 1;
					else
						return 0;
				break;
			default:
				return 0;
		}

		// Reverse order if setting has been made
		if (CitySettings.CLASS_ELEMENTS_SORT_MODE_FINE_DIRECTION_REVERSED)
			return result * -1;
		else
			return result;
	}

	/** Compares the number of statements inside the given methods. */
	private int compareNOS(final BuildingSegmentComparator comp) {
		if (bs.getNumberOfStatements() < comp.bs.getNumberOfStatements())
			return 1;
		else if (bs.getNumberOfStatements() > comp.bs.getNumberOfStatements())
			return -1;
		else
			return 0;
	}

	public BuildingSegment getBuildingSegment() {
		return bs;
	}

	/**
	 * Called once by constructor. Sets the coarse sort value.<br>
	 * Made an additional method for that to make it better readable.
	 */
	private void setCoarseValue() {
		switch (CitySettings.CLASS_ELEMENTS_SORT_MODE_COARSE) {
			case ATTRIBUTES_FIRST:
				switch (bs.getType()) {
					case "FAMIX.Attribute":
						coarseValue = -1;
						break;
					case "FAMIX.Method":
						coarseValue = 1;
						break;
					default:
						coarseValue = 0;
						break;
				}
				break;
			case METHODS_FIRST:
				switch (bs.getType()) {
					case "FAMIX.Attribute":
						coarseValue = 1;
						break;
					case "FAMIX.Method":
						coarseValue = -1;
						break;
					default:
						coarseValue = 0;
						break;
				}
				break;
			case UNSORTED:
				coarseValue = 0;
				break;
			default:
				switch (bs.getType()) {
					case "FAMIX.Attribute":
						coarseValue = 1;
						break;
					case "FAMIX.Method":
						coarseValue = -1;
						break;
					default:
						coarseValue = 0;
						break;
				}
				break;
		}
	}
}
