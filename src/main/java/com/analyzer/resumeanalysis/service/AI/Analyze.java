package com.analyzer.resumeanalysis.service.AI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Analyze {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Value("${gemini.api.key}")
    private String apiKey;

    public JsonNode extractResumeData(String resumeText) {
        String prompt = String.format("""
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "Extract the following fields from the resume below and format the output as a JSON object that would be returned by a function named `extract_resume` with the following schema:\\n\\n{\\n  \\"name\\": string,\\n  \\"experience\\": string,\\n  \\"education\\": string[],\\n  \\"skills\\": string[],\\n  \\"summary\\": string,\\n  \\"contacts\\": [\\n    {\\n      \\"type\\": string,\\n      \\"value\\": string\\n    }\\n  ],\\n  \\"projects\\": [\\n    {\\n      \\"title\\": string,\\n      \\"tech_stack\\": string[],\\n      \\"description\\": string\\n    }\\n  ]\\n}\\n\\nReturn only the raw JSON, do not use markdown or any other formatting.\\n\\nResume:\\n%s"
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
            return getJsonNode(entity);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse resume JSON: " + e.getMessage(), e);
        }
    }

    public JsonNode suggestImprovements(JsonNode analysedData,String jobProfile){
        String prompt = String.format("""
        You are an expert AI resume reviewer.

        Analyze the following resume (in JSON format) and provide targeted improvement suggestions to better align it with the job role: "%s".

        Break the suggestions into the following sections:
        - summary: string
        - skills: string
        - experience: string
        - project: string
        - others: array of strings (e.g., formatting tips, structure changes, missing sections)

        Also, estimate a realistic candidate selection chance (from 0 to 100 percent) based on the current resume.

        Return only valid JSON with this structure:
        {
          "summary": "...",
          "skills": "...",
          "experience": "...",
          "project": "...",
          "others": ["...", "..."],
          "selection_chance_percent": 75
        }

        Resume:
        %s
        """, jobProfile, analysedData.toPrettyString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "%s"
                }
              ]
            }
          ]
        }
        """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            return getJsonNode(entity);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse improvement suggestions JSON: " + e.getMessage(), e);
        }
    }

    private JsonNode getJsonNode(HttpEntity<String> entity) throws JsonProcessingException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
        String jsonString = Objects.requireNonNull(response.getBody())
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText();


        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new RuntimeException("API returned empty or invalid content.");
        }

        jsonString = jsonString.replaceAll("```json", "")
                .replaceAll("```", "")
                .replaceAll("`", "") // Remove any stray backticks
                .trim();

        return objectMapper.readTree(jsonString);
    }
}