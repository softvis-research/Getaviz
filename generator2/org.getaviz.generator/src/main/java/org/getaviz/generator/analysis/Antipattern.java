package org.getaviz.generator.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;


public class Antipattern implements Step {

    private DatabaseConnector connector = DatabaseConnector.getInstance();
    MetricQueries queries = new MetricQueries();
    private Log log = LogFactory.getLog(Antipattern.class);

    public Antipattern() {
    }

    @Override
    public boolean checkRequirements() {
        return true;
    }

    public void run() {
        log.info("Antipattern started");
        queries.accessToForeignData();
        queries.complexityAndNumbOfMethods();
        queries.countMethodsAccessingAttributes();
        queries.accessToOwnData();
        queries.accesToAllData();
        queries.numberOfAccessorMethods();
        queries.numberOfPublicAttributes();
        queries.numberOfPublicMethods();
        queries.foreignDataProviders();
        queries.numberOfAccessedVar();
        godclass();
        featureEnvy();
        dataclass();
        brainmethod();
        brainclass();
        log.info("Antipattern finished");
    }

    public void godclass(){
       connector.executeWrite("MATCH (c:Class) " +
               "WHERE c.cyclo >= 47 AND c.atfd > 4 AND c.nom > 0 " +
               "WITH toFloat(c.maa) / toFLoat(c.nom*((c.nom-1)/2)) AS tcc, c AS c " +
               "WHERE tcc < 0.33 " +
               "SET c.godclass=true");
    }

    /**
     * Anzahl der fremden Klassen auf deren Attribute zugegriffen wird fehlt (optional)
     */
    public void featureEnvy(){
        connector.executeWrite("MATCH (m:Method) " +
                "WITH m AS m " +
                "WHERE (m.atad - m.atod) > 4 AND m.atad > 0 AND m.fdp <= 4 " +
                "WITH toFloat(m.atod) / toFLoat(m.atad) AS laa, m AS m " +
                "WHERE laa < 0.33 " +
                "SET m.featureEnvy = true");
    }

    /**
     * The number of “functional” public methods divided by the total number of public members [Mar02a]
     * Metrik nochmal überprüfen!
     */
    public void dataclass(){
        connector.executeWrite("MATCH (c:Class) " +
                "WITH c AS c " +
                "WHERE c.cyclo < 31 AND (c.noam + c.nopa) > 4 OR c.cyclo < 47 AND (c.noam + c.nopa) > 7 " +
                "WITH toFloat(c.nom - c.noam) / toFloat(c.nopa + c.nopm) AS woc, c AS c " +
                "WHERE woc < 0.33 " +
                "SET c.dataclass = true");
    }

    /**
     * Cyclo cyclo(high)* loc(65)?
     * Maxnesting ist nicht im Datensatz enthalten
     */
    public void brainmethod(){
        connector.executeWrite("MATCH (m:Method) " +
                "WHERE m.effectiveLineCount > 65 AND m.cyclomaticComplexity >= 23 AND m.noav > 7 " +
                "SET m.brainmethod = true");
    }

    public void brainclass(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method) " +
                "WITH c AS c, m AS m, COUNT(m.brainmethod) AS nobm " +
                "WHERE nobm = 1 AND c.loc >= 390 AND c.cyclo >= 94 OR nobm > 1 AND c.loc >= 195 AND c.cyclo >= 47 " +
                "WITH  toFloat(c.maa) / toFLoat(c.nom*((c.nom-1)/2)) AS tcc, c AS c " +
                "WHERE tcc < 0.5 " +
                "SET c.brainclass = true");
    }

}