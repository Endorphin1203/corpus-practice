package com.corpuspractice.service;

import com.corpuspractice.dto.*;
import com.corpuspractice.entity.Corpus;
import com.corpuspractice.entity.PracticeAnswer;
import com.corpuspractice.entity.PracticeSession;
import com.corpuspractice.mapper.CorpusMapper;
import com.corpuspractice.mapper.PracticeAnswerMapper;
import com.corpuspractice.mapper.PracticeSessionMapper;
import com.corpuspractice.service.ai.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeSessionMapper sessionMapper;
    private final PracticeAnswerMapper answerMapper;
    private final CorpusMapper corpusMapper;
    private final AIService aiService;

    @Transactional
    public PracticeSession startSession(String mode, String questionType, String categoriesFilter) {
        PracticeSession session = new PracticeSession();
        session.setMode(mode);
        session.setQuestionType(questionType);
        session.setCategoriesFilter(categoriesFilter);
        session.setStartedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Transactional
    public FeedbackDTO submitAnswer(Long sessionId, EvaluateRequest request) {
        Corpus corpus = corpusMapper.selectById(request.getCorpusId());
        if (corpus == null) {
            throw new RuntimeException("语料不存在");
        }

        FeedbackDTO feedback;
        if ("choice".equals(request.getQuestionType())) {
            // 选择题直接比对，不调 AI
            feedback = checkChoiceAnswer(request.getUserAnswer(), corpus.getEnglish());
        } else {
            // 翻译/写作调用 AI 纠错
            feedback = aiService.evaluateAnswer(
                    corpus.getChinese(),
                    corpus.getEnglish(),
                    corpus.getNotes(),
                    request.getUserAnswer(),
                    request.getQuestionType());
        }

        // 保存答题记录
        PracticeAnswer answer = new PracticeAnswer();
        answer.setSessionId(sessionId);
        answer.setCorpusId(request.getCorpusId());
        answer.setQuestionType(request.getQuestionType());
        // 优先用前端传的实际题目内容，否则回退到语料中文原文
        answer.setQuestionPrompt(
                request.getQuestionPrompt() != null && !request.getQuestionPrompt().isEmpty()
                        ? request.getQuestionPrompt()
                        : corpus.getChinese());
        answer.setUserAnswer(request.getUserAnswer());
        answer.setIsCorrect("correct".equals(feedback.getVerdict()) ? 1 : 0);
        answer.setAnsweredAt(LocalDateTime.now());
        answerMapper.insert(answer);

        return feedback;
    }

    private FeedbackDTO checkChoiceAnswer(String userAnswer, String correctAnswer) {
        FeedbackDTO feedback = new FeedbackDTO();
        boolean isCorrect = userAnswer != null
                && userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        feedback.setVerdict(isCorrect ? "correct" : "incorrect");

        // 始终展示正确答案
        FeedbackDTO.WordChoiceIssue issue = new FeedbackDTO.WordChoiceIssue();
        issue.setOriginal(userAnswer);
        issue.setAlternative(correctAnswer);
        issue.setIssue(isCorrect ? "你的选择是正确的" : "正确答案如上，你的选择有误，请对照参考");
        feedback.setWordChoiceIssues(List.of(issue));

        feedback.setImprovedVersion("正确答案：\n" + correctAnswer
                + (isCorrect ? "\n\n✅ 回答正确！" : "\n\n❌ 本次选择错误，请记住正确表达。"));
        return feedback;
    }

    @Transactional
    public void finishSession(Long sessionId) {
        PracticeSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setFinishedAt(LocalDateTime.now());

            // 统计正确数
            Long correctCount = answerMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PracticeAnswer>()
                            .eq(PracticeAnswer::getSessionId, sessionId)
                            .eq(PracticeAnswer::getIsCorrect, 1));
            session.setCorrectCount(correctCount.intValue());

            // 统计总数
            Long totalCount = answerMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PracticeAnswer>()
                            .eq(PracticeAnswer::getSessionId, sessionId));
            session.setTotalQuestions(totalCount.intValue());

            sessionMapper.updateById(session);
        }
    }
}
