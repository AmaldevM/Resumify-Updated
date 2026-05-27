package com.example.ResumeParser.dto;

import java.util.List;

public class Resumefilterrequest {
    private Long userId;
    private List<String> skills;
    private double minYearsOfExperience;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public double getMinYearsOfExperience() {
        return minYearsOfExperience;
    }

    public void setMinYearsOfExperience(double minYearsOfExperience) {
        this.minYearsOfExperience = minYearsOfExperience;
    }
}

