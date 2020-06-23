package org.getaviz.jqassistant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.*;

@RestController
public class JQAssistantController {
    private final Log log = LogFactory.getLog(this.getClass());
    private final Runtime runtime = Runtime.getRuntime();
    private final String pathJQAssistant = "/opt/jqassistant/bin/jqassistant.sh";
    private static Process serverProcess = null;

    @PostMapping("/server/start")
    public void startServer() {
        log.info("Starting jQAssistant server");
        try {
            if(serverProcess == null) {
                serverProcess = runtime.exec(pathJQAssistant + " server -embeddedListenAddress 0.0.0.0");
            }
        } catch (IOException e) {
            log.error(e);
            e.printStackTrace();
        }
        log.info("Starting jQAssistant server finished.");
    }

    @PostMapping("/server/stop")
    public void stopServer() {
        log.info("Stopping jQAssistant server");
        if(serverProcess != null) {
            serverProcess.destroy();
            serverProcess = null;
        }
        log.info("jQAssistant server stopped.");
    }

    @PostMapping("/scan/{url}")
    public void startScan (@PathVariable String url) {
        log.info("jQAssistant scan started.");
        log.info("Scanning from URI(s) " + decodeUrl(url));
        try {
            stopServer();
            String options = "scan -reset -u " + decodeUrl(url);
            Process pScan = runtime.exec(pathJQAssistant + " " + options);
            pScan.waitFor();
            startServer();
        } catch (IOException | InterruptedException e) {
            log.error(e);
            e.printStackTrace();
        }
        log.info("jQAssistant scan finished.");
    }

    public static String decodeUrl(String encodedUrl) {
        try {
            return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
