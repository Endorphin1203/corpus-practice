package com.corpuspractice.dto;

import lombok.Data;
import java.util.List;

@Data
public class GenerateRequest {
    private String questionType;        // "choice" or "writing"
    private List<String> subcategories;
    private Long providerId;
    private int count;
}
