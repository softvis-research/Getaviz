package org.getaviz.generator.rd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.database.DatabaseConnector;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Model {

    private Log log = LogFactory.getLog(this.getClass());
    private String time;
    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;
    private long id;
    private ArrayList<RDElement> RDElementsList = new ArrayList<>();

    public Model(boolean methodTypeMode, boolean methodDisks, boolean dataDisks) {
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

    public void writeToDatabase(DatabaseConnector connector) {
        createModel(connector);
        for (RDElement p : RDElementsList) {
            if (p.getParentVisualizedNodeID() == -1) {
                p.setParentID(this.id);
                writeNodes(p, connector);
                p.createParentRelationship(connector);
            } else {
                p.createNode(connector);
            }
        }
        for(RDElement p : RDElementsList) {
            if (p.getParentVisualizedNodeID() != -1) {
                long visualizedNodeID = p.getParentVisualizedNodeID();
                long parentID = searchForParentID(visualizedNodeID);
                p.setParentID(parentID);
                p.createParentRelationship(connector);
            }
        }
    }

    private long searchForParentID(long visualizedNodeID) {
        long id = 0;
        for (RDElement element : RDElementsList) {
            if (visualizedNodeID == element.getVisualizedNodeID()) {
                id = element.getID();
                return id;
            }
        }
        return id;
    }

    public void addRDElement(RDElement element) {
        RDElementsList.add(element);
    }

    private void setID (long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    private void writeNodes(RDElement p, DatabaseConnector connector) { p.createNode(connector); }
}
