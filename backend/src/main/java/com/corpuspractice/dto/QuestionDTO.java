package com.corpuspractice.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long corpusId;
    private String questionType;
    private String prompt;              // 题干
    private String referenceAnswer;     // 标准答案（翻译题）
    private List<String> options;       // 选项（选择题）
    private String sceneDescription;    // 场景描述（写作题）
    private List<Long> requiredCorpusIds; // 要求使用的语料 ID（写作题）
}
