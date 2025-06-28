package com.jobportal.backend.controllers;

import com.jobportal.backend.config.CustomUserDetails;
import com.jobportal.backend.config.JwtAuthFilter;
import com.jobportal.backend.dto.*;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.services.JwtService;
import com.jobportal.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final UserService userService;
//    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@ModelAttribute RegisterRequest request) {
        try {
            User newUser = userService.registerUser(request);
            if(newUser!=null) return ResponseEntity.ok(newUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword(), loginRequest.getRole());
            System.out.println(authResponse.getUserDTO().getSavedJobs());
            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            // Send error message with status 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Generic fallback for unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Something went wrong, please try again."));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try{
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            jwtService.invalidateToken(token); // Blacklist token

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @PutMapping("/profile/update")
    public ResponseEntity<?> updateUser(@ModelAttribute UpdateUserRequest updateRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Check if the userDetails (current authenticated user) matches the user in the request
            if (!userDetails.getUsername().equals(updateRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only update your own profile"));
            }

            // Proceed with the update
            User updatedUser = userService.updateUser(updateRequest);

            // Response structure
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "User updated successfully",
                    "data", updatedUser,
                    "timestamp", LocalDateTime.now()
            );

            // Log successful update
            logger.info("User profile updated successfully for user: {}", updateRequest.getEmail());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Log error for not found user
            logger.error("User not found for update: {}", updateRequest.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        } catch (Exception e) {
            // Log generic error
            logger.error("Error updating user profile: {}", updateRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid request data"));
        }
    }

    @PutMapping("/profile/picture/update")
    public ResponseEntity<?> updatePic(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Proceed with the update
            User updatedUser = userService.updatePicture(file, userDetails.getUsername());

            // Response structure
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "User updated successfully",
                    "data", updatedUser,
                    "timestamp", LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        } catch (Exception e) {
            // Log generic error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid request data"));
        }
    }

    @GetMapping("/save/{jobId}")
    public ResponseEntity<?> saveJob(@PathVariable String jobId, @AuthenticationPrincipal CustomUserDetails userDetails){
        try {
            UserDTO user=userService.saveJob(new ObjectId(jobId), userDetails);
            System.out.println("UserDTO before sending: " + user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", user
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Something went wrong, please try again."));
        }
    }



}