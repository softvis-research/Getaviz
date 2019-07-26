package org.getaviz.generator.rd.s2m;

import org.getaviz.generator.database.DatabaseConnector;

import java.util.GregorianCalendar;

public class DBModel {

    private String calendar;
    private boolean methodTypeMode;
    private boolean methodDisks;
    private boolean dataDisks;
    private long id;
    DatabaseConnector connector;

    DBModel(boolean methodTypeMode, boolean methodDisks, boolean dataDisks, DatabaseConnector connector) {
        this.calendar = new GregorianCalendar().getTime().toString();
        this.methodTypeMode = methodTypeMode;
        this.methodDisks = methodDisks;
        this.dataDisks = dataDisks;
        this.connector = connector;
    }

    public void createModel() {
        long id = connector.addNode(
                String.format(
                        "CREATE (m:Model:RD {date: \'%s\'})-[:USED]->(c:Configuration:RD {method_type_mode: \'%s\', " +
                                "method_disks: \'%s\', data_disks:\'%s\'})",
                        calendar, methodTypeMode, methodDisks, dataDisks),
                "m").id();
        setID(id);
    }


    private void setID (long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
