package com.corpuspractice.controller;

import com.corpuspractice.common.Result;
import com.corpuspractice.dto.*;
import com.corpuspractice.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping("/generate")
    public Result<List<QuestionDTO>> generate(@RequestBody GenerateRequest request) {
        List<QuestionDTO> questions;
        if ("translation".equals(request.getQuestionType())) {
            questions = exerciseService.generateTranslationQuestions(
                    request.getSubcategories(), request.getCount(), request.getMode());
        } else {
            questions = exerciseService.generateQuestions(request);
        }
        return Result.ok(questions);
    }
}
