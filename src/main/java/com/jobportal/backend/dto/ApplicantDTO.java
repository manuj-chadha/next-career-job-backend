package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicantDTO {
    private String applicantId;
    private String applicationId;
    private String name;
    private String email;
    private String contact;
    private String resume;
    private String resumeName;
    private Instant appliedAt;

    public static ApplicantDTO fromEntity(Application application){
        return ApplicantDTO.builder()
                .applicantId(application.getApplicant().getId().toHexString())
                .applicationId(application.getId().toHexString())
                .name(application.getApplicant().getFullname())
                .email(application.getApplicant().getEmail())
                .contact(application.getApplicant().getPhoneNumber())
                .resume(application.getApplicant().getProfile().getResume())
                .resumeName(application.getApplicant().getProfile().getResumeOriginalName())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}

