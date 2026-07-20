package com.corpuspractice.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long corpusId;
    private String questionType;
    private String prompt;              // 题干
    private String stemTranslation;     // 题干中文翻译（选择题）
    private String corpusCategory;      // 来源语料大类
    private String corpusSubcategory;   // 来源语料小类
    private String corpusChinese;       // 来源语料中文（选择题用）
    private String corpusEnglish;       // 来源语料英文（选择题用）
    private String referenceAnswer;     // 标准答案
    private List<String> options;       // 选项（选择题）
    private String sceneDescription;    // 场景描述（写作题）
    private List<Long> requiredCorpusIds; // 要求使用的语料 ID（写作题）
}
