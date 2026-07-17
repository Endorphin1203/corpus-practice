package com.corpuspractice.controller;

import com.corpuspractice.common.Result;
import com.corpuspractice.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
