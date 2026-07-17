package com.corpuspractice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("corpus")
public class Corpus {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String chinese;
    private String english;
    private String notes;
    private String category;
    private String subcategory;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
