package org.getaviz.jqassistant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
public class JQAssistantController {

    static JQAssistantService service = new JQAssistantService();

    @PostMapping("/server/start")
    public void startServer() {
        service.startServer();
    }

    @PostMapping("/server/stop")
    public void stopServer() {
        JQAssistantService.stopServer();
    }

    @GetMapping("/server/isRunning")
    public boolean isRunning() {
        return JQAssistantService.isRunning();
    }

    @PostMapping("/scan/{url}")
    public void startScan (@PathVariable String url) {
        JQAssistantService service = new JQAssistantService();
        service.scan(decodeUrl(url));
    }

    public static String decodeUrl(String encodedUrl) {
        try {
            return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
