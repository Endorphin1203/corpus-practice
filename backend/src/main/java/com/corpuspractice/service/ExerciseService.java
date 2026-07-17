package com.corpuspractice.service;

import com.corpuspractice.dto.*;
import com.corpuspractice.entity.Corpus;
import com.corpuspractice.entity.ExerciseCache;
import com.corpuspractice.mapper.CorpusMapper;
import com.corpuspractice.mapper.ExerciseCacheMapper;
import com.corpuspractice.service.ai.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final CorpusMapper corpusMapper;
    private final ExerciseCacheMapper cacheMapper;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public List<QuestionDTO> generateQuestions(GenerateRequest request) {
        List<QuestionDTO> questions = new ArrayList<>();

        for (int i = 0; i < request.getCount(); i++) {
            if ("choice".equals(request.getQuestionType())) {
                questions.add(generateChoice(request));
            } else if ("writing".equals(request.getQuestionType())) {
                questions.add(generateWriting(request));
            }
        }
        return questions;
    }

    public List<QuestionDTO> generateTranslationQuestions(List<String> subcategories, int count) {
        List<Corpus> corpusList = new ArrayList<>();
        for (String sub : subcategories) {
            // 从每个子分类随机抽取
            List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, count);
            corpusList.addAll(items);
        }
        Collections.shuffle(corpusList);
        if (corpusList.size() > count) {
            corpusList = corpusList.subList(0, count);
        }

        List<QuestionDTO> questions = new ArrayList<>();
        for (Corpus corpus : corpusList) {
            QuestionDTO dto = new QuestionDTO();
            dto.setCorpusId(corpus.getId());
            dto.setQuestionType("translation");
            dto.setPrompt(corpus.getChinese());
            dto.setReferenceAnswer(corpus.getEnglish());
            questions.add(dto);
        }
        return questions;
    }

    private QuestionDTO generateChoice(GenerateRequest request) {
        List<String> subs = request.getSubcategories();
        String sub = subs.get((int) (Math.random() * subs.size()));
        List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, 3);
        if (items.isEmpty()) {
            throw new RuntimeException("所选分类没有足够语料");
        }
        Corpus corpus = items.get(0);
        return aiService.generateChoiceQuestion(corpus.getId(), corpus.getChinese(), corpus.getEnglish());
    }

    private QuestionDTO generateWriting(GenerateRequest request) {
        List<String> subs = request.getSubcategories();
        String sub = subs.get((int) (Math.random() * subs.size()));
        List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, 3);
        if (items.isEmpty()) {
            throw new RuntimeException("所选分类没有足够语料");
        }
        List<Long> ids = items.stream().map(Corpus::getId).toList();
        List<String> chineseList = items.stream().map(Corpus::getChinese).toList();
        List<String> englishList = items.stream().map(Corpus::getEnglish).toList();
        return aiService.generateWritingQuestion(ids, chineseList, englishList, sub);
    }
}
