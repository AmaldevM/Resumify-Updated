package com.example.ResumeParser.dto;

import java.util.List;

public class ResumeWithSkillsDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private double yearsOfExperience;
    private List<String> skills;

    public ResumeWithSkillsDTO() {}

    public ResumeWithSkillsDTO(Long id, String name, String phoneNumber, String email, double yearsOfExperience, List<String> skills) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.yearsOfExperience = yearsOfExperience;
        this.skills = skills;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}

