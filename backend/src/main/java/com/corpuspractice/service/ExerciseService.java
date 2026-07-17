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

    public List<QuestionDTO> generateQuestions(GenerateRequest request, Set<Long> usedCorpusIds) {
        List<QuestionDTO> questions = new ArrayList<>();

        for (int i = 0; i < request.getCount(); i++) {
            if ("choice".equals(request.getQuestionType())) {
                QuestionDTO q = generateChoice(request, usedCorpusIds);
                if (q != null) {
                    usedCorpusIds.add(q.getCorpusId());
                    questions.add(q);
                }
            } else if ("writing".equals(request.getQuestionType())) {
                QuestionDTO q = generateWriting(request, usedCorpusIds);
                if (q != null) {
                    if (q.getRequiredCorpusIds() != null) {
                        usedCorpusIds.addAll(q.getRequiredCorpusIds());
                    }
                    questions.add(q);
                }
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

        // 去重：按 corpus ID 去重，确保同一语料不出现两次
        Set<Long> seen = new LinkedHashSet<>();
        List<Corpus> deduped = new ArrayList<>();
        for (Corpus c : corpusList) {
            if (seen.add(c.getId())) {
                deduped.add(c);
            }
        }
        // 限制数量
        if (deduped.size() > count) {
            deduped = deduped.subList(0, count);
        }

        List<QuestionDTO> questions = new ArrayList<>();
        for (Corpus corpus : deduped) {
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
        // 从每个分类取足够多的语料（多取一些以保证去重后还有足够数量）
        int fetchCount = Math.max(count, count * 2 / Math.max(subcategories.size(), 1));
        List<Corpus> corpusList = new ArrayList<>();
        for (String sub : subcategories) {
            List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, fetchCount);
            corpusList.addAll(items);
        }
        Collections.shuffle(corpusList);
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

    private QuestionDTO generateChoice(GenerateRequest request, Set<Long> usedCorpusIds) {
        List<String> subs = request.getSubcategories();
        List<Corpus> allItems = new ArrayList<>();
        for (String sub : subs) {
            allItems.addAll(corpusMapper.selectRandomByCategory(null, sub, 50));
        }
        // 过滤已使用的语料
        List<Corpus> available = allItems.stream()
                .filter(c -> !usedCorpusIds.contains(c.getId()))
                .toList();
        if (available.isEmpty()) {
            return null; // 没有可用语料
        }
        Corpus corpus = available.get((int) (Math.random() * available.size()));

        // 检查缓存
        String cacheKey = "[\"" + corpus.getId() + "\"]";
        QuestionDTO cached = checkCache(cacheKey, "choice");
        if (cached != null) return cached;

        // 调用 AI 生成并缓存
        QuestionDTO question = aiService.generateChoiceQuestion(
                corpus.getId(), corpus.getChinese(), corpus.getEnglish());
        saveCache(cacheKey, "choice", question);
        return question;
    }

    private QuestionDTO generateWriting(GenerateRequest request, Set<Long> usedCorpusIds) {
        List<String> subs = request.getSubcategories();
        List<Corpus> allItems = new ArrayList<>();
        for (String sub : subs) {
            allItems.addAll(corpusMapper.selectRandomByCategory(null, sub, 50));
        }
        // 过滤已使用的语料
        List<Corpus> available = allItems.stream()
                .filter(c -> !usedCorpusIds.contains(c.getId()))
                .toList();
        if (available.size() < 3) {
            return null;
        }
        Collections.shuffle(available);
        List<Corpus> items = available.subList(0, 3);
        List<Long> ids = items.stream().map(Corpus::getId).toList();
        List<String> chineseList = items.stream().map(Corpus::getChinese).toList();
        List<String> englishList = items.stream().map(Corpus::getEnglish).toList();

        // 检查缓存
        String cacheKey = ids.toString();
        QuestionDTO cached = checkCache(cacheKey, "writing");
        if (cached != null) return cached;

        // 调用 AI 生成并缓存
        QuestionDTO question = aiService.generateWritingQuestion(ids, chineseList, englishList, subs.get(0));
        saveCache(cacheKey, "writing", question);
        return question;
    }

    private QuestionDTO checkCache(String corpusIds, String questionType) {
        List<ExerciseCache> caches = cacheMapper.findValidCache(corpusIds, questionType);
        if (!caches.isEmpty()) {
            try {
                return objectMapper.readValue(caches.get(0).getQuestionData(), QuestionDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("缓存反序列化失败，将重新生成", e);
            }
        }
        return null;
    }

    private void saveCache(String corpusIds, String questionType, QuestionDTO question) {
        try {
            ExerciseCache cache = new ExerciseCache();
            cache.setCorpusIds(corpusIds);
            cache.setQuestionType(questionType);
            cache.setQuestionData(objectMapper.writeValueAsString(question));
            cacheMapper.insert(cache);
        } catch (JsonProcessingException e) {
            log.warn("缓存保存失败", e);
        }
    }
}
