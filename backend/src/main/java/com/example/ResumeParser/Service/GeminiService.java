package com.example.ResumeParser.Service;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    private final String geminiApiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        // Fallback to checking System env
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = ""; // Empty string triggers mock fallback
        }
        this.geminiApiKey = key;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        if (isMockMode()) {
            System.out.println("⚠️ GEMINI_API_KEY not found or empty. Running in Mock AI Demo Mode.");
        } else {
            System.out.println("🚀 GEMINI_API_KEY detected. Running in Google Gemini AI Production Mode.");
        }
    }

    public boolean isMockMode() {
        return geminiApiKey == null || geminiApiKey.trim().isEmpty();
    }

    /**
     * Parses raw resume text into structured JSON matching the UI expectations.
     */
    public String parseResume(String rawText) {
        if (isMockMode()) {
            return generateMockResumeParsing(rawText);
        }

        String prompt = "You are an expert ATS resume parser. Extract the user details from the following resume text. " +
                "Output exactly a JSON object conforming to the following JSON schema:\n" +
                "{\n" +
                "  \"name\": \"Full Name\",\n" +
                "  \"email\": \"Email Address\",\n" +
                "  \"phone\": \"Phone Number\",\n" +
                "  \"jobPosition\": \"Current or target job position (e.g. Software Engineer)\",\n" +
                "  \"yearsOfExperience\": 4.5,\n" +
                "  \"skills\": [\"Skill1\", \"Skill2\"],\n" +
                "  \"profileSummary\": \"A short professional summary of the candidate\",\n" +
                "  \"education\": [\n" +
                "    { \"degree\": \"Degree title\", \"institute\": \"School/University\", \"years\": \"Start-End year\" }\n" +
                "  ],\n" +
                "  \"experience\": [\n" +
                "    { \"title\": \"Job Title\", \"company\": \"Company Name\", \"years\": \"Start-End year\", \"achievements\": [\"achievement 1\", \"achievement 2\"] }\n" +
                "  ],\n" +
                "  \"projects\": [\n" +
                "    { \"title\": \"Project Name\", \"description\": \"Detailed description of what was done and technologies used\" }\n" +
                "  ]\n" +
                "}\n" +
                "Rules:\n" +
                "1. If a section is missing, leave it as an empty array or empty string, do not make it null.\n" +
                "2. Years of experience must be a floating point number.\n" +
                "3. Return ONLY raw JSON, do not wrap in markdown tags or comments.\n\n" +
                "Resume Text:\n" + rawText;

        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            System.err.println("Gemini parsing failed. Falling back to Mock: " + e.getMessage());
            return generateMockResumeParsing(rawText);
        }
    }

    /**
     * Computes the ATS score and returns missing keywords, skill gaps, and suggestions.
     */
    public String scanAts(String resumeText, String jobDescription) {
        if (isMockMode()) {
            return generateMockAtsResult(resumeText, jobDescription);
        }

        String prompt = "You are an expert Recruiter and ATS scanner. Evaluate the following resume against the job description. " +
                "Output exactly a JSON object conforming to the following schema:\n" +
                "{\n" +
                "  \"atsScore\": 85,\n" +
                "  \"missingKeywords\": [\"Docker\", \"Kubernetes\"],\n" +
                "  \"suggestedImprovements\": [\"Add quantifiable metrics to experience section\", \"Elaborate on React projects\"],\n" +
                "  \"skillGaps\": [\"Cloud deployment\", \"Unit testing\"],\n" +
                "  \"resumeStrengthPercentage\": 78\n" +
                "}\n" +
                "Rules:\n" +
                "1. Output ONLY the raw JSON object, no markdown or markdown wrapping.\n" +
                "2. Provide accurate and honest evaluation of the match.\n\n" +
                "Job Description:\n" + jobDescription + "\n\n" +
                "Resume Text:\n" + resumeText;

        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            System.err.println("Gemini ATS scanning failed. Falling back to Mock: " + e.getMessage());
            return generateMockAtsResult(resumeText, jobDescription);
        }
    }

    /**
     * Enhances a resume by suggesting action verbs, identifying weak phrases, and suggesting grammar fixes.
     */
    public String enhanceResume(String resumeText, String targetRole, String jobDescription) {
        if (isMockMode()) {
            return generateMockEnhancementResult(resumeText, targetRole, jobDescription);
        }

        String prompt = "You are a professional resume writer and coach. Analyze the following resume text and provide actionable enhancements " +
                "specifically tailored for the role: " + targetRole + " (and optionally matching the job description if provided: " + jobDescription + ").\n" +
                "Output exactly a JSON object conforming to the following schema:\n" +
                "{\n" +
                "  \"weakPhrases\": [\n" +
                "    { \"original\": \"original phrase from resume\", \"suggestion\": \"improved version with active verbs and metrics\" }\n" +
                "  ],\n" +
                "  \"actionVerbs\": [\"Spearheaded\", \"Engineered\", \"Accelerated\"],\n" +
                "  \"grammarIssues\": [\n" +
                "    { \"original\": \"original grammatical/stylistic issue\", \"suggestion\": \"fixed version\" }\n" +
                "  ],\n" +
                "  \"improvedProjectDescriptions\": [\n" +
                "    { \"title\": \"Project Name\", \"description\": \"Rewrite the project description to show impacts and metrics\" }\n" +
                "  ]\n" +
                "}\n" +
                "Rules:\n" +
                "1. If there are no clear grammatical errors, suggest style/brevity fixes in the grammarIssues section.\n" +
                "2. Output ONLY the raw JSON, no markdown.\n\n" +
                "Resume Text:\n" + resumeText;

        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            System.err.println("Gemini enhancement failed. Falling back to Mock: " + e.getMessage());
            return generateMockEnhancementResult(resumeText, targetRole, jobDescription);
        }
    }

    private String callGeminiApi(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Generation Config to enforce JSON output
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));
        requestBody.put("generationConfig", generationConfig);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidate = root.path("candidates").get(0);
            String textResponse = candidate.path("content").path("parts").get(0).path("text").asText();
            return textResponse;
        } else {
            throw new RuntimeException("HTTP Error: " + response.getStatusCode());
        }
    }

    // --- MOCK FALLBACK IMPLEMENTATIONS ---

    private String generateMockResumeParsing(String rawText) {
        String name = extractRegex(rawText, "(?i)(?:name|candidate|profile)?\\s*:?\\s*([A-Z][a-z]+ [A-Z][a-z]+)", "Candidate Name");
        if ("Candidate Name".equals(name)) {
            // Pick first few words that look like a name
            Pattern p = Pattern.compile("([A-Z][a-zA-Z]+ [A-Z][a-zA-Z]+)");
            Matcher m = p.matcher(rawText);
            if (m.find()) name = m.group(1);
        }

        String email = extractRegex(rawText, "[\\w\\.-]+@[\\w\\.-]+", "contact@example.com");
        String phone = extractRegex(rawText, "(\\+\\d{1,3}[\\s-]?)?(\\(\\d{1,4}\\)[\\s-]?)?([\\d\\s-]{5,15})", "+1-555-0199");

        // Try to estimate years of experience from keywords
        double experienceYears = 2.0;
        if (rawText.toLowerCase().contains("senior")) experienceYears = 6.5;
        else if (rawText.toLowerCase().contains("lead")) experienceYears = 8.0;
        else if (rawText.toLowerCase().contains("intern")) experienceYears = 0.5;

        // Parse skills from known skills list
        List<String> detectedSkills = new ArrayList<>();
        String[] skillBank = {"Angular", "React", "Vue", "Java", "Spring Boot", "Spring", "Python", "Flask", "Django", "SQL", "MySQL", "PostgreSQL", "Docker", "Kubernetes", "AWS", "Azure", "Git", "TypeScript", "JavaScript", "HTML", "CSS"};
        for (String skill : skillBank) {
            if (rawText.toLowerCase().contains(skill.toLowerCase())) {
                detectedSkills.add(skill);
            }
        }
        if (detectedSkills.isEmpty()) {
            detectedSkills.addAll(Arrays.asList("Java", "SQL", "Git"));
        }

        // Job position
        String jobPosition = "Software Engineer";
        if (rawText.toLowerCase().contains("frontend")) jobPosition = "Frontend Developer";
        else if (rawText.toLowerCase().contains("backend")) jobPosition = "Backend Developer";
        else if (rawText.toLowerCase().contains("full stack") || rawText.toLowerCase().contains("fullstack")) jobPosition = "Full Stack Developer";

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("name", name);
            root.put("email", email);
            root.put("phone", phone);
            root.put("jobPosition", jobPosition);
            root.put("yearsOfExperience", experienceYears);
            
            root.putPOJO("skills", detectedSkills);
            
            root.put("profileSummary", "Highly motivated software professional with experience in building scalable web applications. Adept at leveraging modern web technologies to optimize performance and deliver clean, well-tested code.");
            
            // Mock Education
            List<Map<String, String>> eduList = new ArrayList<>();
            Map<String, String> edu = new HashMap<>();
            edu.put("degree", "Bachelor of Science in Computer Science");
            edu.put("institute", "State Technical University");
            edu.put("years", "2018 - 2022");
            eduList.add(edu);
            root.putPOJO("education", eduList);

            // Mock Experience
            List<Map<String, Object>> expList = new ArrayList<>();
            Map<String, Object> exp = new HashMap<>();
            exp.put("title", jobPosition);
            exp.put("company", "Innovate Tech Labs");
            exp.put("years", "2022 - Present");
            exp.put("achievements", Arrays.asList(
                "Developed and maintained microservices using backend frameworks, increasing throughput by 15%.",
                "Collaborated with cross-functional teams to integrate responsive user interfaces.",
                "Optimized database indexing and queries, decreasing API load times by 20%."
            ));

            expList.add(exp);
            root.putPOJO("experience", expList);

            // Mock Projects
            List<Map<String, String>> projList = new ArrayList<>();
            Map<String, String> proj = new HashMap<>();
            proj.put("title", "E-Commerce Platform Re-architecture");
            proj.put("description", "Migrated a legacy monolithic platform into a cloud-native microservices architecture, enhancing search performance and checkout flows.");
            projList.add(proj);
            root.putPOJO("projects", projList);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String generateMockAtsResult(String resumeText, String jobDescription) {
        // Calculate a score based on keyword intersections
        int matchCount = 0;
        String[] keywords = {"angular", "react", "java", "spring", "docker", "kubernetes", "aws", "sql", "git", "ci/cd", "microservices", "testing", "agile"};
        List<String> missing = new ArrayList<>();
        List<String> gaps = new ArrayList<>();

        for (String kw : keywords) {
            boolean inResume = resumeText.toLowerCase().contains(kw);
            boolean inJob = jobDescription.toLowerCase().contains(kw);

            if (inJob && !inResume) {
                missing.add(kw.substring(0, 1).toUpperCase() + kw.substring(1));
                gaps.add(kw.substring(0, 1).toUpperCase() + kw.substring(1));
            } else if (inJob && inResume) {
                matchCount++;
            }
        }

        int atsScore = 40 + (matchCount * 8);
        if (atsScore > 98) atsScore = 98;
        if (atsScore < 50) atsScore = 55; // Decent baseline for demo

        List<String> improvements = new ArrayList<>();
        improvements.add("Include quantifiable achievements in work experience (e.g. 'boosted sales by 20%')");
        if (missing.size() > 0) {
            improvements.add("Explicitly integrate missing keywords like " + String.join(", ", missing.subList(0, Math.min(2, missing.size()))) + " into your professional summary.");
        }
        improvements.add("Refine formatting and use strong action verbs at the beginning of bullet points.");

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("atsScore", atsScore);
            root.putPOJO("missingKeywords", missing.isEmpty() ? Arrays.asList("Docker", "AWS") : missing);
            root.putPOJO("suggestedImprovements", improvements);
            root.putPOJO("skillGaps", gaps.isEmpty() ? Arrays.asList("Cloud Architecture", "Unit Testing") : gaps);
            root.put("resumeStrengthPercentage", atsScore - 5);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String generateMockEnhancementResult(String resumeText, String targetRole, String jobDescription) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            
            List<Map<String, String>> weakPhrases = new ArrayList<>();
            Map<String, String> phrase1 = new HashMap<>();
            phrase1.put("original", "Worked on a web application using Angular and Spring Boot");
            phrase1.put("suggestion", "Engineered and shipped a responsive web application leveraging Angular and Spring Boot, boosting developer velocity by 25%");
            weakPhrases.add(phrase1);
            
            Map<String, String> phrase2 = new HashMap<>();
            phrase2.put("original", "Responsible for writing unit tests and fixing bugs");
            phrase2.put("suggestion", "Spearheaded test-driven development (TDD), achieving 85% code coverage and cutting production bugs by 40%");
            weakPhrases.add(phrase2);
            root.putPOJO("weakPhrases", weakPhrases);

            root.putPOJO("actionVerbs", Arrays.asList("Spearheaded", "Architected", "Engineered", "Optimized", "Formulated", "Pioneered"));

            List<Map<String, String>> grammarIssues = new ArrayList<>();
            Map<String, String> gram1 = new HashMap<>();
            gram1.put("original", "I was managing the database deployment...");
            gram1.put("suggestion", "Managed database deployments (active voice)...");
            grammarIssues.add(gram1);
            root.putPOJO("grammarIssues", grammarIssues);

            List<Map<String, String>> improvedProjects = new ArrayList<>();
            Map<String, String> proj1 = new HashMap<>();
            proj1.put("title", "Project Portfolio Builder");
            proj1.put("description", "Architected a real-time portfolio generation engine. Integrated secure RESTful services that scaled to support 500+ daily uploads, reducing response times by 35% using Redis caching.");
            improvedProjects.add(proj1);
            root.putPOJO("improvedProjectDescriptions", improvedProjects);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String extractRegex(String text, String regex, String defaultValue) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }
}
