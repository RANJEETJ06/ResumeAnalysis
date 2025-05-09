package com.analyzer.resumeanalysis.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record ResumeAnalysisWithImprovementsDto(ResumeAnalysisDto resumeAnalysisDto, JsonNode improvements) {
}
