package com.xtremand.ai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class PollinationsService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String generateText(String prompt) {
        try {
            // Encode the prompt for safe URL usage
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://text.pollinations.ai/" + encodedPrompt))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Pollinations API returned error: " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            return "Exception: " + e.getMessage();
        }
    }
}
