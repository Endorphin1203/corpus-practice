package com.corpuspractice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("practice_session")
public class PracticeSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String mode;
    private String questionType;
    private String categoriesFilter;
    private Integer totalQuestions;
    private Integer correctCount;
    private Long userId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
