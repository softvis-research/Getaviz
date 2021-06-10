package org.getaviz.jqassistant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class JQAssistantService {
    private static final String pathJQAssistant = "/opt/jqassistant/bin/jqassistant.sh";
    private static final Runtime runtime = Runtime.getRuntime();
    private static Process serverProcess;
    private static final Log log = LogFactory.getLog(JQAssistantService.class);

    @Async
    public void startServer() {
        log.info("Starting jQAssistant server");
        try {
            if(serverProcess == null || !serverProcess.isAlive()) {
                serverProcess = runtime.exec(pathJQAssistant + " server -embeddedListenAddress 0.0.0.0 -daomen");
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            log.error(e);
            e.printStackTrace();
        }
        log.info("Starting jQAssistant server finished.");
    }

    public static void stopServer() {
        log.info("Stopping jQAssistant server");
        if(serverProcess.isAlive()) {
            try {
                serverProcess.destroyForcibly().waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("jQAssistant server stopped.");
    }

    @Async
    public void scan(String url) {
        log.info("jQAssistant scan started.");
        log.info("Scanning from URI(s) " + url);
        try {
            stopServer();
            String options = "scan -reset -u " + url;
            Process pScan = runtime.exec(pathJQAssistant + " " + options);
            pScan.waitFor();
            startServer();
        } catch (IOException | InterruptedException e) {
            log.error(e);
            e.printStackTrace();
        }
        log.info("jQAssistant scan finished.");
    }

    public static boolean isRunning() {
        return serverProcess.isAlive();
    }
}
