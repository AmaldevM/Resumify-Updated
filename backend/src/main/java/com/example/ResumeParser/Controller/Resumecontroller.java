package com.example.ResumeParser.Controller;

import org.springframework.web.multipart.MultipartFile;
import com.example.ResumeParser.Service.Resumeservice;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.dto.ResumeWithSkillsDTO;
import com.example.ResumeParser.dto.Resumefilterrequest;
import com.example.ResumeParser.dto.SkillFilterRequest;
import com.example.ResumeParser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.ResumeParser.entity.Skill;
import com.example.ResumeParser.Service.GeminiService;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Resumecontroller {

    @Autowired
    private Resumeservice resumeservice;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeminiService geminiService;


    @GetMapping("/hii")
    public String hello() {
        return "hello";
    }

//for uploading resumes of a specific user 
    @PostMapping("/resumes/upload/{userId}")
    public ResponseEntity<String> uploadResume(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        System.out.println("Upload called with userId: " + userId);
        
        // ✅ Ensure the user exists before proceeding
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // ✅ Let the service handle associating resume with this user
        try {
            resumeservice.uploadResumeForUser(user.getId(), file);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ResponseEntity.ok("Resume uploaded and saved successfully for user with ID " + userId);
    }



// New endpoint to get resumes by userId
    @GetMapping("/resumesByUserId")
    public ResponseEntity<List<ResumeWithSkillsDTO>> getResumesByUserId(@RequestParam("userId") Long userId) {
        List<ResumeWithSkillsDTO> resumes = resumeservice.getResumesByUserId(userId);
        return ResponseEntity.ok(resumes);
    }

    // Aligned endpoint to get resumes by userId path variable
    @GetMapping("/resumes/resumesByUserId/{userId}")
    public ResponseEntity<List<ResumeWithSkillsDTO>> getUserResumes(@PathVariable Long userId) {
        List<ResumeWithSkillsDTO> resumes = resumeservice.getResumesByUserId(userId);
        return ResponseEntity.ok(resumes);
    }

    // Filter resumes by skills
    @GetMapping("/resumes/by-skills")
    public ResponseEntity<List<ResumeWithSkillsDTO>> getResumesBySkills(
            @RequestParam("userId") Long userId,
            @RequestParam("skills") List<String> skills) {
        List<ResumeWithSkillsDTO> resumes = resumeservice.getResumesBySkillsAndExperience(userId, skills, 0.0, 100.0);
        return ResponseEntity.ok(resumes);
    }

    // Filter resumes by skills and experience range
    @GetMapping("/resumes/by-skills-experience-range")
    public ResponseEntity<List<ResumeWithSkillsDTO>> getResumesBySkillsAndExperience(
            @RequestParam("userId") Long userId,
            @RequestParam("skills") List<String> skills,
            @RequestParam("start") double startExp,
            @RequestParam("end") double endExp) {
        List<ResumeWithSkillsDTO> resumes = resumeservice.getResumesBySkillsAndExperience(userId, skills, startExp, endExp);
        return ResponseEntity.ok(resumes);
    }

// for filtering resumes of a specific user
    @PostMapping("/filter")
        public List<ResumeWithSkillsDTO> filterResumes(@RequestBody Resumefilterrequest request) {
            return resumeservice.filterResumes(request.getSkills(), request.getMinYearsOfExperience(), request.getUserId());
    }
    
// Function to show the image of the resume
    @GetMapping("/resumes/image/{resumeId}")
    public ResponseEntity<Resource> getResumeImage(@PathVariable Long resumeId) {
        Resume resume = resumeservice.getResumeById(resumeId);
        if (resume == null || resume.getResumeImage() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageData = resume.getResumeImage();
        ByteArrayResource resource = new ByteArrayResource(imageData);

        return ResponseEntity.ok()
                .contentLength(imageData.length)
                .contentType(MediaType.IMAGE_PNG) // Use actual image format if needed
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resume_" + resumeId + ".png\"")
                .body(resource);
    }


// deleting the stored resumed details of a specific user
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUserResumes(@PathVariable Long userId) {
        resumeservice.deleteResumesByUserId(userId);
        return ResponseEntity.ok("User's resumes deleted successfully");
    }

    // Delete all resumes for a user
    @DeleteMapping("/resumes/delete/user/{userId}")
    public ResponseEntity<?> deleteAllResumes(@PathVariable Long userId) {
        resumeservice.deleteResumesByUserId(userId);
        return ResponseEntity.ok("All resumes deleted successfully");
    }

    // Delete a single resume
    @DeleteMapping("/resumes/delete/user/{userId}/resume/{resumeId}")
    public ResponseEntity<?> deleteSingleResume(@PathVariable Long userId, @PathVariable Long resumeId) {
        resumeservice.deleteResumeById(userId, resumeId);
        return ResponseEntity.ok("Resume deleted successfully");
    }

    // Resume Preview endpoint
    @GetMapping(value = "/resumes/preview/{userId}/{resumeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResumePreview(@PathVariable Long userId, @PathVariable Long resumeId) {
        Resume resume = resumeservice.getResumeById(resumeId);
        if (resume == null || !resume.getUser().getId().equals(userId)) {
            return ResponseEntity.notFound().build();
        }

        String parsedJson = resume.getParsedJson();
        if (parsedJson == null || parsedJson.trim().isEmpty() || "{}".equals(parsedJson.trim())) {
            String rawText = resume.getRawText();
            if (rawText == null || rawText.trim().isEmpty()) {
                rawText = "Name: " + resume.getName() + "\nSkills: " + String.join(", ", resume.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
            }
            parsedJson = geminiService.parseResume(rawText);
            resume.setParsedJson(parsedJson);
            resumeservice.saveResume(resume);
        }

        return ResponseEntity.ok(parsedJson);
    }

    // ATS score checker
    @PostMapping(value = "/resumes/ats-score", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> checkAtsScore(@RequestBody Map<String, Object> payload) {
        Long resumeId = Long.valueOf(payload.get("resumeId").toString());
        String jobDescription = payload.get("jobDescription").toString();

        Resume resume = resumeservice.getResumeById(resumeId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }

        String rawText = resume.getRawText();
        if (rawText == null || rawText.trim().isEmpty()) {
            rawText = "Name: " + resume.getName() + "\nSkills: " + String.join(", ", resume.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
        }

        String atsResult = geminiService.scanAts(rawText, jobDescription);
        return ResponseEntity.ok(atsResult);
    }

    // Smart resume enhancement
    @PostMapping(value = "/resumes/enhance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> enhanceResume(@RequestBody Map<String, Object> payload) {
        Long resumeId = Long.valueOf(payload.get("resumeId").toString());
        String targetRole = payload.containsKey("targetRole") ? payload.get("targetRole").toString() : "Software Engineer";
        String jobDescription = payload.containsKey("jobDescription") ? payload.get("jobDescription").toString() : "";

        Resume resume = resumeservice.getResumeById(resumeId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }

        String rawText = resume.getRawText();
        if (rawText == null || rawText.trim().isEmpty()) {
            rawText = "Name: " + resume.getName() + "\nSkills: " + String.join(", ", resume.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
        }

        String enhancementResult = geminiService.enhanceResume(rawText, targetRole, jobDescription);
        return ResponseEntity.ok(enhancementResult);
    }

}
