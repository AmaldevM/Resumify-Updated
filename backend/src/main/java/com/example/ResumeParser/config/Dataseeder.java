
package com.example.ResumeParser.config;

import com.example.ResumeParser.entity.Knownskill;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.Knownskillrepository;
import com.example.ResumeParser.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Dataseeder {

    @Bean
    CommandLineRunner seedKnownSkills(Knownskillrepository knownSkillRepository) {
        return args -> {
            List<String> skills = List.of(
                "Angular", "AWS", "Azure", "Bootstrap", "c", "c++", "CSS",
                "Django", "Docker", "Express", "Flask", "GCP", "Git", "HTML",
                "Java", "JavaScript", "Jenkins", "Kafka", "Kubernetes", "MongoDB",
                "MySQL", "Node.js", "php", "PostgreSQL", "Python", "RabbitMQ",
                "React", "Redis", "SASS", "Spring", "Spring Boot", "SQL",
                "TypeScript", "Vue"
            );

            for (String skillName : skills) {
                if (!knownSkillRepository.existsByNameIgnoreCase(skillName)) {
                    knownSkillRepository.save(new Knownskill(skillName));
                }
            }
        };
    }

    @Bean
    CommandLineRunner seedDemoUser(UserRepository userRepository) {
        return args -> {
            String demoEmail = "demo@resumify.com";
            if (userRepository.findByEmail(demoEmail).isEmpty()) {
                User demoUser = new User();
                demoUser.setUsername("demouser");
                demoUser.setEmail(demoEmail);
                demoUser.setPassword("Password123");
                demoUser.setValidationCode("123456");
                userRepository.save(demoUser);
            }
        };
    }
}
