package com.corpuspractice.dto;

import lombok.Data;

@Data
public class EvaluateRequest {
    private Long corpusId;
    private String questionType;
    private String userAnswer;
    private Long providerId;
}
