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

    private Map<ACityRectangle, ACityElement> rectangleElementsMap;

    public ADistrictCircluarLayout(ACityElement district, Collection<ACityElement> subElements, SettingsConfiguration config) {
        this.config = config;

        this.district = district;
        this.subElements = subElements;

        rectangleElementsMap = new HashMap<>();
    }

    public void calculate(){

        ACityRectangle coveringACityRectangle = arrangeSubElements(subElements);
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

    private void setNewPositionFromNode(ACityRectangle rectangle, ACityKDTreeNode fitNode) {
        ACityElement element = rectangleElementsMap.get(rectangle);

        double xPosition = fitNode.getACityRectangle().getCenterX();// - config.getBuildingHorizontalGap() / 2;
        double xPositionDelta = xPosition - element.getXPosition();
        element.setXPosition(xPosition);

        double zPosition = fitNode.getACityRectangle().getCenterY();//- config.getBuildingHorizontalGap() / 2;
        double zPositionDelta = zPosition - element.getZPosition();
        element.setZPosition(zPosition);

        Collection<ACityElement> subElements = element.getSubElements();
        if(!subElements.isEmpty()){
            adjustPositionsOfSubSubElements(subElements, xPositionDelta, zPositionDelta);
        }
    }

    private void adjustPositionsOfSubSubElements(Collection<ACityElement> elements, double parentX, double parentZ) {
        for (ACityElement element : elements) {

            double centerX = element.getXPosition();
            double centerY = element.getYPosition();
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

    private ACityRectangle arrangeSubElements(Collection<ACityElement> subElements) {
        // get maxArea (worst case) for root of KDTree
        ACityRectangle docACityRectangle = calculateMaxAreaRoot(subElements);
        ACityKDTree ptree = new ACityKDTree(docACityRectangle);

        ACityRectangle covrec = new ACityRectangle();

        List<ACityRectangle> elements = createACityRectanglesOfElements(subElements);
        Collections.sort(elements);
        Collections.reverse(elements);

        List<ACityRectangle> originSet = new ArrayList<>();
        List<ACityRectangle> customCode = new ArrayList<>();
        List<ACityRectangle> standardCode = new ArrayList<>();

        // order the rectangles to the fit sets
        for (ACityRectangle element : elements) {

            ACityElement recElement = rectangleElementsMap.get(element);
            ACityElement.ACitySubType refBuilding = recElement.getSubType();

            //no sourceNode, no refBuilding
            if (recElement.getSourceNode() == null && refBuilding == null) {
                continue;
            }

            // for RefBuildings
            if (recElement.getSourceNode() == null && refBuilding != null){
                if (refBuilding == ACityElement.ACitySubType.Sea || refBuilding == ACityElement.ACitySubType.Mountain ){
                    originSet.add(element);
                }
            }

            // for Elements with SourceNode
            if ( recElement.getSourceNode() != null && refBuilding == null) {

                String creator = recElement.getSourceNodeProperty(SAPNodeProperties.creator);
                String iterationString = recElement.getSourceNodeProperty(SAPNodeProperties.iteration);
                int iteration = Integer.parseInt(iterationString);

                if (iteration == 0 && (!creator.equals("SAP"))) {
                    originSet.add(element);
                } else if (iteration >= 1 && (!creator.equals("SAP"))) {
                    customCode.add(element);
                } else if (iteration >= 1 && creator.equals("SAP")) {
                    standardCode.add(element);
                }
            }
        }

        // Light Map algorithm for the origin set
        for (ACityRectangle el : originSet) {
            Map<ACityKDTreeNode, Double> preservers = new LinkedHashMap<>();
            Map<ACityKDTreeNode, Double> expanders = new LinkedHashMap<>();
            ACityKDTreeNode targetNode = new ACityKDTreeNode();
            ACityKDTreeNode fitNode = new ACityKDTreeNode();

            List<ACityKDTreeNode> pnodes = ptree.getFittingNodes(el);

            // check all empty leaves: either they extend COVREC (->expanders) or it doesn't
            // change (->preservers)
            for (ACityKDTreeNode pnode : pnodes) {
                sortEmptyLeaf(pnode, el, covrec, preservers, expanders);
            }

            // choose best-fitting pnode
            if (!preservers.isEmpty()) {
                targetNode = bestFitIsPreserver(preservers.entrySet());
            } else {
                targetNode = bestFitIsExpander(expanders.entrySet());
            }

            // modify targetNode if necessary
            if (targetNode.getACityRectangle().getWidth() == el.getWidth()
                    && targetNode.getACityRectangle().getLength() == el.getLength()) { // this if could probably be skipped,
                // trimmingNode() always returns
                // fittingNode
                fitNode = targetNode;
            } else {
                fitNode = trimmingNode(targetNode, el);
            }

            // set fitNode as occupied
            fitNode.setOccupied();

            // give Entity it's Position
            setNewPositionFromNode(el, fitNode);

            // if fitNode expands covrec, update covrec
            if (fitNode.getACityRectangle().getBottomRightX() > covrec.getBottomRightX()
                    || fitNode.getACityRectangle().getBottomRightY() > covrec.getBottomRightY()) {
                updateCovrec(fitNode, covrec);
            }
        }


        arrangeDistrictsCircular(customCode, covrec);
        arrangeDistrictsCircular(standardCode, covrec);

        return covrec; // used to adjust viewpoint in x3d
    }

    private void arrangeDistrictsCircular(List<ACityRectangle> elements, ACityRectangle covrec) {
        double covrecRadius = covrec.getPerimeterRadius() + config.getBuildingHorizontalGap();
        SettingsConfiguration.NotInOriginLayoutVersion version = config.getAbapNotInOrigin_layout_version();

        if (elements.size() == 0)
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
                        case MINIMAL_DISTANCE:
//							rotationAngle = Math.acos(1 - (Math.pow(previousRadius + currentRadius, 2) / (2 * Math.pow(radius, 2))));
                            rotationAngle = 2 * Math.asin((previousRadius + currentRadius) / (2 * radius));
                            break;
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
                        default:
//							rotationAngle = Math.acos(1 - (Math.pow(previousRadius + currentRadius, 2) / (2 * Math.pow(radius, 2))));
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


    private ACityRectangle calculateMaxAreaRoot(Collection<ACityElement> elements) {
        double sum_width = 0;
        double sum_length = 0;
        for (ACityElement element : elements) {
            sum_width += element.getWidth() + config.getBuildingHorizontalGap();
            sum_length += element.getLength() + config.getBuildingHorizontalGap();
        }
        return new ACityRectangle(0, 0, sum_width, sum_length, 1);
    }

    private void sortEmptyLeaf(ACityKDTreeNode pnode, ACityRectangle el, ACityRectangle covrec,
                               Map<ACityKDTreeNode, Double> preservers, Map<ACityKDTreeNode, Double> expanders) {
        // either element fits in current bounds (->preservers) or it doesn't
        // (->expanders)
        double nodeUpperLeftX = pnode.getACityRectangle().getUpperLeftX();
        double nodeUpperLeftY = pnode.getACityRectangle().getUpperLeftY();
        double nodeNewBottomRightX = nodeUpperLeftX + el.getWidth(); // expected BottomRightCorner, if el was insert
        // into pnode
        double nodeNewBottomRightY = nodeUpperLeftY + el.getLength(); // this new corner-point is compared with covrec

        if (nodeNewBottomRightX <= covrec.getBottomRightX() && nodeNewBottomRightY <= covrec.getBottomRightY()) {
            double waste = pnode.getACityRectangle().getArea() - el.getArea();
            preservers.put(pnode, waste);
        } else {
            double ratio = ((Math.max(nodeNewBottomRightX, covrec.getBottomRightX()))
                    / (Math.max(nodeNewBottomRightY, covrec.getBottomRightY())));
            expanders.put(pnode, ratio);
        }
    }

    private ACityKDTreeNode bestFitIsPreserver(Set<Map.Entry<ACityKDTreeNode, Double>> entrySet) {
        // determines which entry in Set has the lowest value of all
        double lowestValue = -1;
        ACityKDTreeNode targetNode = new ACityKDTreeNode();
        for (Map.Entry<ACityKDTreeNode, Double> entry : entrySet) {
            if (entry.getValue() < lowestValue || lowestValue == -1) {
                lowestValue = entry.getValue();
                targetNode = entry.getKey();
            }
        }
        return targetNode;
    }

    private ACityKDTreeNode bestFitIsExpander(Set<Map.Entry<ACityKDTreeNode, Double>> entrySet) {
        double closestTo = 1;
        double lowestDistance = -1;
        ACityKDTreeNode targetNode = new ACityKDTreeNode();
        for (Map.Entry<ACityKDTreeNode, Double> entry : entrySet) {
            double distance = Math.abs(entry.getValue() - closestTo);
            if (distance < lowestDistance || lowestDistance == -1) {
                lowestDistance = distance;
                targetNode = entry.getKey();
            }
        }
        return targetNode;
    }

    private ACityKDTreeNode trimmingNode(ACityKDTreeNode node, ACityRectangle r) {

        double nodeUpperLeftX = node.getACityRectangle().getUpperLeftX();
        double nodeUpperLeftY = node.getACityRectangle().getUpperLeftY();
        double nodeBottomRightX = node.getACityRectangle().getBottomRightX();
        double nodeBottomRightY = node.getACityRectangle().getBottomRightY();

        // first split: horizontal cut, if necessary
        // Round to 3 digits to prevent infinity loop, because e.g. 12.34000000007 is
        // declared equal to 12.34
        if (Math.round(node.getACityRectangle().getLength() * 1000d) != Math.round(r.getLength() * 1000d)) {
            // new child-nodes
            node.setLeftChild(new ACityKDTreeNode(
                    new ACityRectangle(nodeUpperLeftX, nodeUpperLeftY, nodeBottomRightX, (nodeUpperLeftY + r.getLength()))));
            node.setRightChild(new ACityKDTreeNode(new ACityRectangle(nodeUpperLeftX, (nodeUpperLeftY + r.getLength()),
                    nodeBottomRightX, nodeBottomRightY)));
            // set node as occupied (only leaves can contain elements)
            node.setOccupied();



            return trimmingNode(node.getLeftChild(), r);
            // second split: vertical cut, if necessary
            // Round to 3 digits, because e.g. 12.34000000007 is declared equal to 12.34
        } else if (Math.round(node.getACityRectangle().getWidth() * 1000d) != Math.round(r.getWidth() * 1000d)) {
            // new child-nodes
            node.setLeftChild(new ACityKDTreeNode(
                    new ACityRectangle(nodeUpperLeftX, nodeUpperLeftY, (nodeUpperLeftX + r.getWidth()), nodeBottomRightY)));
            node.setRightChild(new ACityKDTreeNode(new ACityRectangle((nodeUpperLeftX + r.getWidth()), nodeUpperLeftY,
                    nodeBottomRightX, nodeBottomRightY)));
            // set node as occupied (only leaves can contain elements)
            node.setOccupied();


            return node.getLeftChild();
        } else {

            return node;
        }
    }

    private void updateCovrec(ACityKDTreeNode fitNode, ACityRectangle covrec) {
        double newX = (Math.max(fitNode.getACityRectangle().getBottomRightX(), covrec.getBottomRightX()));
        double newY = (Math.max(fitNode.getACityRectangle().getBottomRightY(), covrec.getBottomRightY()));
        covrec.changeRectangle(0, 0, newX, newY);
    }

}