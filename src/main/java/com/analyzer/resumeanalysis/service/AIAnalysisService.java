package com.analyzer.resumeanalysis.service;

import com.analyzer.resumeanalysis.dto.ResumeAnalysisDto;
import com.analyzer.resumeanalysis.dto.ResumeAnalysisWithImprovementsDto;

import java.util.List;

public interface AIAnalysisService {
    ResumeAnalysisWithImprovementsDto analyze(String rawText);

    List<ResumeAnalysisDto> getAnalysisByName(String name);

}
