package com.corpuspractice.dto;

import lombok.Data;

@Data
public class EvaluateRequest {
    private Long corpusId;
    private String questionType;
    private String questionPrompt;  // 前端传的实际题目内容（选择题为题干，翻译题为中文，写作为场景）
    private String userAnswer;
    private Long providerId;
}
