package com.analyzer.resumeanalysis.repository;


import com.analyzer.resumeanalysis.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    @Query("SELECT r FROM ResumeAnalysis r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ResumeAnalysis> searchByRegex(@Param("name") String name);
}

