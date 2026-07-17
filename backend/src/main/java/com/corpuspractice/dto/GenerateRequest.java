package com.corpuspractice.dto;

import lombok.Data;
import java.util.List;

@Data
public class GenerateRequest {
    private String questionType;        // "choice" or "writing" or "translation"
    private String mode;                // "daily_review" or "exam_cram"
    private List<String> subcategories;
    private Long providerId;
    private int count;
}
