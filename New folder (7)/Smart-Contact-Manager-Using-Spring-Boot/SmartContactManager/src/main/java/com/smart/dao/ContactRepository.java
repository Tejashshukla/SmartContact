package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smart.entities.Contact;

public interface ContactRepository extends MongoRepository<Contact, String> {
    
    @Query(value = "{ 'userId' : ?0 }")
    Page<Contact> findByUserId(String userId, Pageable pageable);
    
    @Query("{ 'email' : ?0 }")
    Contact findByEmail(String email);
    
    @Query("{ 'name' : { $regex: ?0, $options: 'i' }, 'userId' : ?1 }")
    List<Contact> findByNameContainingAndUser(String name, String userId);
    
    @Query(value = "{ 'userId' : ?0 }", count = true)
    long countByUserId(String userId);
}
