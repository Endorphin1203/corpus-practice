package com.corpuspractice.service.ai;

import com.corpuspractice.config.CryptoUtil;
import com.corpuspractice.dto.*;
import com.corpuspractice.entity.AiProvider;
import com.corpuspractice.mapper.AiProviderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class OpenAICompatService implements AIService {

    private final AiProviderMapper providerMapper;
    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final int timeout;

    public OpenAICompatService(AiProviderMapper providerMapper,
                                CryptoUtil cryptoUtil,
                                ObjectMapper objectMapper,
                                @Value("${app.ai.timeout-seconds:30}") int timeout) {
        this.providerMapper = providerMapper;
        this.cryptoUtil = cryptoUtil;
        this.objectMapper = objectMapper;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    @Override
    public QuestionDTO generateChoiceQuestion(Long corpusId, String chinese, String english) {
        AiProvider provider = getActiveProvider();
        String prompt = buildChoicePrompt(chinese, english);
        String response = callAI(provider, prompt);
        return parseChoiceResponse(response, corpusId, english);
    }

    @Override
    public QuestionDTO generateWritingQuestion(List<Long> corpusIds,
                                                List<String> chineseList,
                                                List<String> englishList,
                                                String subcategory) {
        AiProvider provider = getActiveProvider();
        String prompt = buildWritingPrompt(corpusIds, chineseList, englishList, subcategory);
        String response = callAI(provider, prompt);
        return parseWritingResponse(response, corpusIds);
    }

    @Override
    public FeedbackDTO evaluateAnswer(String chinese, String referenceEnglish, String notes,
                                       String userAnswer, String questionType) {
        AiProvider provider = getActiveProvider();
        String prompt = buildEvaluatePrompt(chinese, referenceEnglish, notes, userAnswer);
        String response = callAI(provider, prompt);
        return parseFeedbackResponse(response);
    }

    private AiProvider getActiveProvider() {
        List<AiProvider> providers = providerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiProvider>()
                        .eq(AiProvider::getIsActive, 1));
        if (providers.isEmpty()) {
            throw new RuntimeException("没有可用的 AI 后端，请先在设置中配置");
        }
        return providers.get(0);
    }

    private String callAI(AiProvider provider, String systemPrompt) {
        try {
            String apiKey = cryptoUtil.decrypt(provider.getApiKey());
            String url = provider.getBaseUrl();
            if (!url.endsWith("/")) url += "/";
            url += "chat/completions";

            Map<String, Object> body = new HashMap<>();
            body.put("model", provider.getModelName());
            body.put("messages", List.of(
                    Map.of("role", "user", "content", systemPrompt)
            ));
            body.put("temperature", 0.7);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeout))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("AI 调用失败: status={}, body={}", response.statusCode(), response.body());
                throw new RuntimeException("AI 服务返回错误: " + response.statusCode());
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (IOException | InterruptedException e) {
            log.error("AI 调用异常", e);
            throw new RuntimeException("AI 服务暂时不可用: " + e.getMessage());
        }
    }

    private String buildChoicePrompt(String chinese, String english) {
        return """
            你是一个英语教学助手。请根据下面的中文语料及其对应英文，生成一道语境选择题。

            中文原文：%s
            正确英文：%s

            要求：
            1. 生成 4 个选项（A/B/C/D），其中 1 个是上述正确英文
            2. 其他 3 个选项应是看似合理但不准确的干扰项（用词不当、语法有轻微问题、或语境不符）
            3. 题干应是一个简短的中文语境描述，让用户判断哪个英文表达最合适

            请严格按以下 JSON 格式返回（不要包含其他内容）：
            {
              "stem": "题干（中文语境描述）",
              "options": ["选项A", "选项B", "选项C", "选项D"],
              "correctIndex": 0
            }
            """.formatted(chinese, english);
    }

    private QuestionDTO parseChoiceResponse(String response, Long corpusId, String english) {
        try {
            String json = extractJson(response);
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            QuestionDTO dto = new QuestionDTO();
            dto.setCorpusId(corpusId);
            dto.setQuestionType("choice");
            dto.setPrompt((String) map.get("stem"));
            dto.setOptions((List<String>) map.get("options"));
            dto.setReferenceAnswer(english);
            return dto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 返回格式解析失败", e);
        }
    }

    private String buildWritingPrompt(List<Long> corpusIds, List<String> chineseList,
                                       List<String> englishList, String subcategory) {
        StringBuilder corpusSection = new StringBuilder();
        for (int i = 0; i < chineseList.size(); i++) {
            corpusSection.append("- 语料%d: %s → %s\n".formatted(corpusIds.get(i), chineseList.get(i), englishList.get(i)));
        }

        return """
            你是一个英语教学助手。请根据下面指定的语料，生成一道场景写作题。

            指定语料（%s 类）：
            %s

            要求：
            1. 描述一个具体的写作场景（中文，50-100字），使这些语料可以自然地运用其中
            2. 明确要求使用上述语料
            3. 场景应贴近考试写作题风格

            请严格按以下 JSON 格式返回：
            {
              "scene": "写作场景描述",
              "requirement": "写作要求（包含需使用语料的说明）",
              "corpusIds": %s
            }
            """.formatted(subcategory, corpusSection.toString(), corpusIds.toString());
    }

    private QuestionDTO parseWritingResponse(String response, List<Long> corpusIds) {
        try {
            String json = extractJson(response);
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            QuestionDTO dto = new QuestionDTO();
            dto.setQuestionType("writing");
            dto.setSceneDescription(map.get("scene") + "\n\n" + map.get("requirement"));
            dto.setRequiredCorpusIds(corpusIds);
            return dto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 返回格式解析失败", e);
        }
    }

    private String buildEvaluatePrompt(String chinese, String referenceEnglish, String notes,
                                        String userAnswer) {
        return """
            你是一个英语教学助手。请对学生的翻译答案进行精细评估。

            中文原题：%s
            标准英文答案：%s
            %s

            学生答案：%s

            请从以下维度评估，严格按 JSON 格式返回（不要包含其他内容）：
            {
              "verdict": "correct|partial|incorrect",
              "grammarErrors": [
                {"location": "错误位置（用英文指出）", "error": "错误说明（中文）", "suggestion": "修改建议（中文）"}
              ],
              "wordChoiceIssues": [
                {"original": "学生使用的词", "issue": "问题说明（中文）", "alternative": "推荐替换词"}
              ],
              "improvedVersion": "AI润色后的完整参考译文"
            }

            说明：
            - verdict: 完全正确用 "correct"，大意对但有小问题用 "partial"，完全错误用 "incorrect"
            - grammarErrors: 语法错误列表，没有则为空数组
            - wordChoiceIssues: 用词不当列表，没有则为空数组
            - improvedVersion: 基于学生答案润色后的版本
            """.formatted(chinese, referenceEnglish,
                    notes != null && !notes.isEmpty() ? "备注：" + notes : "",
                    userAnswer);
    }

    private FeedbackDTO parseFeedbackResponse(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, FeedbackDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 返回格式解析失败", e);
        }
    }

    private String extractJson(String response) {
        String trimmed = response.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
