package com.jobportal.backend.repositories;

import com.jobportal.backend.entity.Job;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, ObjectId> {
    List<Job> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keyword, String keyword1);

    List<Job> findByCreatedBy(ObjectId adminId);
}
