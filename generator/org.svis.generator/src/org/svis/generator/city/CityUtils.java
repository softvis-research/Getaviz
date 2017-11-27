package org.svis.generator.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.svis.generator.city.CitySettings.OutputFormat;
import org.svis.generator.city.m2m.BuildingSegmentComparator;
import org.svis.generator.city.m2m.RGBColor;
import org.svis.xtext.city.BuildingSegment;
import org.svis.xtext.city.DeclaredType;
import org.svis.xtext.famix.FAMIXAnnotationType;
import org.svis.xtext.famix.FAMIXClass;
import org.svis.xtext.famix.FAMIXElement;
import org.svis.xtext.famix.FAMIXEnum;
import org.svis.xtext.famix.FAMIXParameterType;
import org.svis.xtext.famix.FAMIXParameterizableClass;
import org.svis.xtext.famix.FAMIXParameterizedType;
import org.svis.xtext.famix.FAMIXPrimitiveType;
import org.svis.xtext.famix.FAMIXType;
import org.svis.xtext.famix.IntegerReference;

public class CityUtils {

	/** Used to save some memory for empty string values, which often occurs. */
	private static final String emptyString = "";
	
	public static String getFamixClassString(final String className) {
		String s = className.substring(0, 5) + "." + className.substring(5, className.length());
		if (className.endsWith("Impl"))
			s = s.substring(0, s.length()-4);
		return s;
	}

	/**
	 * Fills the data of a {@link DeclaredType} instance with data of the {@link IntegerReference}.
	 *
	 * @param dt DeclaredType
	 * @param elem IntegerReference
	 * @return declared type
	 */
	public static DeclaredType fillDeclaredType(final DeclaredType dt, final IntegerReference elem) {
		if (elem == null || elem.getRef() == null) {
//			System.out.println("NULL");
			dt.setFqn(emptyString);
			dt.setId(emptyString);
			dt.setValue(emptyString);
			dt.setIsPrimitiveType("false");
			dt.setArguments(emptyString);
			return dt;
		}

//		final FAMIXElement refElem = (FAMIXElement) elem.getRef();
//		if (refElem instanceof FAMIXParameterizedTypeImpl) System.out.println(refElem.getClass().getSimpleName());

		try {
			final FAMIXElement refElem = (FAMIXElement) elem.getRef();
			dt.setName(refElem.getName());
			dt.setType(getFamixClassString(refElem.getClass().getSimpleName()));
			dt.setArguments(emptyString);
			switch (refElem.getClass().getSimpleName()) {
				case "FAMIXAnnotationTypeImpl":
					final FAMIXAnnotationType fAnnotation = (FAMIXAnnotationType) refElem;
					dt.setFqn(fAnnotation.getFqn());
//					dt.setId(sha1Hex(dt.getFqn()));
					dt.setValue(fAnnotation.getValue());
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
					break;
				case "FAMIXClassImpl":
					final FAMIXClass fClass = (FAMIXClass) refElem;
					dt.setFqn(fClass.getFqn());
//					dt.setId(sha1Hex(dt.getFqn()));
					dt.setValue(fClass.getValue());
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
					break;
				case "FAMIXEnumImpl":
					final FAMIXEnum cRefElem = (FAMIXEnum) refElem;
					dt.setFqn(cRefElem.getFqn());
//					dt.setId(sha1Hex(dt.getFqn()));
					dt.setValue(cRefElem.getValue());
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
					break;
				case "FAMIXParameterizableClassImpl":
					final FAMIXParameterizableClass fParamClass = (FAMIXParameterizableClass) refElem;
					dt.setFqn(fParamClass.getFqn());
					dt.setValue(fParamClass.getValue());
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
					break;
				case "FAMIXParameterizedTypeImpl":
					final FAMIXParameterizedType fParamzType = (FAMIXParameterizedType) refElem;
					dt.setFqn(fParamzType.getFqn());
					dt.setValue(fParamzType.getValue());
					dt.setIsPrimitiveType("false");

					String arguments = new String("ArgumentsSize: " + fParamzType.getArguments().size() + " ");
					for (IntegerReference argElem : fParamzType.getArguments()) {
						if (argElem != null && argElem.getRef() != null)
							arguments += getFamixElementValue(argElem.getRef()) + " "; 
					}
					dt.setArguments(arguments);
//					if(fParamzType.getArguments().size() > 3) System.out.println(arguments);
					break;
				case "FAMIXParameterTypeImpl":
					final FAMIXParameterType fParamrType = (FAMIXParameterType) refElem;
					dt.setFqn(fParamrType.getName());
//					dt.setId(sha1Hex(dt.getName()));
					dt.setValue(fParamrType.getValue());
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
					break;
				case "FAMIXPrimitiveTypeImpl":
					final FAMIXPrimitiveType fprimitiveType = (FAMIXPrimitiveType) refElem;
					dt.setFqn(fprimitiveType.getName());
//					dt.setId(sha1Hex(dt.getName()));
					dt.setValue(fprimitiveType.getValue());
					dt.setIsPrimitiveType("true");
					dt.setArguments(emptyString);
					break;
				case "FAMIXTypeImpl":
//					System.out.println(elem.getClass().getSimpleName());
					final FAMIXType fType = (FAMIXType) refElem;
					dt.setFqn(fType.getName());
//					dt.setId(sha1Hex(dt.getName()));
					dt.setValue(fType.getValue());
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
					break;
				default: {
//					System.out.println("Default: " + refElem.getClass().getSimpleName());
					dt.setFqn(emptyString);
					dt.setId(emptyString);
					dt.setValue(emptyString);
					dt.setIsPrimitiveType("false");
					dt.setArguments(emptyString);
				}
			}
		} catch (final NullPointerException e) {
			dt.setFqn(emptyString);
			dt.setId(emptyString);
			dt.setValue(emptyString);
			dt.setIsPrimitiveType("false");
			dt.setArguments(emptyString);
			System.err.println("NullPointerException in CityUtils.fillDeclaredType(DeclaredType, IntegerReference)."
					+ " The returned DeclaredType will be empty.");
//			e.printStackTrace();
		} catch (final ClassCastException e) {
			dt.setFqn(emptyString);
			dt.setId(emptyString);
			dt.setValue(emptyString);
			dt.setIsPrimitiveType("false");
			dt.setArguments(emptyString);
			System.err.println("ClassCastException in CityUtils.fillDeclaredType(DeclaredType, IntegerReference)."
					+ " The returned DeclaredType will be empty.");
//			e.printStackTrace();
		}
//		System.out.println(refElem.getClass().getSimpleName() + " -> " + getFamixElementValue(refElem) + " -> " + dt.getIsPrimitiveType() + "(" + refElem.getName() + ")");
		return dt;
	}

