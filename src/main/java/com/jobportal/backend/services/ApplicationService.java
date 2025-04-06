package com.jobportal.backend.services;

import com.jobportal.backend.dto.ApplicantDTO;
import com.jobportal.backend.dto.ApplicationDTO;
import com.jobportal.backend.dto.JobWithApplicantsDTO;
import com.jobportal.backend.entity.Application;
import com.jobportal.backend.entity.Job;
import com.jobportal.backend.entity.User;
import com.jobportal.backend.repositories.ApplicationRepository;
import com.jobportal.backend.repositories.JobRepository;
import com.jobportal.backend.repositories.UserRepo;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.bson.types.ObjectId;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationService {
    private final EmailService emailService;

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepo userRepository;

    public void applyJob(ObjectId jobId, String email) throws DuplicateRequestException {
        // Check if Job exists
        Optional<Job> jobOptional = jobRepository.findById(jobId);
        if (jobOptional.isEmpty()) {
            throw new RuntimeException("Job not found.");
        }

        // Check if User exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        Job job = jobOptional.get();
        User user = userOptional.get();

        // Check if user has already applied
        if (applicationRepository.existsByJobIdAndApplicantId(jobId, user.getId())) {
            throw new DuplicateRequestException("You have already applied for this job.");
        }

        // Create new Application
        Application application = Application.builder()
                .job(job)
                .applicant(user)
                .status(Application.Status.PENDING) // Default status
                .build();
        System.out.println(job.getApplications());
        applicationRepository.save(application);

        job.getApplications().add(application);
        jobRepository.save(job);
    }

    public List<Application> getAppliedJobs(String email) {
        try {
            User user=userRepository.findByEmail(email).orElse(null);
            if(user==null) throw new UsernameNotFoundException("User not present.");
            return applicationRepository.findByApplicantId(user.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching applied jobs.", e);
        }
    }

    public JobWithApplicantsDTO getApplicants(String jobId, String email) {
        User user=userRepository.findByEmail(email).orElse(null);
        if(user==null) throw new UsernameNotFoundException("User not found.");
        Job job = jobRepository.findById(new ObjectId(jobId))
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));

        List<Application> applications = applicationRepository.findByJobId(job.getId());

        if (applications.isEmpty()) {
            throw new RuntimeException("No applicants found for Job ID: " + jobId);
        }

        List<ApplicantDTO> applicants =applications.stream().map(ApplicantDTO::fromEntity).collect(Collectors.toList());

        return new JobWithApplicantsDTO(job.getId().toString(), job.getTitle(), applicants);
    }

    public void updateStatus(ObjectId applicationId, String status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        // Validate status
        if (!EnumUtils.isValidEnum(Application.Status.class, status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status value. Allowed values: PENDING, ACCEPTED, REJECTED.");
        }

        // Update status
        application.setStatus(Application.Status.valueOf(status.toUpperCase()));
        applicationRepository.save(application);

        emailService.sendStatusEmail(application.getApplicant().getEmail(), application.getApplicant().getFullname(), application.getStatus().toString());

    }

}
