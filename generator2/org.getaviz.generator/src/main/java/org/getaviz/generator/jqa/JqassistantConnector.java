package org.getaviz.generator.jqa;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class JqassistantConnector {

    private static String dbname = "jqassistant";

    public void startDatabase() {
        HttpPost post = new HttpPost("http://" + dbname + ":8080/server/start");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
