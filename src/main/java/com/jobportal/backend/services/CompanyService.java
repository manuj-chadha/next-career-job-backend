package com.jobportal.backend.services;

import com.jobportal.backend.dto.CompanyDto;
import com.jobportal.backend.entity.Company;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.repositories.CompanyRepository;
import com.jobportal.backend.repositories.UserRepo;
import com.jobportal.backend.utils.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {


    private final CompanyRepository companyRepository;

    private final UserRepo userRepository;
    private final CloudinaryService cloudinaryService;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(@PathVariable String companyId) {
        return companyRepository.findById(new ObjectId(companyId));
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(String id, CompanyDto companyDto, String email) throws IOException {
        User user=userRepository.findByEmail(email).orElse(null);
        if(user==null) throw new UsernameNotFoundException("Unauthorised access.");
        Company company = companyRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new NoSuchElementException("Company not found."));

        String logoUrl = cloudinaryService.uploadFile(companyDto.getLogo()); // Replace with your Cloudinary service

        // Update fields
        if (companyDto.getName() != null) company.setName(companyDto.getName());
        if (companyDto.getDescription() != null) company.setDescription(companyDto.getDescription());
        if (companyDto.getWebsite() != null) company.setWebsite(companyDto.getWebsite());
        if (companyDto.getLocation() != null) company.setLocation(companyDto.getLocation());
        company.setLogo(logoUrl);

        return companyRepository.save(company);
    }


    public void deleteCompany(ObjectId id) {
        companyRepository.deleteById(id);
    }

    public Company registerCompany(String companyName, String email){
        User user=userRepository.findByEmail(email).orElse(null);
        if(user==null) throw new UsernameNotFoundException("Unauthorised access.");
        if (companyName == null || companyName.isEmpty()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        Company company=Company.
                builder()
                .name(companyName)
                .user(user)
                .build();
        return companyRepository.save(company);
    }
    public Company registerCompany(CompanyDto companyDto) throws IOException {
        // Validate required fields
        if (companyDto.getName() == null || companyDto.getName().isEmpty()) {
            throw new IllegalArgumentException("Company name is required.");
        }


        // Find user by ID (Assuming you have a UserRepository)
        User user = userRepository.findByEmail(companyDto.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        MultipartFile logo=companyDto.getLogo();
        String logoUrl = "";
        if (logo != null && !logo.isEmpty()) {
            logoUrl = cloudinaryService.uploadFile(logo);
        }
        boolean exists = companyRepository.existsByNameAndUser(companyDto.getName(), user);
        if (exists) {
            throw new DuplicateKeyException("You have already registered this company.");
        }
        // Create a new Company object
        Company company = Company.builder()
                .name(companyDto.getName())
                .description(companyDto.getDescription())
                .website(companyDto.getWebsite())
                .location(companyDto.getLocation())
                .logo(logoUrl)
                .user(user)
                .build();

        // Save company to the database
        return companyRepository.save(company);
    }

    public List<Company> getCompanies(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return companyRepository.findByUserId(user.getId());
    }


}
