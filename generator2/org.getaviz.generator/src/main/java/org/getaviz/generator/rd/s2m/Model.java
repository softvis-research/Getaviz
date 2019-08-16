package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class Model {

    private String time;
    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;
    private long id;
    private ArrayList<RDElement> RDElementsList = new ArrayList<>();

    Model(boolean methodTypeMode, boolean methodDisks, boolean dataDisks) {
        this.time = new GregorianCalendar().getTime().toString();
        this.methodTypeMode = methodTypeMode;
        this.methodDisks = methodDisks;
        this.dataDisks = dataDisks;
    }

    private void createModel(DatabaseConnector connector) {
        long id = connector.addNode(
                String.format(
                        "CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', " +
                                "method_disks: \'%s\', data_disks:\'%s\'})",
                        time, methodTypeMode, methodDisks, dataDisks),
                "m").id();
        setID(id);
    }

    void writeToDatabase(DatabaseConnector connector) {
        createModel(connector);
        RDElementsList.forEach(p -> {
            if (p.getParentVisualizedNodeID() == -1) {
                p.setParentID(this.id);
                writeNodes(p, connector);
            } else {
                long visualizedNodeID = p.getParentVisualizedNodeID();
                long parentID = searchForParentID(visualizedNodeID);
                p.setParentID(parentID);
                writeNodes(p, connector);
            }
        });
    }

    private long searchForParentID(long visualizedNodeID) {
        Iterator<RDElement> iterator = RDElementsList.iterator();
        long id = 0;
        while ((iterator.hasNext())) {
            RDElement disk = iterator.next();
            if (visualizedNodeID == disk.getVisualizedNodeID()) {
                id = disk.getId();
                return id;
            }
        }
        return id;
    }

    void addRDElement(RDElement element) {
        RDElementsList.add(element);
    }

    private void setID (long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    private void writeNodes(RDElement p, DatabaseConnector connector) { p.writeToDatabase(connector, "JQA2RD"); }
}