	/**
	 * Trys to cast the element to its actual famix type and returns the value
	 * through its {@code getValue()}-method. Otherwise returns an empty string.
	 *
	 *
	 * @param elem FAMIXElement
	 * @return famix element value
	 */
	public static String getFamixElementValue(final FAMIXElement elem) {
		if (elem == null)
			return emptyString;

		switch (elem.getClass().getSimpleName()) {
			case "FAMIXAnnotationTypeImpl":
				return ((FAMIXAnnotationType) elem).getValue();
			case "FAMIXClassImpl":
				return ((FAMIXClass) elem).getValue();
			case "FAMIXEnumImpl":
				return ((FAMIXEnum) elem).getValue();
			case "FAMIXParameterizableClassImpl":
				return ((FAMIXParameterizableClass) elem).getValue();
			case "FAMIXParameterizedTypeImpl":
				return ((FAMIXParameterizedType) elem).getValue();
			case "FAMIXParameterTypeImpl":
				return ((FAMIXParameterType) elem).getValue();
			case "FAMIXPrimitiveTypeImpl":
				return ((FAMIXPrimitiveType) elem).getValue();
			case "FAMIXTypeImpl":
				return ((FAMIXType) elem).getValue();
			default:
				return emptyString;
		}
	}

	/**
	 * Creates the color gradient for the packages depending on your hierarchy level.
	 *
	 * @param start RGBColor
	 * @param end RGBColor
	 * @param maxLevel int
	 * @return color range
	 */
	public static RGBColor[] createPackageColorGradient(final RGBColor start, final RGBColor end, final int maxLevel) {
		int steps = maxLevel - 1;
		if (maxLevel == 1) {
			steps++;
		}
		double r_step = (end.r() - start.r()) / steps;
		double g_step = (end.g() - start.g()) / steps;
		double b_step = (end.b() - start.b()) / steps;

		RGBColor[] colorRange = new RGBColor[maxLevel];
		double newR, newG, newB;
		for (int i = 0; i < maxLevel; ++i) {
			newR = start.r() + i * r_step;
			newG = start.g() + i * g_step;
			newB = start.b() + i * b_step;

			colorRange[i] = new RGBColor(newR, newG, newB);
		}

		return colorRange;
	}

