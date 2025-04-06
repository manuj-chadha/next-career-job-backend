package com.jobportal.backend.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    private String fullname;

    private String email;

    private String phoneNumber;

    private String password;

//    private Role role;
    private String role;

    private Profile profile;

    @Getter
    private boolean active = true; // Add this field

}

