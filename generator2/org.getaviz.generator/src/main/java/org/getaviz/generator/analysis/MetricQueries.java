package org.getaviz.generator.analysis;

import org.getaviz.generator.database.DatabaseConnector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MetricQueries {

    private DatabaseConnector connector = DatabaseConnector.getInstance();


    /**
     * MAA: Methods Accessing Attributes
     * Count Method pairs that access the same attribute inside a class
     * Relevant for Godclass, Brainclass
     */
    public void countMethodsAccessingAttributes() {
        connector.executeWrite("match (c:Class)-[:DECLARES]->(m1:Method), (c)-[:DECLARES]-(m2:Method), " +
                "(c)-[:DECLARES]-(f:Field), (m1)-[:WRITES|READS]->(f), (m2)-[:WRITES|READS]->(f) " +
                "where (not m1.fqn = m2.fqn) " +
                "with distinct m1.fqn + m2.fqn as pair, c as c " +
                "with count(pair) as pairNr, c as c " +
                "set c.maa= pairNr / 2");
    }

    /**
     * ATFD: Access To Foreign Data
     * Number of Attributes a Class accesses that do not belong to it
     * Relevant for Godclass
     */
    public void accessToForeignData() {
        connector.executeWrite("match (c1:Class)-[:DECLARES]->(f:Field), " +
                "(c2:Class)-[:DECLARES]->(m:Method)-[:READS|WRITES]->(f:Field) " +
                "where (NOT c1.fqn = c2.fqn) " +
                "with distinct f as f, c2 as c2 " +
                "with c2 as c2, count(f) as atfd " +
                "set c2.atfd = atfd");
    }

    /**
     * CYCLO: Cyclomatic Complexity
     * NOM: Number Of Methods
     * LOC: Lines Of Code
     * Count NOM, add up CYCLO and LOC from Methods for each Class
     * Relevant for Godclass, Dataclass, Brainclass
     */
    public void complexityAndNumbOfMethods()  {
        connector.executeWrite("match (c:Class)-[:DECLARES]->(m:Method) " +
                "with sum(m.cyclomaticComplexity) as cyclo, count(m.fqn) as nom, sum(m.effectiveLineCount) as loc, c as c " +
                "set c.cyclo=cyclo, c.nom=nom, c.loc=loc ");
    }

    /**
     *  ATOD: Access To Own Data
     *  Number of attributes a method access in its own class
     *  Relevant for Feature Envy
     */
    public void accessToOwnData(){
        connector.executeWrite(
                "MATCH (c:Class)-[:DECLARES]->(m:Method), " +
                "(c)-[:DECLARES]->(f:Field),  " +
                "(m)-[:READS|WRITES]->(f) " +
                "with distinct f as f, m as m " +
                "with m as m, count(m) as atod " +
                "set m.atod = atod");
    }

    /**
     * ATAD: Access To All Data
     * Number of attributes a method access
     * Relevant for Feature Envy
     */
    public void accesToAllData(){
        connector.executeWrite("MATCH (m:Method)-[:READS|:WRITES]->(f:Field) " +
                "with distinct f as f, m as m " +
                "with m as m, count(m) as atad " +
                "set m.atad = atad ");
    }

    /**
     * NOPA: Number Of Public Attributes
     * Count all public attributes a class owns
     * Relevant for Dataclass
     */
    public void numberOfPublicAttributes(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(f:Field) " +
                "WITH c AS c, f AS f " +
                "WHERE f.visibility = \"public\" " +
                "WITH COUNT(f.fqn) AS nopa, c AS c " +
                "SET c.nopa = nopa ");
    }

    /**
     * NOPM: Number Of Public Methods
     * Count all public methods a class owns
     * Relevant for Dataclass
     */
    public void numberOfPublicMethods(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method) " +
                "WITH c AS c, m AS m " +
                "WHERE m.visibility = \"public\" " +
                "WITH COUNT(m) AS nopm, c AS c " +
                "SET c.nopm = nopm ");
    }

    /**
     * NOAM: Number of Accessor Methods
     * Count methods that begin either wit "get" or "set"
     * Relevant for Dataclass
     */
    public void numberOfAccessorMethods(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method) " +
                "WITH c AS c, m AS m " +
                "WHERE m.name STARTS WITH \"get\" OR m.name STARTS WITH \"set\" " +
                "WITH COUNT(m.fqn) AS noam, c AS c " +
                "SET c.noam = noam ");
    }

    /**
     * FDP: Foreign Data Provider
     * Count classes in which accessed attributes are defined
     * Relevant for Feature Envy
     */
    public void foreignDataProviders(){
        connector.executeWrite(
                "match (c1:Class)-[:DECLARES]->(m:Method), " +
                "(c2:Class)-[:DECLARES]->(f:Field)," +
                "(m)-[:READS|WRITES]->(f) " +
                "where (NOT c1.fqn = c2.fqn) " +
                "with distinct c2 as c2, m as m " +
                "with count(c2) as fdp, m as m " +
                "set m.fdp = fdp ");
    }


    /**
     * NOAV: Number of Accessed Variables
     * Count the Number of Variables a Method uses
     * Relevant for Brain Method
     */
    public void numberOfAccessedVar(){
        connector.executeWrite("MATCH (m:Method)-[:DECLARES]->(v:Variable) " +
                "WITH m AS m, COUNT(v.name) AS noav " +
                "SET m.noav = noav");
    }



}