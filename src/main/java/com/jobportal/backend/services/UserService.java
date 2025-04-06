package com.jobportal.backend.services;

import com.jobportal.backend.dto.AuthResponse;
import com.jobportal.backend.dto.RegisterRequest;
import com.jobportal.backend.dto.UpdateUserRequest;
import com.jobportal.backend.dto.UserDTO;
import com.jobportal.backend.entity.Profile;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.repositories.UserRepo;
import com.jobportal.backend.utils.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class UserService {
    private final CloudinaryService cloudinaryService;
    private final JwtService jwtService;
    private final UserRepo userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
    public User registerUser(RegisterRequest registerRequest) throws Exception {
        String fullname=registerRequest.getFullname();
        String email=registerRequest.getEmail();
        String phoneNumber=registerRequest.getPhoneNumber();
        String password=registerRequest.getPassword();
        String role=registerRequest.getRole();
        MultipartFile file= registerRequest.getFile();

        try {
            if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Fullname, email, or password cannot be empty");
            }

            if (existsByEmail(email)) {
                throw new IllegalStateException("User with this email already exists");
            }

            String hashedPassword = passwordEncoder.encode(password);

            String profilePhotoUrl = "";
            if (file != null && !file.isEmpty()) {
                profilePhotoUrl = cloudinaryService.uploadFile(file);
            }
            Profile profile = Profile.builder()
                    .profilePhoto(profilePhotoUrl)
                    .bio("")
                    .skills(new ArrayList<>())
                    .resume("")
                    .resumeOriginalName("")

                    .build();


            User user=User.builder()
                    .fullname(fullname)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .password(hashedPassword)
                    .role(role.toUpperCase())
                    .profile(profile)
                    .build();
            saveUser(user);

            return user;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new Exception("Validation error: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void saveUser(User user) {
        userRepository.save(user);
    }

    public AuthResponse loginUser(String email, String password, String role) {
        User user= userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Incorrect email or password."));
        if (!role.equalsIgnoreCase(user.getRole()))
            throw new IllegalArgumentException("Incorrect role specified.");
        String jwtToken = jwtService.generateToken(user);
        System.out.println(jwtToken);
        return new AuthResponse(jwtToken, UserDTO.fromUser(user));
    }

    public User updateUser(UpdateUserRequest updateRequest) throws Exception {

        User user = userRepository.findByEmail(updateRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            // Update basic fields
            Profile profile = user.getProfile();
            if (profile == null) {
                profile = new Profile();
            }

            // Update email only if provided
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
                user.setEmail(updateRequest.getEmail());
            }

            // Update profile details
            if (updateRequest.getBio() != null) {
                profile.setBio(updateRequest.getBio());
            }
            if (updateRequest.getSkills() != null) {
                profile.setSkills(updateRequest.getSkills());
            }

            if (updateRequest.getResume() != null && !updateRequest.getResume().isEmpty()) {
                profile.setResumeOriginalName(updateRequest.getResume().getOriginalFilename());
                String resumeUrl = cloudinaryService.uploadFile(updateRequest.getResume());
                profile.setResume(resumeUrl);
            }
            else if (updateRequest.getResumeUrl() != null && !updateRequest.getResumeUrl().isEmpty()) {
                profile.setResume(updateRequest.getResumeUrl());
            }

            // Save updated profile
            user.setProfile(profile);
            saveUser(user);

            return user;
        } catch (IOException e) {
            throw new Exception("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error updating user: " + e.getMessage());
        }
    }



    public boolean existsByEmail(String email) {
        User user=userRepository.findByEmail(email).orElse(null);
        if(user==null) return false;
        return true;
    }
}

