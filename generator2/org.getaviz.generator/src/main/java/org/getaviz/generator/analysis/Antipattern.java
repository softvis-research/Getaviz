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
        godclass();
        featureEnvy();
        dataclass();
        brainmethod();
        brainclass();
        log.info("Antipattern finished");
    }

    public void godclass(){
       connector.executeWrite("MATCH (c:Class) " +
               "WHERE c.cyclo >= 47 AND c.atfd >= 4 AND c.nom > 0 " +
               "WITH toFloat(c.maa) / toFLoat(c.nom) AS tcc, c AS c " +
               "WHERE tcc < 0.33 " +
               "SET c.godclass=true");
    }

    /**
     * Anzahl der fremden Klassen auf deren Attribute zugegriffen wird fehlt (optional)
     */
    public void featureEnvy(){
        connector.executeWrite("MATCH (m:Method)\n" +
                "WITH m AS m\n" +
                "WHERE (m.atad - m.atod) > 4 AND m.atad > 0\n" +
                "WITH toFloat(m.atod) / toFLoat(m.atad) AS laa, m AS m\n" +
                "WHERE laa < 0.33\n" +
                "SET m.featureEnvy = true");
    }

    /**
     * The number of “functional” public methods divided by the total number of public members [Mar02a]
     * Metrik nochmal überprüfen!
     */
    public void dataclass(){
        connector.executeWrite("MATCH (c:Class)\n" +
                "WITH c AS c\n" +
                "WHERE c.cyclo < 31 AND (c.noam + c.nopa) > 4 OR c.cyclo < 47 AND (c.noam + c.nopa) > 7\n" +
                "WITH toFloat(c.nom - c.noam) / toFloat(c.nopa + c.noam) AS woc, c AS c\n" +
                "WHERE woc < 0.33\n" +
                "SET c.dataclass = true");
    }

    /**
     * Cyclo als cyclomatic Complexity, wmc, amw oder cyclo*loc besser?
     * Maxnesting + Nr of Var fehlen! Sind nicht im Datensatz enthalten
     */
    public void brainmethod(){
        connector.executeWrite("MATCH (m:Method)\n" +
                "WHERE m.effectiveLineCount > 65 AND m.cyclomaticComplexity > 31\n" +
                "SET m.brainmethod = true");
    }

    public void brainclass(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method)\n" +
                "WITH c AS c, m AS m, COUNT(m.brainmethod) AS nobm\n" +
                "WHERE nobm = 1 AND c.loc > 390 AND c.cyclo > 92 OR nobm > 1 AND c.loc > 195 AND c.cyclo > 47\n" +
                "WITH  toFloat(c.maa) / toFLoat(c.nom) AS tcc, c AS c\n" +
                "WHERE tcc < 0.5\n" +
                "SET c.brainclass = true");
    }

}