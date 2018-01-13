package org.svis.generator.multisphere.m2m;

import java.util.ArrayList;
import java.util.List;

public class DeterministicSphere extends Sphere {

	
	
		
	public DeterministicSphere(int pointAmount, double radius, double x,
			double y, double z) {
		super(pointAmount, radius, x, y, z);
	}
	
	public DeterministicSphere(int pointAmount, double radius, double x,
			double y, double z, boolean logOn) {
		super(pointAmount, radius, x, y, z, logOn);		
	}

	protected void createPoints(int pointAmount) {

		// create defaultPoints
		List<List<SurfacePoint>> pointLists = new ArrayList<List<SurfacePoint>>();
		int pointListSize = 0;

		pointLists.add(new ArrayList<SurfacePoint>());
		pointLists.add(new ArrayList<SurfacePoint>());
		pointLists.add(new ArrayList<SurfacePoint>());
		pointListSize = pointLists.size();

		for (int i = 0; i < pointAmount; i++) {

			// thirdlast list size > 1 ? -> create two new lists
			if (pointLists.get(pointListSize - 3).size() > 1) {
				pointLists.add(new ArrayList<SurfacePoint>());
				pointLists.add(new ArrayList<SurfacePoint>());
				pointListSize = pointLists.size();
			}

			// empty list? -> create new point in empty list
			boolean emptyListFound = false;
			for (List<SurfacePoint> list : pointLists) {
				if (list.size() == 0) {
					list.add(new SurfacePoint(0, 0, this));
					emptyListFound = true;
					break;
				}
			}

			if (emptyListFound) {
				continue;
			}

			// size of next list the same ? -> create new point in this list
			for (int j = 0; j < pointListSize; j++) {
				List<SurfacePoint> thisList = pointLists.get(j);
				List<SurfacePoint> nextList = pointLists.get(j + 1);

				if (thisList.size() == nextList.size()
						|| thisList.size() - 1 == nextList.size()) {
					thisList.add(new SurfacePoint(0, 0, this));
					break;
				}
			}

		}

		// Someting like this 21111 or 32211
		// -> 11211 and 12321

		int[] orderedPointListSizes = new int[pointListSize];

		boolean left = false;
		int positionCounter = 0;
		for (int i = 0; i < pointListSize; i++) {

			if (left) {
				orderedPointListSizes[positionCounter - 1] = pointLists.get(
						pointListSize - i - 1).size();
				left = false;
			} else {
				orderedPointListSizes[pointListSize - positionCounter - 1] = pointLists
						.get(pointListSize - i - 1).size();
				left = true;
				positionCounter++;
			}
		}

		// calculate angles and positions
		double intervalAlpha = Math.PI / (pointListSize - 1);

		for (int i = 0; i < pointListSize; i++) {

			int thisListSize = orderedPointListSizes[i];

			double intervalBeta = Math.PI * 2 / thisListSize;

			for (int j = 0; j < thisListSize; j++) {

				double alpha = intervalAlpha * i;
				double beta = intervalBeta * j;

				surfacePoints.add(new SurfacePoint(alpha, beta, this));
			}
		}

	}
	
	
	
	
}
