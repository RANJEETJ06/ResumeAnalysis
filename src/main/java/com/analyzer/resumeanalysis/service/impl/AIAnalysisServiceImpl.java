package com.analyzer.resumeanalysis.service.impl;

import com.analyzer.resumeanalysis.Exception.ResourceNotFoundException;
import com.analyzer.resumeanalysis.dto.ResumeAnalysisDto;
import com.analyzer.resumeanalysis.dto.ResumeAnalysisWithImprovementsDto;
import com.analyzer.resumeanalysis.entity.ResumeAnalysis;
import com.analyzer.resumeanalysis.repository.ResumeAnalysisRepository;
import com.analyzer.resumeanalysis.service.AI.Analyze;
import com.analyzer.resumeanalysis.service.AIAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ModelMapper modelMapper;
    private final Analyze analyze;

    @Override
    public ResumeAnalysisWithImprovementsDto analyze(String rawText) {
        ResumeAnalysis analysis = new ResumeAnalysis();

        JsonNode analysedData=analyze.extractResumeData(rawText);

        analysis.setName(analysedData.get("name").asText());
        analysis.setExperience(analysedData.get("experience").asText());

        List<String> educationList = new ArrayList<>();
        JsonNode educationNode = analysedData.get("education");
        if (educationNode != null) {
            for (JsonNode educationItem : educationNode) {
                educationList.add(educationItem.asText());
            }
        }else {
            educationList=new ArrayList<>();
        }
        analysis.setEducation(educationList);
        List<String> skillList = new ArrayList<>();
        JsonNode skillNode= analysedData.get("skills");
        if (skillNode != null) {
            for (JsonNode skillItem : skillNode) {
                skillList.add(skillItem.asText());
            }
        }else {
            skillList=new ArrayList<>();
        }
        analysis.setSkills(skillList);
        analysis.setSummary(analysedData.get("summary").asText());

        // Extracting contacts
        List<String> contactsList = new ArrayList<>();
        JsonNode contactsNode = analysedData.get("contacts");
        System.out.println(contactsNode);
        if (contactsNode != null) {
            for (JsonNode contact : contactsNode) {
                String type = contact.has("type") ? contact.get("type").asText() : "";
                String value = contact.has("value") ? contact.get("value").asText() : "";
                contactsList.add(type + ": " + value);
            }
        }
        analysis.setContacts(contactsList);

        // Extracting projects
        List<String> projectsList = new ArrayList<>();
        JsonNode projectsNode = analysedData.get("projects");
        if (projectsNode != null && projectsNode.isArray()) {
            for (JsonNode project : projectsNode) {
                String title = project.has("title") ? project.get("title").asText() : "";
                String description = project.has("description") ? project.get("description").asText() : "";
                String techStack = project.has("tech_stack") && project.get("tech_stack").isArray()
                        ? project.get("tech_stack").toString() : "";

                String formatted = "Title: " + title + "\nTech Stack: " + techStack + "\nDescription: " + description;
                projectsList.add(formatted);
            }
        }
        analysis.setProjects(projectsList);


        ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);
        JsonNode improvements = analyze.suggestImprovements(analysedData, "SpringBoot Developer");
        ResumeAnalysisDto savedAnalysisDto=modelMapper.map(savedAnalysis,ResumeAnalysisDto.class);
        return new ResumeAnalysisWithImprovementsDto(savedAnalysisDto,improvements);
    }

    @Override
    public List<ResumeAnalysisDto> getAnalysisByName(String name) {
        List<ResumeAnalysis> analyses = resumeAnalysisRepository.searchByRegex(name);

        if (analyses.isEmpty()) {
            throw new ResourceNotFoundException("ResumeAnalysis", "name", name);
        }

        return analyses.stream()
                .map(analysis -> modelMapper.map(analysis, ResumeAnalysisDto.class))
                .collect(Collectors.toList());
    }

}
