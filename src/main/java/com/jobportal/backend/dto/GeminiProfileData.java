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
public class GeminiProfileData {
    private String fullname;
    private String email;
    private String phoneNumber;
    private List<String> skills;
    private String bio;
    private List<Experience> experience;
    private List<Education> education;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Experience {
        private String title;
        private String company;
        private String years;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Education {
        private String degree;
        private String college;
        private String year;
    }
}