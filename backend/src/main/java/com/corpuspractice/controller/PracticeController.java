package com.corpuspractice.controller;

import com.corpuspractice.common.Result;
import com.corpuspractice.dto.*;
import com.corpuspractice.entity.PracticeSession;
import com.corpuspractice.mapper.PracticeAnswerMapper;
import com.corpuspractice.mapper.PracticeSessionMapper;
import com.corpuspractice.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;
    private final PracticeSessionMapper sessionMapper;
    private final PracticeAnswerMapper answerMapper;

    @PostMapping("/start")
    public Result<PracticeSession> start(@RequestBody Map<String, String> body) {
        PracticeSession session = practiceService.startSession(
                body.get("mode"),
                body.get("questionType"),
                body.get("categoriesFilter"));
        return Result.ok(session);
    }

    @PostMapping("/{id}/evaluate")
    public Result<FeedbackDTO> evaluate(@PathVariable Long id, @RequestBody EvaluateRequest request) {
        FeedbackDTO feedback = practiceService.submitAnswer(id, request);
        return Result.ok(feedback);
    }

    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        practiceService.finishSession(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<PracticeSession> getById(@PathVariable Long id) {
        return Result.ok(sessionMapper.selectById(id));
    }

    @GetMapping("/{id}/answers")
    public Result<List<Map<String, Object>>> getAnswers(@PathVariable Long id) {
        return Result.ok(answerMapper.findBySessionId(id));
    }

    @GetMapping
    public Result<List<PracticeSession>> list() {
        return Result.ok(sessionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PracticeSession>()
                        .orderByDesc(PracticeSession::getStartedAt)));
    }
}
