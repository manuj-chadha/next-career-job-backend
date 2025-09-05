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

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@Document(collection = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "loc_created_idx", def = "{'location': 1, 'createdAt': -1}")
public class Job {

    @Id
    private ObjectId id;

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

    @Indexed   // Single-field index
    @Field("location")
    private String location;

    @Indexed   // Single-field index
    @Field("jobType")
    private String jobType;

    @Field("position")
    private String position;

    @DBRef
    private Company company;

    @DBRef
    private User createdBy;

    @Indexed   // Helps with sorting/pagination
    @CreatedDate
    private Instant createdAt;

    @DBRef(lazy = true)
    private List<Application> applications;
}