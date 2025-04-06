package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Company;
import com.jobportal.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "companies")  // MongoDB collection name
public class ReturnCompany {

    private String id;

    private String name;

    private String description;

    private String website;

    private String location;

    private String logo;
    private User user;
    private Instant createdAt;

    public static ReturnCompany fromEntity(Company company) {
        if (company == null) {
            return null;
        }

        return ReturnCompany.builder()
                .id(company.getId().toHexString())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .location(company.getLocation())
                .logo(company.getLogo())
                .user(company.getUser())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
