package com.jobportal.backend.controllers;

import com.jobportal.backend.dto.CompanyDto;
import com.jobportal.backend.dto.CompanyRegister;
import com.jobportal.backend.dto.ReturnCompany;
import com.jobportal.backend.entity.Company;
import com.jobportal.backend.services.CompanyService;
import com.jobportal.backend.utils.CloudinaryService;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@CrossOrigin
public class CompanyController {

    private final CompanyService companyService;
    private final CloudinaryService cloudinaryService;
    @PostMapping("/register")
    public ResponseEntity<?> registerCompany(@RequestBody String companyName) {
        try{
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "User is not authenticated.",
                        "success", false
                ));
            }
            String authenticatedUserEmail = authentication.getName();
            Company company = companyService.registerCompany(companyName, authenticatedUserEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Company registered successfully.",
                    "company", CompanyRegister.fromEntity(company),
                    "success", true
            ));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error registering company.",
                    "error", e.getMessage(),
                    "success", false
            ));
        }

    }
    @PostMapping("/setup/{id}")
    public ResponseEntity<?> registerCompany(@ModelAttribute CompanyDto companyDto) {
        try {
            Company company = companyService.registerCompany(companyDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Company registered successfully.",
                    "company", company,
                    "success", true
            ));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "You can't register the same company.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error registering company.",
                    "error",  e.getMessage(),
                    "success", false
            ));
        }
    }
    @GetMapping("/get")
    public ResponseEntity<?> getCompanies() {
        try {
            // Ensure the user is authenticated by checking the SecurityContextHolder
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "User is not authenticated.",
                        "success", false
                ));
            }

            String authenticatedUserEmail = authentication.getName();

            List<Company> companies = companyService.getCompanies(authenticatedUserEmail);

            // Handle case where no companies are found
            if (companies.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "message", "Companies not found.",
                        "companies", List.of(),
                        "success", true
                ));
            }

            // Return the list of companies if found
            return ResponseEntity.ok(Map.of(
                    "companies", companies.stream()
                                    .map(ReturnCompany::fromEntity)
                                    .collect(Collectors.toList()),
                    "success", true
            ));

        } catch (Exception e) {
            // Handle any other errors that may occur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching companies.",
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable String id) {
        try {
            Company company = companyService.getCompanyById(id).orElse(null);
            if (company == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "Company not found.",
                        "success", false
                ));
            }
            return ResponseEntity.ok(Map.of(
                    "company", ReturnCompany.fromEntity(company),
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error fetching company.",
                    "success", false
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable String id,
                                          @ModelAttribute CompanyDto companyDto) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "User is not authenticated.",
                        "success", false
                ));
            }
            String authenticatedUserEmail = authentication.getName();
            Company updatedCompany = companyService.updateCompany(id, companyDto, authenticatedUserEmail);
            return ResponseEntity.ok(Map.of(
                    "message", "Company information updated.",
                    "success", true,
                    "company", ReturnCompany.fromEntity(updatedCompany)
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Company not found.",
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error updating company.",
                    "success", false
            ));
        }
    }

}


