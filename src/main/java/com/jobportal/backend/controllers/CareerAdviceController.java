package com.jobportal.backend.controllers;

import com.jobportal.backend.dto.QueryRequest;
import com.jobportal.backend.services.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/career-advice")
//@CrossOrigin(origins = "*")
public class CareerAdviceController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping
    public ResponseEntity<?> getAdvice(@RequestBody QueryRequest request) throws IllegalAccessException {
        try{
            var authentication= SecurityContextHolder.getContext().getAuthentication();
            if(authentication==null || !authentication.isAuthenticated()) throw new IllegalAccessException("Unauthroised access.");
            String reply=geminiService.getAdvice(request.getQuery());
            if(reply!=null || !reply.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "message", "Reply generated successfully.",
                        "reply", reply,
                        "success", true
                ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Error generating response",
                    "success", false
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
