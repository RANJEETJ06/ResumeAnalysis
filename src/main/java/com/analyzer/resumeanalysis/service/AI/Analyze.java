package com.analyzer.resumeanalysis.service.AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class Analyze {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Value("${gemini.api.key}")
    private String apiKey;

    public JsonNode extractResumeData(String resumeText) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String prompt = String.format("""
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "Extract the following fields from the resume below and format the output as a JSON object that would be returned by a function named `extract_resume` with the following schema:\\n\\n{\\n  \\"name\\": string,\\n  \\"experience\\": string,\\n  \\"education\\": string[],\\n  \\"skills\\": string[],\\n  \\"summary\\": string\\n}\\n\\nReturn only the raw JSON, do not use markdown or any other formatting.\\n\\nResume:\\n%s"
                    }
                  ]
                }
              ]
            }
            """, resumeText.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(prompt, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

            if (response.getBody() == null) {
                throw new RuntimeException("API returned empty response.");
            }

            String jsonString = response.getBody()
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readTree(jsonString);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse resume JSON: " + e.getMessage(), e);
        }
    }
}