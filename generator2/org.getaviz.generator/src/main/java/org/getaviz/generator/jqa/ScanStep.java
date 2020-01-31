package org.getaviz.generator.jqa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.Step;
import org.getaviz.generator.database.DatabaseConnector;

import java.io.IOException;

public class ScanStep implements Step {

    private Log log = LogFactory.getLog(this.getClass());
    private Runtime runtime = Runtime.getRuntime();
    private String inputFiles;
    private String pathJqassisent;

    ScanStep(SettingsConfiguration config, String pathJqassisent) {
        this.inputFiles = config.getInputFiles();
        this.pathJqassisent = pathJqassisent;
    }

    public ScanStep(SettingsConfiguration config) {
        this(config,"/opt/jqassistant/bin/jqassistant.sh");
    }

    public void run() {
        log.info("jQA scan started.");
        log.info("Scanning from URI(s) " + inputFiles);
        try {
            Process pScan = runtime.exec(pathJqassisent + " scan -reset -u " + inputFiles + " -storeUri " +
                    DatabaseConnector.getDatabaseURL());
            pScan.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error(e);
            e.printStackTrace();
        }
        log.info("jQA scan ended.");
    }

    void setPathJqassisent (String path) {
        this.pathJqassisent = path;
    }
}
