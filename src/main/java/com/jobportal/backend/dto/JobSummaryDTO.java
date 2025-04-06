package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSummaryDTO {
    private String id;
    private String title;
    private String company;

    public static JobSummaryDTO fromEntity(Job job) {
        if (job == null) return null;

        return JobSummaryDTO.builder()
                .id(job.getId().toHexString())
                .title(job.getTitle())
                .company(job.getCompany() != null ? job.getCompany().getName() : "Unknown")
                .build();
    }
    @Override
    public String toString() {
        return "JobSummaryDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                '}';
    }


}
