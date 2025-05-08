package com.analyzer.resumeanalysis.service.impl;

import com.analyzer.resumeanalysis.Exception.ResourceNotFoundException;
import com.analyzer.resumeanalysis.dto.ResumeAnalysisDto;
import com.analyzer.resumeanalysis.entity.ResumeAnalysis;
import com.analyzer.resumeanalysis.repository.ResumeAnalysisRepository;
import com.analyzer.resumeanalysis.service.AI.Analyze;
import com.analyzer.resumeanalysis.service.AIAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ModelMapper modelMapper;
    private final Analyze analyze;

    @Override
    public ResumeAnalysisDto analyze(String rawText) {
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

        ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);

        return modelMapper.map(savedAnalysis,ResumeAnalysisDto.class);
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
