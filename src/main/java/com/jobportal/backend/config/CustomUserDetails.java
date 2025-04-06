package com.jobportal.backend.config;

import com.jobportal.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming User has a getRole() method
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())); // Example: ROLE_STUDENT or ROLE_RECRUITER
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Return the actual password stored in the User object
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Return the actual username stored in the User object
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive(); // Example: User activation logic
    }
}
