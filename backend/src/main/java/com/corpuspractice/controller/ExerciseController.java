package com.corpuspractice.controller;

import com.corpuspractice.common.Result;
import com.corpuspractice.dto.*;
import com.corpuspractice.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping("/generate")
    public Result<List<QuestionDTO>> generate(@RequestBody GenerateRequest request) {
        Set<Long> used = request.getUsedCorpusIds() != null
                ? new HashSet<>(request.getUsedCorpusIds())
                : new HashSet<>();

        List<QuestionDTO> questions;
        if ("translation".equals(request.getQuestionType())) {
            questions = exerciseService.generateTranslationQuestions(
                    request.getSubcategories(), request.getCount(), request.getMode());
        } else {
            questions = exerciseService.generateQuestions(request, used);
        }
        return Result.ok(questions);
    }
}
