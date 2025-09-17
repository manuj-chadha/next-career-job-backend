package com.jobportal.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.backend.config.CustomUserDetails;
import com.jobportal.backend.dto.*;
import com.jobportal.backend.entity.Job;
import com.jobportal.backend.entity.Profile;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.repositories.UserRepo;
import com.jobportal.backend.utils.CloudinaryService;
import com.jobportal.backend.utils.ResumeParser;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final CloudinaryService cloudinaryService;
    private final JwtService jwtService;
    private final UserRepo userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ResumeParser resumeParser;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;


    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
    public User registerUser(RegisterRequest registerRequest) throws Exception {
        String fullname = registerRequest.getFullname();
        String email = registerRequest.getEmail();
        String phoneNumber = registerRequest.getPhoneNumber();
        String password = registerRequest.getPassword();
        String role = registerRequest.getRole();
        MultipartFile resumeFile = registerRequest.getFile(); // renamed for clarity

        try {
            if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Fullname, email, or password cannot be empty");
            }

            if (existsByEmail(email)) {
                throw new IllegalStateException("User with this email already exists");
            }

            String hashedPassword = passwordEncoder.encode(password);

            // Upload profile photo if present
            String profilePhotoUrl = "";
            if (registerRequest.getFile() != null && !registerRequest.getFile().isEmpty()) {
                profilePhotoUrl = cloudinaryService.uploadFile(registerRequest.getFile());
            }

            String resumeUrl = "";
            String resumeText = "";
            String resumeOriginalName = "";
            if (resumeFile != null && !resumeFile.isEmpty()) {
                resumeOriginalName = resumeFile.getOriginalFilename();
                resumeUrl = cloudinaryService.uploadPdf(resumeFile); // upload to Cloudinary
                resumeText = resumeParser.parse(resumeFile);         // parse text
            }

            Profile profile = Profile.builder()
                    .profilePhoto(profilePhotoUrl)
                    .bio("")
                    .skills(new ArrayList<>())
                    .resume(resumeUrl)
                    .resumeOriginalName(resumeOriginalName)
                    .resumeText(resumeText)
                    .build();

            User user = User.builder()
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
            throw new Exception("Validation error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("File upload failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("An unexpected error occurred: " + e.getMessage(), e);
        }
    }


    private void saveUser(User user) {
        userRepository.save(user);
    }

    public AuthResponse loginUser(String email, String password, String role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Incorrect email or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect email or password.");
        }

        if (!role.equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("Incorrect role specified.");
        }

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, UserDTO.fromUser(user));
    }

    public User updateUser(UpdateUserRequest updateRequest) throws Exception {
        User user = userRepository.findByEmail(updateRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            user.setProfile(profile);
        }

        try {
            // ----- Update basic user fields -----
            if (updateRequest.getFullname() != null && !updateRequest.getFullname().isEmpty()) {
                user.setFullname(updateRequest.getFullname());
            }
            if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().isEmpty()) {
                user.setPhoneNumber(updateRequest.getPhoneNumber());
            }

            // ----- Update profile fields -----
            if (updateRequest.getBio() != null) profile.setBio(updateRequest.getBio());
            if (updateRequest.getSkills() != null) profile.setSkills(updateRequest.getSkills());

            String resumeText = null;

            // ----- Resume MultipartFile -----
            if (updateRequest.getResume() != null && !updateRequest.getResume().isEmpty()) {
                MultipartFile resumeFile = updateRequest.getResume();
                profile.setResumeOriginalName(resumeFile.getOriginalFilename());

                // Upload to Cloudinary
                String resumeUrl = cloudinaryService.uploadPdf(resumeFile);
                profile.setResume(resumeUrl);

                // Parse resume text
                resumeText = resumeParser.parse(resumeFile);
                profile.setResumeText(resumeText);

                // Auto-fill profile and user fields via Gemini
                autoFillFromResumeText(user, profile, resumeText);

            }
            // ----- Resume URL -----
            else if (updateRequest.getResumeUrl() != null && !updateRequest.getResumeUrl().isEmpty()) {
                profile.setResume(updateRequest.getResumeUrl());
                profile.setResumeOriginalName("from_url");

                // Parse resume text from URL
                resumeText = resumeParser.parseFromUrl(updateRequest.getResumeUrl());
                profile.setResumeText(resumeText);

                // Auto-fill profile and user fields via Gemini
                autoFillFromResumeText(user, profile, resumeText);
            }

            // ----- Save user -----
            return userRepository.save(user);

        } catch (IOException e) {
            throw new Exception("File upload failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("Error updating user: " + e.getMessage(), e);
        }
    }

    private String cleanGeminiJson(String raw) {
        if (raw == null) return "";

        return raw.replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();
    }

    private void autoFillFromResumeText(User user, Profile profile, String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) return;

        try {
            String geminiJson = geminiService.getStructuredProfile(resumeText);
            geminiJson=cleanGeminiJson(geminiJson);
            GeminiProfileData data = objectMapper.readValue(geminiJson, GeminiProfileData.class);

            if (data.getFullname() != null && !data.getFullname().isEmpty()) user.setFullname(data.getFullname());
            if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) user.setPhoneNumber(data.getPhoneNumber());

            if (data.getSkills() != null && !data.getSkills().isEmpty()) profile.setSkills(data.getSkills());
            if (data.getBio() != null && !data.getBio().isEmpty()) profile.setBio(data.getBio());
            if (data.getExperience() != null) profile.setExperience(data.getExperience());
            if (data.getEducation() != null) profile.setEducation(data.getEducation());

        } catch (Exception ignored) {
            System.out.println(ignored.getMessage());
            throw new RuntimeException(ignored.getMessage());
        }
    }





    public boolean existsByEmail(String email) {
        User user=userRepository.findByEmail(email).orElse(null);
        if(user==null) return false;
        return true;
    }

    public User updatePicture(MultipartFile file, String username) throws Exception {
        User user=findByEmail(username);
        if(user==null) throw new UsernameNotFoundException("User unauthorised.");

        try {
            Profile profile = user.getProfile();
            if (profile == null) {
                profile = new Profile();
            }
            if(!file.isEmpty()){
                String profileUrl = cloudinaryService.uploadFile(file);
                profile.setProfilePhoto(profileUrl);
            }
            user.setProfile(profile);
            saveUser(user);

            return user;
        } catch (IOException e) {
            throw new Exception("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error updating user: " + e.getMessage());
        }
    }

    public UserDTO saveJob(ObjectId jobId, CustomUserDetails userDetails) {
        User user = findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("Invalid access");
        }
        if (user.getSavedJobs() == null) {
            user.setSavedJobs(new ArrayList<>());
        }
        List<ObjectId> savedJobs = user.getSavedJobs();
        if (!savedJobs.contains(jobId)) {
            savedJobs.add(jobId);
            saveUser(user);
        }
        return UserDTO.fromUser(user);
    }

}