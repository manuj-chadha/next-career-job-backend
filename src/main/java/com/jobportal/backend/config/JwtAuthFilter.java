package com.jobportal.backend.config;

import com.jobportal.backend.services.JwtService;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.config.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        logger.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No valid Authorization header found.");
            chain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
        logger.info("Extracted JWT Token: {}", jwtToken);

        try {
            // Check if the Token is Blacklisted
            if (jwtService.isTokenBlacklisted(jwtToken)) {
                logger.warn("Token is blacklisted: {}", jwtToken);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is invalidated");
                return;
            }

            userEmail = jwtService.extractEmail(jwtToken);
            logger.info("Extracted email from JWT: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                logger.info("Loaded UserDetails for: {}", userDetails.getUsername());

                if (userDetails instanceof CustomUserDetails customUserDetails) {
                    User user = customUserDetails.getUser();

                    // Validate the token before authenticating the user
                    if (jwtService.validateToken(jwtToken, user)) {
                        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in the security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("User authenticated successfully: {}", userEmail);
                    } else {
                        logger.warn("JWT validation failed for user: {}", userEmail);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            logger.error("Token expired for user: {}", e.getClaims().getSubject());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Expired");
            return;
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return;
        } catch (Exception e) {
            logger.error("Unexpected error during JWT processing: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Authentication Error");
            return;
        }

        chain.doFilter(request, response);
    }
}