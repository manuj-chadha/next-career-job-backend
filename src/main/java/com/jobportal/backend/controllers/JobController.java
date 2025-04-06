package com.jobportal.backend.controllers;

import com.jobportal.backend.dto.JobDto;
import com.jobportal.backend.dto.ReturnJobDto;
import com.jobportal.backend.entity.Job;
import com.jobportal.backend.services.JobService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@CrossOrigin
public class JobController {

    private final JobService jobService;

    // Admin posts a job
    @PostMapping("/post")
    public ResponseEntity<?> postJob(@RequestBody JobDto jobDto) {
        try {
            var authentication=SecurityContextHolder.getContext().getAuthentication();
            Job job = jobService.createJob(jobDto, authentication.getName());

            ReturnJobDto dto= ReturnJobDto.builder()
                        .id(job.getId().toHexString()) // Convert ObjectId to String
                        .title(job.getTitle())
                        .description(job.getDescription())
                        .requirements(job.getRequirements())
                        .salary(job.getSalary())
                        .location(job.getLocation())
                        .jobType(job.getJobType())
                        .experience(job.getExperience())
                        .position(job.getPosition())
                        .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "New job created successfully.",
                    "job", dto,
                    "success", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error creating job.",
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateJob(@PathVariable String id, @RequestBody JobDto jobDto) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            Job updatedJob = jobService.updateJob(id, jobDto, authentication.getName());

            ReturnJobDto dto = ReturnJobDto.builder()
                    .id(updatedJob.getId().toHexString()) // Convert ObjectId to String
                    .title(updatedJob.getTitle())
                    .description(updatedJob.getDescription())
                    .requirements(updatedJob.getRequirements())
                    .salary(updatedJob.getSalary())
                    .location(updatedJob.getLocation())
                    .jobType(updatedJob.getJobType())
                    .experience(updatedJob.getExperience())
                    .position(updatedJob.getPosition())
                    .build();

            return ResponseEntity.ok(Map.of(
                    "message", "Job updated successfully.",
                    "job", dto,
                    "success", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error updating job.",
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }



    // Get all jobs for students with optional keyword search
    @GetMapping("/get")
    public ResponseEntity<?> getAllJobs(@RequestParam(required = false) String keyword) {
        try {
            List<ReturnJobDto> jobs = jobService.getAllJobs(keyword);
            return ResponseEntity.ok(Map.of(
                    "jobs", jobs,
                    "success", true
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Jobs not found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching jobs.",
                    "success", false
            ));
        }
    }


    // Get job by ID for students
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getJobById(@PathVariable String id) {
        try {
            Job job = jobService.getJobById(id);

            return ResponseEntity.ok(Map.of(
                    "job", ReturnJobDto.fromEntity(job),
                    "success", true
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Job not found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching job.",
                    "success", false
            ));
        }
    }


    // Admin: Get jobs created by the logged-in admin
    @GetMapping("/getadminjobs")
    public ResponseEntity<?> getAdminJobs() {
        try {
            var authentication= SecurityContextHolder.getContext().getAuthentication();
            List<ReturnJobDto> jobs = jobService.getAdminJobs(authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "jobs", jobs,
                    "success", true
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Jobs not found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching jobs.",
                    "success", false
            ));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable ObjectId id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Job deleted successfully.",
                    "success", true
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Job not found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error deleting job.",
                    "success", false
            ));
        }
    }


}
