package com.corpuspractice.dto;

import lombok.Data;

@Data
public class EvaluateRequest {
    private Long corpusId;
    private String questionType;
    private String questionPrompt;     // 前端传的实际题目内容
    private String correctAnswer;      // 选择题正确答案
    private String stemText;           // 选择题题干（英文）
    private String stemTranslation;    // 选择题题干翻译
    private String userAnswer;
    private Long providerId;
}
