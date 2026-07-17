package com.corpuspractice.controller;

import com.corpuspractice.common.Result;
import com.corpuspractice.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(statsService.getOverview());
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend() {
        return Result.ok(statsService.getTrend());
    }

    @GetMapping("/by-category")
    public Result<List<Map<String, Object>>> byCategory() {
        return Result.ok(statsService.getByCategory());
    }

    @GetMapping("/weak-corpus")
    public Result<List<Map<String, Object>>> weakCorpus() {
        return Result.ok(statsService.getWeakCorpus());
    }

    @GetMapping("/avg-duration")
    public Result<List<Map<String, Object>>> avgDuration() {
        return Result.ok(statsService.getAvgDuration());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = statsService.exportCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        String filename = "练习记录_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
