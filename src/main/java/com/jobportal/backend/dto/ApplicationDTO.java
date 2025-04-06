package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Application;
import com.jobportal.backend.entity.Job;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private String id;
    private JobSummaryDTO job;
    private String applicantId;
    private String status;
    private Instant appliedAt;
    public static ApplicationDTO fromEntity(Application application){
        return ApplicationDTO.builder()
                .id(application.getId().toHexString())
                .job(application.getJob() != null ? JobSummaryDTO.fromEntity(application.getJob()) : null)
                .applicantId(application.getApplicant().getId().toHexString())
                .status(application.getStatus().toString())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}
