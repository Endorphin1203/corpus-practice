package com.corpuspractice.dto;

import lombok.Data;

@Data
public class EvaluateRequest {
    private Long corpusId;
    private String questionType;
    private String questionPrompt;  // 前端传的实际题目内容
    private String correctAnswer;  // 前端传的正确答案（选择题为 AI 生成的正确选项）
    private String userAnswer;
    private Long providerId;
}
