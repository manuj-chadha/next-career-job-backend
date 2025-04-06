package com.jobportal.backend.controllers;

import com.jobportal.backend.dto.ApplicationDTO;
import com.jobportal.backend.dto.JobWithApplicantsDTO;
import com.jobportal.backend.services.ApplicationService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/apply/{jobId}")
    public ResponseEntity<String> applyJob(@PathVariable String jobId) {
        var authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        String email = authentication.getName();

        try {
            applicationService.applyJob(new ObjectId(jobId), email);
            return ResponseEntity.status(HttpStatus.CREATED).body("Job applied successfully.");
        }
        catch (ConfigDataResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (DuplicateRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while applying for the job.");
        }
    }


    @GetMapping("/get")
    public ResponseEntity<?> getAppliedJobs() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalAccessException("User unauthorized.");
            }


            List<ApplicationDTO> applications = applicationService.getAppliedJobs(authentication.getName())
                    .stream()
                    .map(ApplicationDTO::fromEntity)
                    .toList();

            applications
                    .forEach(app -> {
                        System.out.println("Job: " + app.getJob()); // Might be null!
//                        System.out.println("Company: " + (app.getJob() != null ? app.getJob() : "null"));
                    });
            return ResponseEntity.ok(Map.of(
                    "applications", applications,
                    "success", true
            ));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "User unauthorized.",
                    "success", false
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No applied jobs found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching applied jobs.",
                    "success", false
            ));
        }
    }



    @GetMapping("/{jobId}/applicants")
    public ResponseEntity<?> getApplicants(@PathVariable String jobId) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalAccessException("Unauthorized access.");
            }

            // Fetch applicants
            JobWithApplicantsDTO jobWithApplicants = applicationService.getApplicants(jobId, authentication.getName());

            return ResponseEntity.ok(Map.of(
                    "jobWithApplicants", jobWithApplicants,
                    "success", true
            ));

        } catch (ConfigDataResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Job not found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching applicants.",
                    "success", false
            ));
        }
    }


    @PutMapping("/status/{applicationId}/update")
    public ResponseEntity<?> updateStatus(@PathVariable String applicationId, @RequestBody Map<String, String> requestBody) {
        try {
            String status = requestBody.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status is required.");
            }
            applicationService.updateStatus(new ObjectId(applicationId), status);
            return ResponseEntity.ok("Status updated successfully.");
        } catch (ConfigDataResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "An error occurred.",
                    "error", e.getMessage()
            ));
        }
    }

}