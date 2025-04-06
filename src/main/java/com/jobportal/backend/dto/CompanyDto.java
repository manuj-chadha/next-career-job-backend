package com.jobportal.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data                               // Generates Getters, Setters, toString(), equals(), and hashCode()
@AllArgsConstructor                 // Generates All-Args Constructor
@NoArgsConstructor                  // Generates No-Args Constructor
public class CompanyDto {
    private String name;
    private String description;
    private String website;
    private String location;
    private MultipartFile logo;
    private String email;
}

