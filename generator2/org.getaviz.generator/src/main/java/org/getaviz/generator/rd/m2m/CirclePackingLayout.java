package org.getaviz.generator.rd.m2m;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.rd.Disk;
import org.getaviz.generator.rd.MainDisk;
import org.getaviz.generator.rd.SubDisk;

import java.util.Collections;
import java.util.List;

public class CirclePackingLayout {
    private final List<MainDisk> rootDisks;
    private final Log log = LogFactory.getLog(this.getClass());

    class Node {
        Disk val;
        Node next = null;
        Node prev = null;
        Node insertNext = null;
        Node (Disk disk) {
            val = disk;
        }
    }

    CirclePackingLayout(List<MainDisk> rootDisks) {
        this.rootDisks = rootDisks;
    }

    void run() {
        layout(rootDisks);
        transformPositions(rootDisks);
    }

    private  <T extends Disk> void transformPositions(List<T> disks) {
        disks.forEach( (disk) -> {
            transformPositionOfInnerDisks(disk);
            transformPositions(disk.getInnerDisks());
        });
    }

    private void transformPositionOfInnerDisks(Disk disk) {
        disk.getInnerDisks().forEach( (innerDisk) -> {
            Position pos = innerDisk.getPosition();
            pos.x += disk.getPosition().x;
            pos.y += disk.getPosition().y;
        });
    }

    private <T extends Disk> void layout(List<T> diskList) {
        diskList.forEach(disk ->  {
            if (disk.hasInnerDisks()) {
                try {
                    layout(disk.getInnerDisks());
                } catch (NullPointerException e) {
                    log.error(e);
                    log.error(e.getStackTrace());
                }
                try {
                    calculateRadiusForOuterDisks(disk);
                } catch (NullPointerException e) {
                    log.error(e);
                    log.error(e.getStackTrace());
                }
            } else {
                if(disk instanceof SubDisk) {
                    try {
                        calculateRadiusForInnerDisks((SubDisk) disk);
                    } catch (NullPointerException e) {
                        log.error(e);
                        log.error(e.getStackTrace());
                    }
                } else {
                    log.info("surprise:" + disk.getID());
                    disk.setRadius(disk.getBorderWidth());
                }
            }
        });
        placeDisks(diskList);
    }

    private void calculateRadiusForInnerDisks(SubDisk disk) {
        disk.setRadius(Math.sqrt((disk.getInnerSegmentsArea() + disk.getOuterSegmentsArea()) / Math.PI) + disk.getBorderWidth());
    }

    private void calculateRadiusForOuterDisks(Disk disk) {
        CoordinateList coordinates = new CoordinateList();
        disk.getInnerDisks().forEach((innerDisk) -> coordinates.add(innerDisk.getCoordinates(), true));

        GeometryFactory geoFactory = new GeometryFactory();
        MultiPoint innerCirclemultipoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
        MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCirclemultipoint);

        final double radius = mbc.getRadius();