	/**
	 * Sets the color for a specific {@link BuildingSegment}.
	 * 
	 * @param bs BuildingSegment which has to get a color.
	 */
	public static void setBuildingSegmentColor(final BuildingSegment bs) {
		if(CitySettings.OUTPUT_FORMAT == OutputFormat.AFrame){
			switch (CitySettings.SCHEME) {
				case VISIBILITY:
					if (bs.getModifiers().contains("public")) {
						bs.setColor(CitySettings.COLOR_DARK_GREEN_HEX);
					} else if (bs.getModifiers().contains("protected")) {
						bs.setColor(CitySettings.COLOR_YELLOW_HEX);
					} else if (bs.getModifiers().contains("private")) {
						bs.setColor(CitySettings.COLOR_RED_HEX);
					} else {
						// Package visibility or default
						bs.setColor(CitySettings.COLOR_BLUE_HEX);
					}
					break;
				case TYPES:
					switch (bs.getType()) {
						case "FAMIX.Attribute": setAttributeColor(bs); break;
						case "FAMIX.Method": setMethodColor(bs); break;
						default:
							bs.setColor(CitySettings.COLOR_BLUE_HEX);
	//						System.out.println("setBuildingSegmentColor(BS) type not handled: \"" + bs.getType() + "\" colored with default color.");
							break;
					}
					break;
				default: bs.setColor(CitySettings.COLOR_BLUE_HEX);
			}	
		} else {
			switch (CitySettings.SCHEME) {
				case VISIBILITY:
					if (bs.getModifiers().contains("public")) {
						bs.setColor(CitySettings.COLOR_DARK_GREEN.asPercentage());
					} else if (bs.getModifiers().contains("protected")) {
						bs.setColor(CitySettings.COLOR_YELLOW.asPercentage());
					} else if (bs.getModifiers().contains("private")) {
						bs.setColor(CitySettings.COLOR_RED.asPercentage());
					} else {
						// Package visibility or default
						bs.setColor(CitySettings.COLOR_BLUE.asPercentage());
					}
					break;
				case TYPES:
					switch (bs.getType()) {
						case "FAMIX.Attribute": setAttributeColor(bs); break;
						case "FAMIX.Method": setMethodColor(bs); break;
						default:
							bs.setColor(CitySettings.COLOR_BLUE.asPercentage());
	//						System.out.println("setBuildingSegmentColor(BS) type not handled: \"" + bs.getType() + "\" colored with default color.");
							break;
					}
					break;
				default: bs.setColor(CitySettings.COLOR_BLUE.asPercentage());
			}
		}	
	}

	private static void setAttributeColor(final BuildingSegment bs) {
		if(CitySettings.OUTPUT_FORMAT == OutputFormat.AFrame){
			if (Boolean.parseBoolean(bs.getDeclaredType().getIsPrimitiveType())) {
				bs.setColor(CitySettings.COLOR_PINK_HEX);
			} else { // complex type
				bs.setColor(CitySettings.COLOR_AQUA_HEX);
			}	
		} else {
			if (Boolean.parseBoolean(bs.getDeclaredType().getIsPrimitiveType())) {
				bs.setColor(CitySettings.COLOR_PINK.asPercentage());
			} else { // complex type
				bs.setColor(CitySettings.COLOR_AQUA.asPercentage());
			}
		}	
	}

	private static void setMethodColor(final BuildingSegment bs) {
		if(CitySettings.OUTPUT_FORMAT == OutputFormat.AFrame){
	//		if (bs.getMethodKind().equals("constructor")) {
			if (bs.getValue().equals(bs.getParent().getValue())) {
				bs.setColor(CitySettings.COLOR_RED_HEX);
			} else if (bs.getSignature().startsWith("get")) {
				bs.setColor(CitySettings.COLOR_LIGHT_GREEN_HEX);
			} else if (bs.getSignature().startsWith("set")) {
				bs.setColor(CitySettings.COLOR_DARK_GREEN_HEX);
			} else if (bs.getModifiers().contains("static")) {
				bs.setColor(CitySettings.COLOR_YELLOW_HEX);
			} else if (bs.getModifiers().contains("abstract")) {
				bs.setColor(CitySettings.COLOR_ORANGE_HEX);
			} else {
				// Default
				bs.setColor(CitySettings.COLOR_VIOLET_HEX);
			}
		} else {
	//		if (bs.getMethodKind().equals("constructor")) {
			if (bs.getValue().equals(bs.getParent().getValue())) {
				bs.setColor(CitySettings.COLOR_RED.asPercentage());
			} else if (bs.getSignature().startsWith("get")) {
				bs.setColor(CitySettings.COLOR_LIGHT_GREEN.asPercentage());
			} else if (bs.getSignature().startsWith("set")) {
				bs.setColor(CitySettings.COLOR_DARK_GREEN.asPercentage());
			} else if (bs.getModifiers().contains("static")) {
				bs.setColor(CitySettings.COLOR_YELLOW.asPercentage());
			} else if (bs.getModifiers().contains("abstract")) {
				bs.setColor(CitySettings.COLOR_ORANGE.asPercentage());
			} else {
				// Default
				bs.setColor(CitySettings.COLOR_VIOLET.asPercentage());
			}
		}	
	}

	/**
	 * Sorting the {@link BuildingSegment}s with help of
	 * {@link BuildingSegmentComparator} based on sorting settings in
	 * {@link CitySettings}.
	 * 
	 * @param bsList BuildingSegments which are to be sorted.
	 *
	 */
	public static void sortBuildingSegments(final EList<BuildingSegment> bsList) {
		final List<BuildingSegmentComparator> sortedList = new ArrayList<BuildingSegmentComparator>(bsList.size());
		for (BuildingSegment bs : bsList)
			sortedList.add(new BuildingSegmentComparator(bs));
		Collections.sort(sortedList);
		bsList.clear();
		for (BuildingSegmentComparator bsc : sortedList)
			bsList.add(bsc.getBuildingSegment());
	}

}