package com.smart.entities;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @NotBlank(message = "Name field is required !!")
    @Size(min = 2, max = 20, message = "Min 2 and max 20 characters are allowed !!")
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    private String role;
    private boolean enable;
    private String imageUrl;
    private String about;
    
    private List<Contact> contacts = new ArrayList<>();
    
    public User() {
        super();
    }

    public User(String name, String email, String password, String role, boolean enable, String imageUrl,
            String about) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enable = enable;
        this.imageUrl = imageUrl;
        this.about = about;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
