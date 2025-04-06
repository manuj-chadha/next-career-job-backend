package com.jobportal.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {
    private String bio;
    private List<String> skills;
    private String resume;
    private String resumeOriginalName;

    private Company company;

    private String profilePhoto = "";
}
