package com.corpuspractice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exercise_cache")
public class ExerciseCache {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String corpusIds;
    private String questionType;
    private String questionData;
    private LocalDateTime createdAt;
}
