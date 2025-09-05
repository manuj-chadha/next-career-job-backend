package com.jobportal.backend.dto;
import com.jobportal.backend.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobListingDto {
    private String id;
    private String title;
    private String location;
    private Double salary;
    private String jobType;
    private int experience;
    private String companyName;
    private String companyLogo;
    private Instant createdAt;

    public static JobListingDto fromEntity(Job job) {
        return JobListingDto.builder()
                .id(job.getId().toHexString())
                .title(job.getTitle())
                .location(job.getLocation())
                .salary(job.getSalary())
                .jobType(job.getJobType())
                .experience(job.getExperience())
                .companyName(job.getCompany().getName())
                .companyLogo(job.getCompany().getLogo())
                .createdAt(job.getCreatedAt())
                .build();
    }
}