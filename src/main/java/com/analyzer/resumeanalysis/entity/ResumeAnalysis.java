package com.analyzer.resumeanalysis.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class ResumeAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "experience", length = 1000)
    private String experience;
    @ElementCollection
    private List<String> education;

    @ElementCollection
    private List<String> skills;

    @Lob
    private String summary;
}
