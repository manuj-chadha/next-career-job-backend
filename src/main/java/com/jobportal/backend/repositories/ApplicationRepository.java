package com.jobportal.backend.repositories;

import com.jobportal.backend.entity.Application;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, ObjectId> {
    boolean existsByJobIdAndApplicantId(ObjectId jobId, ObjectId userId);

    @Query("{ 'applicant.$id': ?0 }")
    List<Application> findByApplicantId(ObjectId id);


    List<Application> findByJobId(ObjectId id);
}