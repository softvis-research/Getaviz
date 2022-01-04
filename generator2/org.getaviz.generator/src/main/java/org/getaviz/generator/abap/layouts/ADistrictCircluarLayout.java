package org.getaviz.generator.abap.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.layouts.kdtree.ACityKDTree;
import org.getaviz.generator.abap.layouts.kdtree.ACityKDTreeNode;
import org.getaviz.generator.abap.layouts.kdtree.ACityRectangle;

import java.util.*;

public class ADistrictCircluarLayout {
    //Old coding -> Refactor, generalize and maybe reimplement

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement district;
    private Collection<ACityElement> subElements;
    private ACityRectangle encircledRectangle;

    private Map<ACityRectangle, ACityElement> rectangleElementsMap;

    public ADistrictCircluarLayout(ACityElement district, Collection<ACityElement> subElements, SettingsConfiguration config, ACityRectangle encircledRectangle) {
        this.config = config;

        this.district = district;
        this.subElements = subElements;
        this.encircledRectangle = encircledRectangle;

        rectangleElementsMap = new HashMap<>();
    }

    public void calculate(){
        ACityRectangle coveringACityRectangle = arrangeSubElements(subElements, encircledRectangle);
        setSizeOfDistrict(coveringACityRectangle);
        setPositionOfDistrict(coveringACityRectangle);
    }

    private void setSizeOfDistrict(ACityRectangle coveringACityRectangle) {
        district.setWidth(coveringACityRectangle.getWidth());
        district.setLength(coveringACityRectangle.getLength());
        district.setHeight(config.getACityDistrictHeight());
    }

    private void setPositionOfDistrict(ACityRectangle coveringACityRectangle) {
        district.setXPosition(coveringACityRectangle.getCenterX());
        district.setYPosition(district.getHeight() / 2);
        district.setZPosition(coveringACityRectangle.getCenterY());
    }

    private void adjustPositionsOfSubSubElements(Collection<ACityElement> elements, double parentX, double parentZ) {
        for (ACityElement element : elements) {

            double centerX = element.getXPosition();
            double centerZ = element.getZPosition();

            double newXPosition = centerX + parentX + config.getACityBuildingHorizontalMargin();
            double newZPosition = centerZ + parentZ + config.getACityBuildingHorizontalMargin();

            element.setXPosition(newXPosition);
            element.setZPosition(newZPosition);

            Collection<ACityElement> subElements = element.getSubElements();
            if(!subElements.isEmpty()){
                adjustPositionsOfSubSubElements(subElements, parentX, parentZ);
            }
        }
    }


    /*
        Copied from CityLayout
     */

    private ACityRectangle arrangeSubElements(Collection<ACityElement> subElements, ACityRectangle encircledRectangle) {
        ACityRectangle covrec = new ACityRectangle();

        List<ACityRectangle> elements = createACityRectanglesOfElements(subElements);
        Collections.sort(elements, Collections.reverseOrder());

        arrangeDistrictsCircular(elements, encircledRectangle);

        return covrec; // used to adjust viewpoint in x3d
    }

