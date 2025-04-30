package com.smart.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smart.entities.User;

public interface UserRepository extends MongoRepository<User, String> {
    
    @Query("{ 'email' : ?0 }")
    User getUserByUserName(String email);
}
