package com.analyzer.resumeanalysis.service;

import com.analyzer.resumeanalysis.dto.ResumeAnalysisDto;

import java.util.List;

public interface AIAnalysisService {
    ResumeAnalysisDto analyze(String rawText);

    List<ResumeAnalysisDto> getAnalysisByName(String name);

}
