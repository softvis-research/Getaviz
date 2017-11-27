package org.svis.generator.plant.m2m;

public class PlantManager {

	/**
	 * Generate an random number from 0 to max.
	 * 
	 * @param max
	 * @param allowNegativVaues
	 * @return
	 */
	public static double calcRamdonNumber(int max, boolean allowNegativVaues) {
		double result = 0;
		result = (int) Math.floor((Math.random() * max) + 1);
		double a = (int) Math.floor((Math.random() * max) + 1);
		if (a > (max / 2) && allowNegativVaues) {
			result = (-result);
		}
		return result;
	}

	/**
	 * Calculate the position for an given petal index.
	 * 
	 * @param index
	 * @return doube[]{x,y,z]
	 */
	public static double[] getPetalPositionForIndex(int index) {

		if (index > 15) {
			index = index % 16;
		}
		switch (index) {
		case 0: { // Osten
			double[] a = { 1, 0, 0 };
			return a;
		}
		case 1: {// Norden
			double[] a = { 0, 1, 0 };
			return a;
		}
		case 2: { // Westen
			double[] a = { -1, 0, 0 };
			return a;
		}
		case 3: { // Süden
			double[] a = { 0, -1, 0 };
			return a;
		}
		case 4: {
			double[] a = { 0.8, 0.8, 0 };
			return a;
		}
		case 5: {
			double[] a = { -0.8, 0.8, 0 };
			return a;
		}
		case 6: {
			double[] a = { -0.8, -0.8, 0 };
			return a;
		}
		case 7: {
			double[] a = { 0.8, -0.8, 0 };
			return a;
		}
		case 8: {
			double[] a = { 1, 0.5, 0 };
			return a;
		}
		case 9: {
			double[] a = { 0.5, 1, 0 };
			return a;
		}
		case 10: {
			double[] a = { -0.5, 1, 0 };
			return a;
		}
		case 11: {
			double[] a = { -1, 0.5, 0 };
			return a;
		}
		case 12: {
			double[] a = { -1, -0.5, 0 };
			return a;
		}
		case 13: {
			double[] a = { -0.5, -1, 0 };
			return a;
		}
		case 14: {
			double[] a = { 1, -0.5, 0 };
			return a;
		}
		case 15: {
			double[] a = { 0.5, -1, 0 };
			return a;
		}
		default: {
			double[] a = { 1, 1, 1 };
			return a;
		}
		}
	}

	/**
	 * Calculate the angular for an given petal index.
	 * 
	 * @param index
	 * @return double
	 */
	public static double getPetalAngularForIndex(int index) {
		if (index > 15) {
			index = index % 16;
		}
		switch (index) {
		case 0: {
			return 0;
		}
		case 1: {
			return 1.5708;
		}
		case 2: {
			return 0;
		}
		case 3: {
			return 1.5708;
		}
		case 4: {
			return (0.7854);
		}
		case 5: {
			return (-0.7854);
		}
		case 6: {
			return (0.7854);
		}
		case 7: {
			return (-0.7854);
		}
		case 8: {
			return (0.3927);
		}
		case 9: {
			return (1.0472);
		}
		case 10: {
			return (-1.0472);
		}
		case 11: {
			return (-0.3927);
		}
		case 12: {
			return (0.3927);
		}
		case 13: {
			return (1.0472);
		}
		case 14: {
			return (-0.3927);
		}
		case 15: {
			return (-1.0472);
		}
		default: {
			return 0;
		}
		}
	}

	/**
	 * Multiplication of a array/vector.
	 * 
	 * @param input
	 *            array
	 * @param multi
	 *            double value
	 * @return double[] array
	 */
	public static double[] vektorMultiplication(double[] input, double multi) {

		for (int i = 0; i < input.length; i++) {
			input[i] = input[i] * multi;
		}
		return input;
	}
}
