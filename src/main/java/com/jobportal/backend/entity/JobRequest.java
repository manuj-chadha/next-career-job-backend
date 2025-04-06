package com.jobportal.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "jobs")  // MongoDB collection
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("requirements")
    private List<String> requirements;

    @Field("salary")
    private Double salary;

    @Field("experienceLevel")
    private Integer experienceLevel;

    @Field("location")
    private String location;

    @Field("jobType")
    private String jobType;

    @Field("position")
    private String position;

    @DBRef  // Reference to another document
    private Company company;

    @DBRef  // Reference to the User document
    private ObjectId createdBy;

    @DBRef(lazy = true)  // Lazy loading for applications
    private List<Application> applications;
}
