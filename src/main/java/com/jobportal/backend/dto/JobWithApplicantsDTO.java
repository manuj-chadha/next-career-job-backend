package com.jobportal.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobWithApplicantsDTO {
//    private String applicationId;
    private String jobId;
    private String jobTitle;
    private List<ApplicantDTO> applicants;
}


