package org.getaviz.generator.extract;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.getaviz.generator.Step;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class ScanStep implements Step {
    private final String inputFiles;
    private final boolean skipScan;
    private static Log log = LogFactory.getLog(ScanStep.class);

    ScanStep(String inputFiles, boolean skipScan) {
        this.inputFiles = inputFiles;
        this.skipScan = skipScan;
    }

    public boolean checkRequirements() {
        return !skipScan;
    }

    public void run() {
        if(checkRequirements()) {
            HttpPost post = new HttpPost("http://jqassistant:8080/scan/" + encodeUrl(encodeUrl(inputFiles)));
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)){
                log.info("Status: " + response.getStatusLine().getStatusCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
