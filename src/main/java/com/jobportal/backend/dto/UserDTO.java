package com.jobportal.backend.dto;

import com.jobportal.backend.entity.Profile;
import com.jobportal.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private String id;  // ObjectId as String
    private String fullname;
    private String email;
    private String phoneNumber;
    private String role;
    private Profile profile;
    private boolean active; // Include active status

    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getId().toHexString(), // Convert ObjectId to String
                user.getFullname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getProfile(),
                user.isActive()
        );
    }
}