    private void arrangeDistrictsCircular(List<ACityRectangle> elements, ACityRectangle covrec) {
        double covrecRadius = covrec.getPerimeterRadius() + config.getBuildingHorizontalGap();
        SettingsConfiguration.NotInOriginLayoutVersion version = config.getAbapNotInOrigin_layout_version();

        if (elements.isEmpty())
            return;
        else {

            ACityRectangle biggestRec = elements.get(0);
            double maxOuterRadius = biggestRec.getPerimeterRadius();
            double sumOfPerimeterRadius = 0;

            for (ACityRectangle element : elements) {
                sumOfPerimeterRadius += element.getPerimeterRadius() + config.getBuildingHorizontalGap();

                if(element.getPerimeterRadius() > maxOuterRadius) {
                    maxOuterRadius = element.getPerimeterRadius();
                    biggestRec = element;
                    elements.remove(element);
                    elements.add(0, biggestRec);
                }

            }

            double minRadius = maxOuterRadius
                    + covrecRadius
                    + config.getBuildingHorizontalGap();

            double maxRadius = 0;

            // new estimation of the radius
            if (elements.size() > 1)
                maxRadius = (sumOfPerimeterRadius / elements.size()) / Math.sin(Math.PI / elements.size())
                        + config.getBuildingHorizontalGap();

            double radius = Math.max(minRadius, maxRadius);


            ACityElement biggestRectangle = rectangleElementsMap.get(biggestRec);

            double xPosition = covrec.getCenterX() + radius;
            double xPositionDelta = xPosition - biggestRectangle.getXPosition();
            biggestRectangle.setXPosition(xPosition);

            double zPosition = covrec.getCenterY();
            double zPositionDelta = zPosition - biggestRectangle.getZPosition();
            biggestRectangle.setZPosition(zPosition);

            Collection<ACityElement> subElements = biggestRectangle.getSubElements();
            if(!subElements.isEmpty()){
                adjustPositionsOfSubSubElements(subElements, xPositionDelta, zPositionDelta);
            }

            if (elements.size() > 1) {

                double cacheRotationAngle = 0;

                for (int i = 1; i < elements.size(); ++i) {

                    ACityRectangle previousRec = elements.get(i - 1);
                    ACityRectangle currentRec = elements.get(i);

                    double previousRadius = previousRec.getPerimeterRadius();
                    //+ config.getBuildingHorizontalGap();

                    double currentRadius = currentRec.getPerimeterRadius();
                    //+ config.getBuildingHorizontalGap();

                    double rotationAngle = 0;

                    switch(version) {

                        case FULL_CIRCLE:
                            double idealRotationAngle = 2 * Math.PI / elements.size() - cacheRotationAngle;
                            double leastRotationAngle = 2 * Math.asin((previousRadius + currentRadius) / (2 * radius));

                            if (idealRotationAngle >= leastRotationAngle) {
                                rotationAngle = idealRotationAngle;
                                cacheRotationAngle = 0;
                            } else {
                                rotationAngle = leastRotationAngle;
                                cacheRotationAngle = leastRotationAngle - idealRotationAngle;
                            }

                            break;

                        case MINIMAL_DISTANCE:
                        default:
                            rotationAngle = 2 * Math.asin((previousRadius + currentRadius) / (2 * radius));
                            break;
                    }

                    ACityElement previousRectangle = rectangleElementsMap.get(previousRec);
                    ACityElement currentRectangle = rectangleElementsMap.get(currentRec);

                    double newX = (previousRectangle.getXPosition() - covrec.getCenterX()) * Math.cos(rotationAngle)
                            - (previousRectangle.getZPosition() - covrec.getCenterY()) * Math.sin(rotationAngle)
                            + covrec.getCenterX();

                    double xPositionDeltaManyDistricts = newX - currentRectangle.getXPosition();
                    currentRectangle.setXPosition(newX);


                    double newZ = (previousRectangle.getXPosition() - covrec.getCenterX()) * Math.sin(rotationAngle)
                            + (previousRectangle.getZPosition() - covrec.getCenterY()) * Math.cos(rotationAngle)
                            + covrec.getCenterY();

                    double zPositionDeltaManyDistricts = newZ - currentRectangle.getZPosition();
                    currentRectangle.setZPosition(newZ);


                    Collection<ACityElement> subElementsManyDistricts = currentRectangle.getSubElements();
                    if(!subElementsManyDistricts.isEmpty()){
                        adjustPositionsOfSubSubElements(subElementsManyDistricts, xPositionDeltaManyDistricts, zPositionDeltaManyDistricts);
                    }
                }
            }

            double newCovrecWidth = 2 * radius + (Math.max(biggestRec.getWidth(), biggestRec.getLength()));
            covrec.changeRectangle(covrec.getCenterX(), covrec.getCenterY(), newCovrecWidth, newCovrecWidth, 0);
        }
    }

    private List<ACityRectangle> createACityRectanglesOfElements(Collection<ACityElement> elements) {
        List<ACityRectangle> rectangles = new ArrayList<>();

        for (ACityElement element : elements) {
            double width = element.getWidth();
            double length = element.getLength();

            ACityRectangle rectangle = new ACityRectangle(0, 0, width + config.getBuildingHorizontalGap(),
                    length + config.getBuildingHorizontalGap(), 1);
            rectangles.add(rectangle);
            rectangleElementsMap.put(rectangle, element);
        }
        return rectangles;
    }
}