package com.jobportal.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "applications") // MongoDB collection name
public class Application {

    @Id
    private ObjectId id;  // MongoDB uses String/ObjectId

    @JsonIgnore
    @DBRef
    private Job job;  // Reference to Job document

    @DBRef
    private User applicant;  // Reference to User document

    private Status status = Status.PENDING;  // Enum for application status
    @CreatedDate
    private Instant appliedAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
