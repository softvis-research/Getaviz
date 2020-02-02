package org.getaviz.generator.extract;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;
import java.io.IOException;

class ScanStep implements Step {

    private Log log = LogFactory.getLog(this.getClass());
    private Runtime runtime = Runtime.getRuntime();
    private String inputFiles;
    private String pathJQAssistant = "/opt/jqassistant/bin/jqassistant.sh";
    private boolean skipScan;

    ScanStep(String inputFiles, boolean skipScan) {
        this.inputFiles = inputFiles;
        this.skipScan = skipScan;
    }

    public boolean checkRequirements() {
        return !skipScan;
    }

    public void run() {
        if(checkRequirements()) {
            log.info("jQAssistant scan started.");
            log.info("Scanning from URI(s) " + inputFiles);
            try {
                String options = "scan -reset -u " + inputFiles + " -storeUri " +  DatabaseConnector.getDatabaseURL();
                Process pScan = runtime.exec(pathJQAssistant + " " + options);
                pScan.waitFor();
            } catch (IOException | InterruptedException e) {
                log.error(e);
                e.printStackTrace();
            }
            log.info("jQAssistant scan finished.");
        } else {
            log.info("Requirements for step not fulfilled");
        }
    }

    void setPathJQAssistant(String path) {
        this.pathJQAssistant = path;
    }
}
