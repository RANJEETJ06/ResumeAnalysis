package com.analyzer.resumeanalysis.controller;


import com.analyzer.resumeanalysis.dto.RawTextRequest;
import com.analyzer.resumeanalysis.dto.ResumeAnalysisDto;
import com.analyzer.resumeanalysis.dto.ResumeAnalysisWithImprovementsDto;
import com.analyzer.resumeanalysis.service.AIAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class ResumeAnalysisController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalysisWithImprovementsDto> analyzeText(@RequestBody RawTextRequest request) {
        return ResponseEntity.ok(aiAnalysisService.analyze(request.getText()));
    }

    @GetMapping("/analysis/{name}")
    public ResponseEntity<List<ResumeAnalysisDto>> getAnalysisById(@PathVariable String name) {
        return ResponseEntity.ok(aiAnalysisService.getAnalysisByName(name));
    }
}
