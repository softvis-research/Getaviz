package org.getaviz.generator.extract;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.ProgrammingLanguage;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.StatementResult;
import java.util.ArrayList;
import java.util.List;

public class Importer {
    private static Log log = LogFactory.getLog(Importer.class);
    private ScanStep scanStep;
    private ArrayList<ProgrammingLanguage> languages = new ArrayList<>();

    public Importer(SettingsConfiguration config){
        this.scanStep = new ScanStep(config.getInputFiles(), config.isSkipScan());
    }

    public Importer(String inputFiles) {
        this.scanStep = new ScanStep (inputFiles, false);
    }

    public void run() {
        log.info("Import started");
        scanStep.run();
        log.info("Import finished");
    }

    public List<ProgrammingLanguage> getImportedProgrammingLanguages() {
        if(isJava()) {
            languages.add(ProgrammingLanguage.JAVA);
        }
        if(isC()){
            languages.add(ProgrammingLanguage.C);
        }
        return languages;
    }

    private boolean isC() {
        DatabaseConnector connector = DatabaseConnector.getInstance();
        StatementResult result = connector.executeRead("MATCH (n:C) RETURN n LIMIT 2");
        return result.hasNext();
    }

    private boolean isJava() {
        DatabaseConnector connector = DatabaseConnector.getInstance();
        StatementResult result = connector.executeRead("MATCH (n:Java) RETURN n LIMIT 2");
        return result.hasNext();
    }
}
