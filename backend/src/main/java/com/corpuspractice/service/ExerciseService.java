package com.corpuspractice.service;

import com.corpuspractice.dto.*;
import com.corpuspractice.entity.Corpus;
import com.corpuspractice.entity.ExerciseCache;
import com.corpuspractice.mapper.CorpusMapper;
import com.corpuspractice.mapper.ExerciseCacheMapper;
import com.corpuspractice.mapper.PracticeAnswerMapper;
import com.corpuspractice.service.ai.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final CorpusMapper corpusMapper;
    private final ExerciseCacheMapper cacheMapper;
    private final PracticeAnswerMapper answerMapper;
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

    public List<QuestionDTO> generateTranslationQuestions(List<String> subcategories, int count, String mode) {
        List<Corpus> corpusList;

        if ("exam_cram".equals(mode)) {
            corpusList = selectWeightedByErrors(subcategories, count);
        } else {
            corpusList = selectRandom(subcategories, count);
        }

        Collections.shuffle(corpusList);

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

    private List<Corpus> selectRandom(List<String> subcategories, int count) {
        List<Corpus> corpusList = new ArrayList<>();
        for (String sub : subcategories) {
            List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, count);
            corpusList.addAll(items);
        }
        Collections.shuffle(corpusList);
        if (corpusList.size() > count) {
            corpusList = corpusList.subList(0, count);
        }
        return corpusList;
    }

    private List<Corpus> selectWeightedByErrors(List<String> subcategories, int count) {
        List<Corpus> allCorpus = new ArrayList<>();
        for (String sub : subcategories) {
            allCorpus.addAll(corpusMapper.selectRandomByCategory(null, sub, 100));
        }
        if (allCorpus.isEmpty()) return List.of();

        // 构建加权映射：错误越多的语料权重越大
        Map<Long, Double> weightMap = new HashMap<>();
        int maxErrors = 1;
        for (Corpus c : allCorpus) {
            int total = answerMapper.countTotalByCorpusId(c.getId());
            int correct = answerMapper.countCorrectByCorpusId(c.getId());
            int errors = total - correct;
            weightMap.put(c.getId(), 1.0 + errors);
            if (errors > maxErrors) maxErrors = errors;
        }

        // 标准化权重，错误越多的权重越大
        for (Corpus c : allCorpus) {
            double w = weightMap.get(c.getId());
            if (maxErrors > 0) w = w / (maxErrors + 1);
            weightMap.put(c.getId(), w);
        }

        // 加权随机采样：70% 加权 + 30% 纯随机
        List<Corpus> result = new ArrayList<>();
        List<Corpus> pool = new ArrayList<>(allCorpus);
        Random rand = new Random();

        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            Corpus picked;
            if (rand.nextDouble() < 0.3) {
                // 30% 纯随机
                picked = pool.get(rand.nextInt(pool.size()));
            } else {
                // 70% 加权选择
                double totalWeight = pool.stream().mapToDouble(c -> weightMap.get(c.getId())).sum();
                double r = rand.nextDouble() * totalWeight;
                double cumulative = 0;
                Corpus selected = pool.get(0);
                for (Corpus c : pool) {
                    cumulative += weightMap.get(c.getId());
                    if (r <= cumulative) {
                        selected = c;
                        break;
                    }
                }
                picked = selected;
            }
            result.add(picked);
            pool.remove(picked);
        }

        return result;
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
