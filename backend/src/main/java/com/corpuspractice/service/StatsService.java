package com.corpuspractice.service;

import com.corpuspractice.mapper.PracticeAnswerMapper;
import com.corpuspractice.mapper.PracticeSessionMapper;
import com.corpuspractice.mapper.CorpusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PracticeSessionMapper sessionMapper;
    private final PracticeAnswerMapper answerMapper;
    private final CorpusMapper corpusMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 总练习次数
        overview.put("totalSessions", sessionMapper.selectCount(null));

        // 总题数
        overview.put("totalQuestions", answerMapper.selectCount(null));

        // 正确数
        Long correctCount = answerMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.corpuspractice.entity.PracticeAnswer>()
                        .eq(com.corpuspractice.entity.PracticeAnswer::getIsCorrect, 1));
        overview.put("correctCount", correctCount);

        // 正确率
        Long total = (Long) overview.get("totalQuestions");
        if (total > 0) {
            overview.put("accuracyRate", Math.round(correctCount * 10000.0 / total) / 100.0);
        } else {
            overview.put("accuracyRate", 0.0);
        }

        // 学习天数（有答题记录的天数）
        List<Map<String, Object>> days = answerMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.corpuspractice.entity.PracticeAnswer>()
                        .select(com.corpuspractice.entity.PracticeAnswer::getAnsweredAt)
                        .groupBy(com.corpuspractice.entity.PracticeAnswer::getAnsweredAt));
        overview.put("studyDays", days.size());

        return overview;
    }

    public List<Map<String, Object>> getTrend() {
        // 近 30 天每日正确率
        List<Map<String, Object>> trend = new ArrayList<>();
        String endDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String startDate = LocalDate.now().minusDays(29).format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<Map<String, Object>> records = answerMapper.selectWithCategory(startDate + " 00:00:00", endDate + " 23:59:59");

        Map<String, List<Boolean>> dailyResults = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            dailyResults.put(date, new ArrayList<>());
        }

        for (Map<String, Object> record : records) {
            String answeredAt = record.get("answered_at").toString();
            String date = answeredAt.substring(0, 10);
            if (dailyResults.containsKey(date)) {
                dailyResults.get(date).add(Integer.valueOf(1).equals(record.get("is_correct")));
            }
        }

        for (Map.Entry<String, List<Boolean>> entry : dailyResults.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", entry.getKey());
            List<Boolean> results = entry.getValue();
            if (results.isEmpty()) {
                point.put("accuracy", null);
                point.put("count", 0);
            } else {
                long correct = results.stream().filter(Boolean::booleanValue).count();
                point.put("accuracy", Math.round(correct * 10000.0 / results.size()) / 100.0);
                point.put("count", results.size());
            }
            trend.add(point);
        }
        return trend;
    }

    public List<Map<String, Object>> getByCategory() {
        List<Map<String, Object>> records = answerMapper.selectWithCategory("2020-01-01", "2099-12-31");
        Map<String, List<Boolean>> categoryResults = new HashMap<>();

        for (Map<String, Object> record : records) {
            String sub = (String) record.get("subcategory");
            if (sub == null) continue;
            categoryResults.computeIfAbsent(sub, k -> new ArrayList<>())
                    .add(Integer.valueOf(1).equals(record.get("is_correct")));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Boolean>> entry : categoryResults.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("subcategory", entry.getKey());
            List<Boolean> results = entry.getValue();
            long correct = results.stream().filter(Boolean::booleanValue).count();
            item.put("accuracy", Math.round(correct * 10000.0 / results.size()) / 100.0);
            item.put("total", results.size());
            result.add(item);
        }
        result.sort((a, b) -> Double.compare((Double) a.get("accuracy"), (Double) b.get("accuracy")));
        return result;
    }

    public List<Map<String, Object>> getWeakCorpus() {
        return answerMapper.findWeakCorpus(10);
    }

    public List<Map<String, Object>> getAvgDuration() {
        return answerMapper.avgDurationByCategory();
    }

    public String exportCsv() {
        List<Map<String, Object>> rows = answerMapper.exportAllAnswers();
        StringBuilder sb = new StringBuilder();
        sb.append("答题时间,大类,小类,中文原题,用户答案,是否正确,AI反馈,答题耗时(秒)\n");

        for (Map<String, Object> row : rows) {
            sb.append(formatCsv(row.get("answered_at"))).append(",");
            sb.append(formatCsv(row.get("category"))).append(",");
            sb.append(formatCsv(row.get("subcategory"))).append(",");
            sb.append(formatCsv(row.get("chinese"))).append(",");
            sb.append(formatCsv(row.get("user_answer"))).append(",");
            sb.append("1".equals(String.valueOf(row.get("is_correct"))) ? "正确" : "错误").append(",");
            sb.append(formatCsv(row.get("ai_feedback"))).append(",");
            sb.append(row.get("answer_duration_seconds") != null ? row.get("answer_duration_seconds") : "");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatCsv(Object value) {
        if (value == null) return "";
        String s = value.toString().replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
