package com.corpuspractice.dto;

import lombok.Data;
import java.util.List;

@Data
public class FeedbackDTO {
    private String verdict;                         // "correct" | "partial" | "incorrect"
    private List<GrammarError> grammarErrors;
    private List<WordChoiceIssue> wordChoiceIssues;
    private String improvedVersion;

    @Data
    public static class GrammarError {
        private String location;
        private String error;
        private String suggestion;
    }

    @Data
    public static class WordChoiceIssue {
        private String original;
        private String issue;
        private String alternative;
    }
}
