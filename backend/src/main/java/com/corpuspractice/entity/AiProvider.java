package com.corpuspractice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_provider")
public class AiProvider {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer isActive;
    private LocalDateTime createdAt;
}
