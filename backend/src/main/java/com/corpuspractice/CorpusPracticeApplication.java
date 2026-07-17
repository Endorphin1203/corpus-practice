package com.corpuspractice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.corpuspractice.mapper")
public class CorpusPracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CorpusPracticeApplication.class, args);
    }
}
