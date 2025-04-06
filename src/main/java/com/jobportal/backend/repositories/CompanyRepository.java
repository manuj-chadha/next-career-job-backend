package com.jobportal.backend.repositories;

import com.jobportal.backend.entity.Company;
import com.jobportal.backend.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CompanyRepository extends MongoRepository<Company, ObjectId> {
    List<Company> findByUserId(ObjectId userId);

    boolean existsByNameAndUser(String name, User user);

    Optional<Company> findByWebsite(String email);
}