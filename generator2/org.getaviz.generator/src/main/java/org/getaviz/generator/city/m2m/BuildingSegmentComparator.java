package org.getaviz.generator.city.m2m;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.SettingsConfiguration.Attributes;
import org.getaviz.generator.SettingsConfiguration.Methods;
import org.getaviz.generator.SettingsConfiguration.SortPriorities_Visibility;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.database.Labels;

public class BuildingSegmentComparator implements Comparable<BuildingSegmentComparator> {
	private Node segment;
	private Node relatedEntity;
	// Temporary attribute to use db and famix in same transformation
	private int coarseValue; // compares class elements (methods <-> attributes)
	private int fineValue; // compared after coarseValue
	private int finerValue; // compared after finevalue, if it was equal
	private SettingsConfiguration config = SettingsConfiguration.getInstance();
	private static DatabaseConnector connector = DatabaseConnector.getInstance();

	public BuildingSegmentComparator(final Node segment) {
		this.segment = segment;
		this.relatedEntity = connector.getVisualizedEntity(segment.id());
		setCoarseValue();
		switch (config.getClassElementsSortModeFine()) {
		case ALPHABETICALLY: // names compared directly in compareTo-method
			break;
		case SCHEME:
			switch (config.getScheme()) {
			case VISIBILITY:
				fineValue = getCompValue_Visibility(relatedEntity);
				break;
			case TYPES:
				fineValue = getCompValue_Type(relatedEntity);
				break;
			}
			break;
		case NOS: // numberOfStatements compared directly in compareTo-method
			finerValue = getCompValue_Type(relatedEntity); // If NOS are equal, sort for types
			break;
		case UNSORTED:
			break;
		default:
			break;
		}
	}

	static int getCompValue_Visibility(final String modifier) {
		if (modifier.indexOf("private") >= 0) {
			return SortPriorities_Visibility.PRIVATE;
		} else if (modifier.indexOf("protected") >= 0) {
			return SortPriorities_Visibility.PROTECTED;
		} else if (modifier.indexOf("public") >= 0) {
			return SortPriorities_Visibility.PUBLIC;
		} else {
			return SortPriorities_Visibility.PACKAGE;
		}

	}

	static int getCompValue_Visibility(final Node relatedEntity) {
		String visbility = relatedEntity.get("visibility").asString();
		if (visbility.equals("private")) {
			return SortPriorities_Visibility.PRIVATE;
		} else if (visbility.equals("protected")) {
			return SortPriorities_Visibility.PROTECTED;
		} else if (visbility.equals("public")) {
			return SortPriorities_Visibility.PUBLIC;
		} else {
			return SortPriorities_Visibility.PACKAGE;
		}
	}

	static int getCompValue_Type(final Node relatedEntity) {
		if (relatedEntity.hasLabel(Labels.Field.name())) {
			boolean isPrimitive = false;
			StatementResult result = connector.executeRead("MATCH (n)-[OF_TYPE]->(t:Primitive) WHERE ID(n) = " + relatedEntity.id() + " RETURN t");
			if(result.hasNext()) {
				isPrimitive = true;
			}
			if (isPrimitive) {
				return Attributes.SortPriorities_Types.PRIMITVE;
			} else {
				return Attributes.SortPriorities_Types.COMPLEX;
			}
		} else {
			boolean isStatic = relatedEntity.get("static").asBoolean(false);
			boolean isAbstract = relatedEntity.get("abstract").asBoolean(false);
			if (relatedEntity.hasLabel(Labels.Constructor.name())) {
				return Methods.SortPriorities_Types.CONSTRUCTOR;
			} else if (relatedEntity.hasLabel(Labels.Getter.name())) {
				return Methods.SortPriorities_Types.GETTER;
			} else if (relatedEntity.hasLabel(Labels.Setter.name())) {
				return Methods.SortPriorities_Types.SETTER;
			} else if (isStatic) {
				return Methods.SortPriorities_Types.STATIC;
			} else if (isAbstract) {
				return Methods.SortPriorities_Types.ABSTRACT;
			} else {
				return Methods.SortPriorities_Types.LEFTOVER;
			}
		}
	}

	@Override
	public int compareTo(final BuildingSegmentComparator comp) {
		int result;
		// Coarse sorting after attributes and methods if elements aren't the same type
		// of class element
		if (coarseValue < comp.coarseValue)
			result = -1;
		else if (coarseValue > comp.coarseValue)
			result = 1;
		else
			result = 0;

		if (result != 0)
			return result;

		// Sorting after fine sort mode between equal class elements types (e.g. method
		// compared to method)
		switch (config.getClassElementsSortModeFine()) {
		case UNSORTED:
			return 0;
		case ALPHABETICALLY:
			String name = relatedEntity.get("name").asString();
			result = name.compareTo(comp.relatedEntity.get("name").asString());
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
		if (config.isClassElementsSortModeFineDirectionReversed())
			return result * -1;
		else
			return result;
	}

	/** Compares the number of statements inside the given methods. */
	private int compareNOS(final BuildingSegmentComparator comp) {
		long numberOfStatements = relatedEntity.get("effectiveLineCount").asLong(0);
		long numberOfStatementsComp = comp.relatedEntity.get("effectiveLineCount").asLong(0);
		if (numberOfStatements < numberOfStatementsComp)
			return 1;
		else if (numberOfStatements > numberOfStatementsComp)
			return -1;
		else
			return 0;
	}

	public Node getSegment() {
		return segment;
	}

	/**
	 * Called once by constructor. Sets the coarse sort value.<br>
	 * Made an additional method for that to make it better readable.
	 */
	private void setCoarseValue() {
		switch (config.getClassElementsSortModeCoarse()) {
		case ATTRIBUTES_FIRST:
			if (relatedEntity.hasLabel(Labels.Field.name())) {
				coarseValue = -1;
			} else if (relatedEntity.hasLabel(Labels.Method.name())) {
				coarseValue = 1;
			} else {
				coarseValue = 0;
			}

		case METHODS_FIRST:
			if (relatedEntity.hasLabel(Labels.Field.name())) {
				coarseValue = 1;
			} else if (relatedEntity.hasLabel(Labels.Method.name())) {
				coarseValue = -1;
			} else {
				coarseValue = 0;
			}

			break;
		case UNSORTED:
			coarseValue = 0;
			break;
		default:
			if (relatedEntity.hasLabel(Labels.Field.name())) {
				coarseValue = 1;
			} else if (relatedEntity.hasLabel(Labels.Method.name())) {
				coarseValue = -1;
			} else {
				coarseValue = 0;
			}

		}
	}
}