        disk.updatePosition(mbc.getCentre().x,mbc.getCentre().y);
        if(disk instanceof MainDisk) {
            disk.setRadius(disk.getBorderWidth() + radius);
        } else {
            SubDisk subDisk = (SubDisk) disk;
            disk.setRadius(disk.getBorderWidth() + Math.sqrt(radius * radius + (subDisk.getInnerSegmentsArea() + subDisk.getOuterSegmentsArea())/Math.PI));
        }
        disk.setInnerRadius(radius);
        normalizePositionOfInnerDisks(disk);
    }

    private void normalizePositionOfInnerDisks(Disk disk) {
        disk.getInnerDisks().forEach( (innerDisk) -> {
            innerDisk.getPosition().x -= disk.getPosition().x;
            innerDisk.getPosition().y -= disk.getPosition().y;
        });
        disk.setPosition(new Position(0, 0, disk.getPosition().z));
    }

    private void bound(Disk n, Disk topright, Disk bottemleft) {
      /*  bottemleft.getPosition().x = Math.min(n.getPosition().x - n.getRadius(), bottemleft.getPosition().x);
        bottemleft.getPosition().y = Math.min(n.getPosition().y - n.getRadius(), bottemleft.getPosition().y);
        topright.getPosition().x  = Math.max(n.getPosition().x + n.getRadius(), topright.getPosition().x);
        topright.getPosition().x  = Math.max(n.getPosition().x + n.getRadius(), topright.getPosition().x);*/
    }

    private  <T extends Disk> void placeDisks(List <T> disks) {
        log.info("new place disks");
        if (disks.isEmpty()) return;
        Collections.sort(disks);
        log.info("size: " + disks.size());
        Node firstNode = null;
        Node lastinsertednode = null;
        log.info("info");
        for(Disk disk : disks) {
            log.info("disk id:" + disk.getID());
            Node n = new Node(disk);
            if(firstNode == null) {
                log.info("firstNode");
                firstNode = n;
                lastinsertednode = n;
            } else {
                log.info("anderer node");
                lastinsertednode.insertNext = n;
                lastinsertednode = n;
            }
        }
        log.info("infoafter");
        Node a = firstNode;
      //  T topright = (T) a.val;
        //T bottemleft = (T) a.val;
        Node b = null;
        Node c = null;
        a.val.getPosition().x = -1 * a.val.getRadius() + 0.0; // sometimes we get -0.0, so we add 0.0 to get 0.0
        log.info("134: " + a.val.getPosition().x);
        //bound(a, topright, bottemleft);
        /* Create second circle. */
        log.info("isnull:" + (a.insertNext == null));
        if(a.insertNext == null) return;
        b = a.insertNext;
        log.info("139a: " + (b.val == null));
        log.info("139b: " + b.val.getPosition());
        b.val.getPosition().x = b.val.getRadius();
        log.info("139: " + b.val.getPosition().x);
        b.val.getPosition().y = 0.0;
           // bound(b, topright, bottemleft);
        log.info("default");
        /* Create third circle. */
        if(b.insertNext == null) return;
        c = b.insertNext;
        place(a.val, b.val, c.val);
          //  bound(c.val, topright, bottemleft);
        if(c.insertNext == null) return;

        a.next = c;
        a.prev = b;
        b.next = a;
        b.prev = c;
        c.next = b;
        c.prev = a;
        b = c;
        boolean skip = false;
        c = c.insertNext;
        while(c != null) {
            log.info("infox");
            log.info("new round");
            if (!skip) {
                Node n = a;
                Node nearestNode = n;
                double nearestDist = Double.MAX_VALUE;
                do {
                    double dist_n = distance(n.val);
                    if (dist_n < nearestDist) {
                        nearestDist = dist_n;
                        nearestNode = n;
                    }
                    n = n.next;
                } while (n != a);
                a = nearestNode;
                b = nearestNode.next;
                skip = false;
            }

            place(a.val, b.val, c.val);
            int isect = 0;
            Node j = b.next;
            Node k = a.prev;
            double sj = b.val.getRadius(); //+ b.val.getBorderWidth();
            double sk = a.val.getRadius();// + a.val.getBorderWidth();
            do {
                if (sj <= sk) {
                    if (intersects(j.val, c.val)) {
                        splice(a,j);
                        b = j;
                        skip = true;
                        isect = 1;
                        break;
                    }
                    sj += j.val.getRadius();// + j.val.getBorderWidth();
                    j = j.next;
                } else {
                    if(intersects(k.val, c.val)) {
                        splice(k, b);
                        a = k;
                        skip = true;
                        isect = 1;
                        break;
                    }
                    sk += k.val.getRadius();// + k.val.getBorderWidth();
                    k = k.prev;
                }
            } while (j != k.next);

            if(isect == 0) {
                insert(a, c);
                b = c;
             //   bound(c, topright, bottemleft);
                skip = false;
                c = c.insertNext;
            }
        }

        //return a;
    }

    private double distance(Disk a) {
        return Math.sqrt(a.getPosition().x * a.getPosition().x + a.getPosition().y * a.getPosition().y);
    }

    private void place(Disk a, Disk b, Disk c) {
        double da = b.getRadius() + c.getRadius();
        double db = a.getRadius() + c.getRadius();
        double dx = b.getPosition().x - a.getPosition().x;
        double dy = b.getPosition().y - a.getPosition().y;
        double dc = Math.sqrt(dx * dx + dy * dy);
        if(dc > 0.0) {
            double cos = (db * db + dc*dc - da * da) / (2 * db * dc);
            double theta = Math.acos(cos);
            double x = cos * db;
            double h = Math.sin(theta) * db;
            if(Double.isNaN(h)) {
                h = 0;
            }
            dx = dx / dc;
            log.info("db: " + db);
            log.info("da: " + da);
            log.info("cos: " + cos);
            log.info("dx:" + dx);
            log.info("dc:" + dc);
            dy = dy/ dc;
            c.getPosition().x = a.getPosition().x + x * dx + h * dy;
            log.info("239-a: " + a.getPosition().x);
            log.info("239-x: " + x);
            log.info("239-dx: " + dx);
            log.info("239-h: " + h);
            log.info("239-dy: " + dy);
            log.info("239: " + c.getPosition().x);
            c.getPosition().y = a.getPosition().y + x * dy - h * dx;
        } else {
            c.getPosition().x = a.getPosition().x + db;
            log.info("243: " + c.getPosition().x);
            c.getPosition().y = a.getPosition().y;
        }
    }

    private boolean intersects(Disk a, Disk b) {
     //   if(a == null || b == null) return false;
        double dx = b.getPosition().x - a.getPosition().x;
        double dy = b.getPosition().y - a.getPosition().y;
        double dr = a.getRadius() + b.getRadius();
        if (dr * dr - 1e-6 > dx * dx + dy * dy) {
            return true;
        } else {
            return false;
        }
    }

    private void splice(Node a, Node b) {
        a.next = b;
        b.prev = a;
    }

    private void insert(Node a, Node b) {
        Node c = a.next;
        a.next = b;
        b.prev = a;
        b.next = c;
        if(c != null) c.prev = b;
    }
}
