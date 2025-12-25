package com.jobportal.backend.controllers;

import com.jobportal.backend.dto.QueryRequest;
import com.jobportal.backend.services.GeminiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/career-advice")
public class CareerAdviceController {

    private final GeminiService geminiService;

    public CareerAdviceController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    private static final String CAREER_PROMPT = """
        You are an AI career advisor inside a job portal application built for students and early-career professionals.

        Your role is to provide practical, realistic, and actionable career advice related to jobs, internships, skills,
        resumes, interviews, and career planning.

        Assume the user is a student or fresher unless stated otherwise.

        Rules:
        - Be specific and structured
        - Avoid generic motivation
        - Avoid hallucinating company-specific facts
        - Do not suggest unethical shortcuts
        - Prefer step-by-step guidance

        User query:
        %s
        """;

    @PostMapping
    public ResponseEntity<?> getAdvice(@RequestBody QueryRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "message", "Unauthorized access",
                            "success", false
                    )
            );
        }

        String finalPrompt = String.format(
                CAREER_PROMPT,
                request.getQuery()
        );

        String reply;
        try {
            reply = geminiService.getAdvice(finalPrompt);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of(
                            "message", "AI service temporarily unavailable",
                            "success", false
                    )
            );
        }

        if (reply == null || reply.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                            "message", "Failed to generate career advice",
                            "success", false
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Reply generated successfully",
                        "reply", reply,
                        "success", true
                )
        );
    }
}