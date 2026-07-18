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
        List<String> subs = request.getSubcategories();
        int count = Math.max(request.getCount(), subs.size());

        // 第一步：每个小类保底 1 题
        for (int i = 0; i < subs.size(); i++) {
            QuestionDTO q = generateOneQuestion(request, subs.get(i), usedCorpusIds);
            if (q != null) {
                questions.add(q);
                addUsedIds(q, usedCorpusIds);
            }
        }

        // 第二步：剩余题目轮询小类
        int remaining = count - questions.size();
        for (int i = 0; i < remaining * 2 && questions.size() < count; i++) {
            String sub = subs.get(i % subs.size());
            QuestionDTO q = generateOneQuestion(request, sub, usedCorpusIds);
            if (q != null) {
                questions.add(q);
                addUsedIds(q, usedCorpusIds);
            }
        }

        return questions;
    }

    private QuestionDTO generateOneQuestion(GenerateRequest request, String sub, Set<Long> usedCorpusIds) {
        if ("choice".equals(request.getQuestionType())) {
            return generateChoiceForSub(request, sub, usedCorpusIds);
        } else if ("writing".equals(request.getQuestionType())) {
            return generateWritingForSub(request, sub, usedCorpusIds);
        }
        return null;
    }

    private void addUsedIds(QuestionDTO q, Set<Long> usedCorpusIds) {
        if (q.getCorpusId() != null) usedCorpusIds.add(q.getCorpusId());
        if (q.getRequiredCorpusIds() != null) usedCorpusIds.addAll(q.getRequiredCorpusIds());
    }

    public List<QuestionDTO> generateTranslationQuestions(List<String> subcategories, int count, String mode) {
        if (count < subcategories.size()) {
            count = subcategories.size(); // 至少每个小类 1 题
        }

        List<Corpus> corpusList = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();

        // 第一步：每个小类先保底取 1 个
        for (String sub : subcategories) {
            List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, 3);
            for (Corpus c : items) {
                if (seen.add(c.getId())) {
                    corpusList.add(c);
                    break;
                }
            }
        }

        // 第二步：剩下题目从所有小类中随机填充
        int remaining = count - corpusList.size();
        if (remaining > 0) {
            List<Corpus> extra = new ArrayList<>();
            for (String sub : subcategories) {
                extra.addAll(corpusMapper.selectRandomByCategory(null, sub, remaining + 10));
            }
            Collections.shuffle(extra);
            for (Corpus c : extra) {
                if (seen.add(c.getId())) {
                    corpusList.add(c);
                    if (corpusList.size() >= count) break;
                }
            }
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

    private QuestionDTO generateChoiceForSub(GenerateRequest request, String sub, Set<Long> usedCorpusIds) {
        List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, 50);
        List<Corpus> available = items.stream()
                .filter(c -> !usedCorpusIds.contains(c.getId()))
                .toList();
        if (available.isEmpty()) return null;

        Corpus corpus = available.get((int) (Math.random() * available.size()));

        String cacheKey = "[\"" + corpus.getId() + "\"]";
        QuestionDTO cached = checkCache(cacheKey, "choice");
        if (cached != null) return cached;

        QuestionDTO question = aiService.generateChoiceQuestion(
                corpus.getId(), corpus.getChinese(), corpus.getEnglish());
        saveCache(cacheKey, "choice", question);
        return question;
    }

    private QuestionDTO generateWritingForSub(GenerateRequest request, String sub, Set<Long> usedCorpusIds) {
        List<Corpus> items = corpusMapper.selectRandomByCategory(null, sub, 50);
        List<Corpus> available = items.stream()
                .filter(c -> !usedCorpusIds.contains(c.getId()))
                .toList();
        if (available.size() < 3) return null;

        Collections.shuffle(available);
        List<Corpus> selected = available.subList(0, Math.min(3, available.size()));
        List<Long> ids = selected.stream().map(Corpus::getId).toList();
        List<String> chineseList = selected.stream().map(Corpus::getChinese).toList();
        List<String> englishList = selected.stream().map(Corpus::getEnglish).toList();

        String cacheKey = ids.toString();
        QuestionDTO cached = checkCache(cacheKey, "writing");
        if (cached != null) return cached;

        QuestionDTO question = aiService.generateWritingQuestion(ids, chineseList, englishList, sub);
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
