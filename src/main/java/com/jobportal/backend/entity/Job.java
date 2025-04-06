package com.jobportal.backend.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Document(collection = "jobs")  // MongoDB collection
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EnableMongoAuditing
public class Job {

    @Id
    private ObjectId id;  // MongoDB's ObjectId

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("requirements")
    private List<String> requirements;

    @Field("salary")
    private Double salary;

    @Field("experience")
    private Integer experience;

    @Field("location")
    private String location;

    @Field("jobType")
    private String jobType;

    @Field("position")
    private String position;

    @DBRef  // Reference to another document
    private Company company;

    @DBRef  // Reference to the User document
    private User createdBy;
    @CreatedDate
    private Instant createdAt;

    @DBRef(lazy = true)  // Lazy loading for applications
    private List<Application> applications;
}
