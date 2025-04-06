package com.jobportal.backend.dto;
import com.jobportal.backend.entity.Application;
import com.jobportal.backend.entity.Company;
import com.jobportal.backend.entity.Job;
import com.jobportal.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnJobDto {
    private String id;
    private String title;
    private String description;
    private List<String> requirements;
    private Double salary;
    private String location;
    private String jobType;
    private int experience;
    private String position;
    private Company company;
    private User createdBy;
    private Instant createdAt;
    private List<ApplicationDTO> applications;

    public static ReturnJobDto fromEntity(Job job) {
        return ReturnJobDto.builder()
                .id(job.getId().toHexString()) // Convert ObjectId to String
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .salary(job.getSalary())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .experience(job.getExperience())
                .position(job.getPosition())
                .company(job.getCompany())
                .createdBy(job.getCreatedBy())
                .createdAt(job.getCreatedAt())
                .applications(job.getApplications().stream().map(ApplicationDTO::fromEntity
                ).collect(Collectors.toList()))
                .build();
    }
}

