package com.jobportal.backend.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EnableMongoAuditing
@Document(collection = "companies")  // MongoDB collection name
public class Company {

    @Id
    private ObjectId id;  // MongoDB uses String/ObjectId by default

    private String name;

    private String description;

    private String website;

    private String location;

    private String logo;  // URL to company logo

    @DBRef
    private User user;  // Reference to the User document
    @CreatedDate
    private Instant createdAt;

}
