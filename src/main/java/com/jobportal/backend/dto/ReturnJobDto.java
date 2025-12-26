package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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
    private String companyName;
    private String companyLogo;
    private String companyWebsite;
    private String companyDescription;
    private Instant createdAt;

    public static ReturnJobDto fromEntity(Job job) {
        return ReturnJobDto.builder()
                .id(job.getId().toHexString())   // ObjectId -> String
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .salary(job.getSalary())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .experience(job.getExperience())
                .position(job.getPosition())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .companyLogo(job.getCompany() != null ? job.getCompany().getLogo() : null)
                .companyDescription(job.getCompany() != null ? job.getCompany().getDescription() : null)
                .companyWebsite(job.getCompany() != null ? job.getCompany().getWebsite() : null)
                .createdAt(job.getCreatedAt())
                .build();
    }

}