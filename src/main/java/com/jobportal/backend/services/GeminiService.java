package com.jobportal.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.backend.dto.GeminiProfileData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Core method to send prompt to Gemini and get text output
     */
    public String getAdvice(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = Map.of("parts", Collections.singletonList(Map.of("text", prompt)));
        Map<String, Object> payload = Map.of("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL + apiKey, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        return extractTextFromResponse(responseBody);
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            var candidates = (java.util.List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";

            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (java.util.List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "";

            return parts.get(0).get("text").toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Generate structured JSON profile from resume text
     */
    public String getStructuredProfile(String resumeText) {
        String prompt = """
                You are an AI assistant that extracts structured information from a candidate's resume.
                Your task is to return ONLY JSON in the exact format specified below.
                Do not include any explanations, extra text, or formatting.

                JSON format:
                {
                  "fullname": "",
                  "email": "",
                  "phoneNumber": "",
                  "skills": [],
                  "bio": "",
                  "experience": [
                    {
                      "title": "",
                      "company": "",
                      "years": ""
                    }
                  ],
                  "education": [
                    {
                      "degree": "",
                      "college": "",
                      "year": ""
                    }
                  ]
                }

                Resume text:
                %s
                """.formatted(resumeText);

        String str=getAdvice(prompt);
        return str;
    }

    public GeminiProfileData extractProfileData(String resumeText) {
        try {
            String jsonString = getStructuredProfile(resumeText);
            // parse JSON safely
            return objectMapper.readValue(jsonString, GeminiProfileData.class);
        } catch (Exception e) {
            // fallback empty object if parsing fails
            return new GeminiProfileData();
        }
    }
}