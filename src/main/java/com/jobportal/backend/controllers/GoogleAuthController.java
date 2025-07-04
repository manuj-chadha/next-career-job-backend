package com.jobportal.backend.controllers;

import com.jobportal.backend.dto.UserDTO;
import com.jobportal.backend.entity.Profile;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.repositories.UserRepo;
import com.jobportal.backend.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.redirect.uri}")
    private String redirectUri;

    @Autowired private RestTemplate restTemplate;
    @Autowired private UserRepo userRepo;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @GetMapping("/callback")
    public ResponseEntity<?> googleCallbackMethod(@RequestParam("code") String code,
                                                  @RequestParam("role") String role) {
        try {
            // Exchange code for access token
            String tokenEndpoint = "https://oauth2.googleapis.com/token";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", code);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("redirect_uri", redirectUri);
            body.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (tokenResponse.getStatusCode() != HttpStatus.OK || !tokenResponse.getBody().containsKey("access_token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to retrieve access token from Google.");
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // Fetch user profile using access token
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );

            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to fetch user info from Google.");
            }

            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");

            // Check if user already exists
            User user = userRepo.findByEmail(email).orElse(null);

            if (user == null) {
                Profile profile = new Profile();
                if (picture != null) profile.setProfilePhoto(picture);

                user = User.builder()
                        .email(email)
                        .fullname(name)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(role.toUpperCase())
                        .profile(profile)
                        .active(true)
                        .build();

                user = userRepo.save(user);
            }

            String jwt = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "user", UserDTO.fromUser(user)
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during Google login: " + e.getMessage());
        }
    }
}