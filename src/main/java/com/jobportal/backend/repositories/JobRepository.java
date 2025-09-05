package com.jobportal.backend.repositories;

import com.jobportal.backend.entity.Job;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface JobRepository extends MongoRepository<Job, ObjectId> {

    Page<Job> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String keyword, String keyword1, Pageable pageable
    );

    Page<Job> findAll(Pageable pageable);

    List<Job> findByCreatedBy(ObjectId adminId);
}