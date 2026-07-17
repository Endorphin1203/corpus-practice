package com.corpuspractice.dto;

import lombok.Data;
import java.util.List;

@Data
public class GenerateRequest {
    private String questionType;        // "choice" or "writing" or "translation"
    private String mode;                // "daily_review" or "exam_cram"
    private List<String> subcategories;
    private List<Long> usedCorpusIds;   // 已使用的语料 ID，避免重复
    private Long providerId;
    private int count;
}
