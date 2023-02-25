package org.getaviz.generator.abap.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.layouts.kdtree.ACityKDTree;
import org.getaviz.generator.abap.layouts.kdtree.ACityKDTreeNode;
import org.getaviz.generator.abap.layouts.kdtree.ACityRectangle;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;

import java.math.BigDecimal;
import java.util.*;

public class ADistrictDebuggerLayout {
    private ACityRepository repository;
    //Old coding -> Refactor, generalize and maybe reimplement

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement district;
    private Collection<ACityElement> subElements;

    private Map<ACityRectangle, ACityElement> rectangleElementsMap;

    public ADistrictDebuggerLayout(ACityElement district, Collection<ACityElement> subElements, SettingsConfiguration config, ACityRepository aCityRepository) {
        this.config = config;

        this.district = district;
        this.subElements = subElements;
        this.repository = aCityRepository;
        rectangleElementsMap = new HashMap<>();
    }

    public void calculate(){
        ACityRectangle coveringACityRectangle  = new ACityRectangle(0, 0, 100, 100); //= arrangeSubElements(subElements);
        /*setSizeOfDistrict(coveringACityRectangle);
        setPositionOfDistrict(coveringACityRectangle);
        adjustPositionsOfSubSubElements( subElements );*/
        //ACityRectangle coveringACityRectangle  = arrangeSubElements(subElements);

        setSizeOfDistrict(coveringACityRectangle);

        setPositionOfDistrict(coveringACityRectangle);


    }


    private void setSizeOfDistrict(ACityRectangle coveringACityRectangle) {
        try {
            String type = district.getSourceNodeProperty(SAPNodeProperties.type_name).toString();
        if (type.equals("Namespace"))
        {
            district.setWidth( 25 + 10 ); //coveringACityRectangle.getWidth());
            district.setLength( repository.getHighestPosition() * 35 +50);//200 ); //coveringACityRectangle.getLength());
            district.setHeight( config.getACityDistrictHeight());
        }
        else{
            district.setWidth( 25 ); //coveringACityRectangle.getWidth());
            district.setLength(25 ); //coveringACityRectangle.getLength());
            district.setHeight( 2 ); // config.getACityDistrictHeight());

        }
    } catch (Exception e) {
    }
    }

    private void setPositionOfDistrict(ACityRectangle coveringACityRectangle) {

        district.setYPosition( district.getHeight() / 2); //Does not change?
        //district.setZPosition(coveringACityRectangle.getCenterY()); //Position
        //district.setZPosition( district.getSourceNodeProperty(SAPNodeProperties.position) * 10 ); //Position
        try {
            String type = district.getSourceNodeProperty(SAPNodeProperties.type_name).toString();
            if (!type.equals("Namespace")) {
                String positionStr = district.getSourceNodeProperty(SAPNodeProperties.position);
                int positionInt = Integer.parseInt(positionStr);
                district.setZPosition(positionInt * (district.getWidth() + 10));

                String iterationStr = district.getSourceNodeProperty(SAPNodeProperties.iteration);
                int iterationInt = Integer.parseInt(iterationStr) +1;
                district.setXPosition( iterationInt * (district.getLength() + 10));

                adjustPositionsOfSubSubElements( district.getSubElements(), district.getXPosition(),district.getZPosition() );

            }else{
                district.setZPosition( 25 * repository.getHighestPosition());//district.getLength());
                district.setXPosition( 35 );//coveringACityRectangle.getCenterX()); // Iteration
            }
        } catch (Exception e) {
        }
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
        double oldXPosition = 1;
        double oldZPosition = 1; //Import Export /Change /Return
        for (ACityElement element : elements) {

            double centerX = 1; //element.getXPosition();
            double newXPosition = centerX + parentX + config.getACityBuildingHorizontalMargin()+ oldXPosition - 5;
            element.setXPosition(newXPosition);
            oldXPosition += 2;

            double centerZ;
            String paramTypeString = element.getSourceNodeProperty(SAPNodeProperties.param_type);
            switch (paramTypeString){
                default:
                case "LOCAL":
                    centerZ = parentZ; //element.getZPosition();
                    break;
                case "IMPORT":
                    centerZ = parentZ -10 ;//element.getZPosition() + 10;
                    break;
                case "EXPORT":
                    centerZ = parentZ + 10 ;//element.getZPosition() - 10;
                    break;
                case "RETURN":
                    centerZ = parentZ + 10;//element.getZPosition() - 10;
                    break;
                case "CHANGING":
                    centerZ = parentZ ;//element.getZPosition();
                    break;

            }

            double newZPosition = centerZ; // + parentZ ; //+ config.getACityBuildingHorizontalMargin();// + oldZPosition;
            element.setZPosition(newZPosition);
            //oldZPosition += 4;

            Collection<ACityElement> subElements = element.getSubElements();
            if(!subElements.isEmpty()){
                //adjustPositionsOfSubSubElements(subElements, parentX,  parentZ);
            }
        }
    }



   /* public ACityRectangle arrangeSubElements(Collection<ACityElement> subElements){
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        ACityRectangle docACityRectangle = calculateMaxAreaRoot(subElements);
        ACityKDTree ptree = new ACityKDTree(docACityRectangle);

        ACityRectangle covrec = new ACityRectangle();

        List<ACityRectangle> elements = createACityRectanglesOfElements(subElements);
        Collections.sort(elements);
        Collections.reverse(elements);

        // algorithm
        for (ACityRectangle el : elements) {
        //for (CityRectangle subElement : subElements) {
            int x = subElement.getX();
            int y = subElement.getY();
            int width = subElement.getWidth();
            int height = subElement.getHeight();

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + width);
            maxY = Math.max(maxY, y + height);
        }

        ACityRectangle coveringACityRectangle = new ACityRectangle(minX, minY, maxX - minX, maxY - minY);
        return coveringACityRectangle;
    }*/






    /*
        Copied from CityLayout
     */

    private ACityRectangle arrangeSubElements(Collection<ACityElement> subElements){

        ACityRectangle docACityRectangle = calculateMaxAreaRoot(subElements);
        ACityKDTree ptree = new ACityKDTree(docACityRectangle);

        ACityRectangle covrec = new ACityRectangle();

        List<ACityRectangle> elements = createACityRectanglesOfElements(subElements);
        Collections.sort(elements);
        Collections.reverse(elements);

        // algorithm
        for (ACityRectangle el : elements) {

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

        return covrec;
    }

    private List<ACityRectangle> createACityRectanglesOfElements(Collection<ACityElement> elements) {
        List<ACityRectangle> rectangles = new ArrayList<>();

        for (ACityElement element : elements) {

            if(element.getSubType() != null){
                if(element.getSubType().equals(ACityElement.ACitySubType.Cloud)){
                    continue;
                }}
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
        BigDecimal newY_big = BigDecimal.valueOf(newY);
        covrec.changeRectangle(0, 0, newX, newY_big.doubleValue());
    }


}