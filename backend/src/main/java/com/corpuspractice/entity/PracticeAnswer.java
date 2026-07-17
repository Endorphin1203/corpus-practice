package com.corpuspractice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("practice_answer")
public class PracticeAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long corpusId;
    private String questionType;
    private String questionPrompt;
    private String userAnswer;
    private String aiFeedback;
    private Integer isCorrect;
    private Integer score;
    private Integer answerDurationSeconds;
    private LocalDateTime answeredAt;
}
