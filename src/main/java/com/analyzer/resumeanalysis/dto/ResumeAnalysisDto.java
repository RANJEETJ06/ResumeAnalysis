package com.analyzer.resumeanalysis.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysisDto {

    private Long id;
    private String name;
    private String experience;
    private List<String> education;
    private List<String> skills;
    private String summary;

    // New fields for contacts and projects
    private List<String> contacts;
    private List<String> projects;
}
