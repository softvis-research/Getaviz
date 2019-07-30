package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class Model {

    private String calendar;
    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;
    private long id;
    private ArrayList<RDElement> RDElementsList = new ArrayList<>();

    Model(boolean methodTypeMode, boolean methodDisks, boolean dataDisks, DatabaseConnector connector) {
        this.calendar = new GregorianCalendar().getTime().toString();
        this.methodTypeMode = methodTypeMode;
        this.methodDisks = methodDisks;
        this.dataDisks = dataDisks;
    }

    private void writeToDataBank(DatabaseConnector connector) {
        long id = connector.addNode(
                String.format(
                        "CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', " +
                                "method_disks: \'%s\', data_disks:\'%s\'})",
                        calendar, methodTypeMode, methodDisks, dataDisks),
                "m").id();
        setID(id);
    }

    void createVisualization(DatabaseConnector connector) {
        writeToDataBank(connector);
        RDElementsList.forEach(p -> {
            if (p.getParentVisualizedNodeID() == -1) {
                p.setParentID(this.id);
                write(p, connector);
            } else {
                long parentID = searchForParentID(p);
                p.setParentID(parentID);
                write(p, connector);
            }
        });
    }

    private long searchForParentID(RDElement element) {
        Iterator<RDElement> iterator = RDElementsList.iterator();
        long id = 0;
        while ((iterator.hasNext())) {
            RDElement disk = iterator.next();
            if (element.getParentVisualizedNodeID() == disk.getVisualizedNodeID()) {
                id = disk.getId();
                return id;
            }
        }
        return id;
    }

    private void write(RDElement p, DatabaseConnector connector) {
        p.writeToDataBank(connector);
    }

    private void setID (long id) {
        this.id = id;
    }

    void setRDElementsList(ArrayList<RDElement> list) {
        this.RDElementsList = list;
    }

    public long getId() {
        return id;
    }
}
