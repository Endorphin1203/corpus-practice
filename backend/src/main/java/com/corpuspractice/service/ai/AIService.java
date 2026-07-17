package com.corpuspractice.service.ai;

import com.corpuspractice.dto.*;

public interface AIService {
    QuestionDTO generateChoiceQuestion(Long corpusId, String chinese, String english);
    QuestionDTO generateWritingQuestion(java.util.List<Long> corpusIds,
                                         java.util.List<String> chineseList,
                                         java.util.List<String> englishList,
                                         String subcategory);
    FeedbackDTO evaluateAnswer(String chinese, String referenceEnglish, String notes,
                                String userAnswer, String questionType);
}
