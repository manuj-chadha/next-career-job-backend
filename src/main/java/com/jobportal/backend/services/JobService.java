package com.jobportal.backend.services;

import com.jobportal.backend.dto.JobDto;
import com.jobportal.backend.dto.ReturnJobDto;
import com.jobportal.backend.entity.Company;
import com.jobportal.backend.entity.Job;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.repositories.CompanyRepository;
import com.jobportal.backend.repositories.JobRepository;
import com.jobportal.backend.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JobService  {


    private final JobRepository jobRepository;
    private final UserRepo userRepo;
    private final CompanyRepository companyRepository;

    public List<ReturnJobDto> getAllJobs(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            // Search jobs by title or description using keyword
            return jobRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword).stream()
                    .map(ReturnJobDto::fromEntity)
                    .collect(Collectors.toList());
        }
        // Fetch all jobs if no keyword given
        return jobRepository.findAll().stream()
                .map(ReturnJobDto::fromEntity)
                .collect(Collectors.toList());
    }


    public Job getJobById(String id) {
        return jobRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new NoSuchElementException("Job not found"));
    }

    public Job updateJob(String id, JobDto jobDto, String authenticatedUserEmail) {
        Job job = jobRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));

        // Ensure the authenticated user is the owner of the job before updating
        if (!job.getCreatedBy().getEmail().equals(authenticatedUserEmail)) {
            throw new IllegalArgumentException("You are not authorized to update this job.");
        }

        // Update fields if new values are provided
        if (jobDto.getTitle() != null) job.setTitle(jobDto.getTitle());
        if (jobDto.getDescription() != null) job.setDescription(jobDto.getDescription());
        if (jobDto.getRequirements() != null) job.setRequirements(List.of(jobDto.getRequirements().split(",")));
        if (jobDto.getSalary() != null) job.setSalary(jobDto.getSalary());
        if (jobDto.getLocation() != null) job.setLocation(jobDto.getLocation());
        if (jobDto.getJobType() != null) job.setJobType(jobDto.getJobType());
        if (jobDto.getExperience()>=0) job.setExperience(jobDto.getExperience());
        if (jobDto.getPosition() != null) job.setPosition(jobDto.getPosition());
        // Save and return updated job
        return jobRepository.save(job);
    }


    public void deleteJob(ObjectId id) {
        if (!jobRepository.existsById(id)) {
            throw new NoSuchElementException("Job not found.");
        }
        jobRepository.deleteById(id);
    }

    public Job createJob(JobDto jobDto, String email) {
        if (jobDto.isMissingFields()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        User user=userRepo.findByEmail(email).orElse(null);
        if(user==null) throw new UsernameNotFoundException("User doesn't exist.");

        String website=jobDto.getWebsite();


        Optional<Company> optionalCompany = companyRepository.findByWebsite(jobDto.getWebsite());

        Company company = optionalCompany.orElseThrow(() ->
                new RuntimeException("Company with website " + jobDto.getWebsite() + " not found")
        );


        Job job = Job.builder()
                .title(jobDto.getTitle())
                .description(jobDto.getDescription())
                .requirements(List.of(jobDto.getRequirements().split(","))) // Converting comma-separated string to list
                .salary(jobDto.getSalary()) // Parsing salary to Double
                .location(jobDto.getLocation())
                .jobType(jobDto.getJobType())
                .experience(jobDto.getExperience()) // Parsing experience to Integer
                .position(jobDto.getPosition()) // Parsing position to Integer
                .company(company) // Assuming Company has an ObjectId constructor
                .createdBy(user)
                .applications(new ArrayList<>())
                .build();
        System.out.println(job);

        jobRepository.insert(job);

        return job;
    }



    public List<ReturnJobDto> getAdminJobs(String email) {
        User user=userRepo.findByEmail(email).orElse(null);
        if(user==null) throw new UsernameNotFoundException("Invalid user access.");
        List<Job> jobs = jobRepository.findByCreatedBy(user.getId());

        return jobs.stream()
                .map(ReturnJobDto::fromEntity)
                .collect(Collectors.toList());
    }

}
