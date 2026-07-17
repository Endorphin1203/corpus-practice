package com.corpuspractice.controller;

import com.corpuspractice.entity.Corpus;
import com.corpuspractice.service.CorpusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorpusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorpusService corpusService;

    @Test
    @DisplayName("POST /api/corpus 创建语料返回 200")
    void shouldCreateCorpus() throws Exception {
        String json = """
            {
                "chinese": "测试创建",
                "english": "Test create",
                "category": "描写续写",
                "subcategory": "优秀表达"
            }
            """;

        mockMvc.perform(post("/api/corpus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chinese").value("测试创建"));
    }

    @Test
    @DisplayName("GET /api/corpus 分页查询返回 200")
    void shouldListCorpus() throws Exception {
        mockMvc.perform(get("/api/corpus")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("DELETE /api/corpus/{id} 删除语料返回 200")
    void shouldDeleteCorpus() throws Exception {
        Corpus corpus = new Corpus();
        corpus.setChinese("待删除");
        corpus.setEnglish("To be deleted");
        corpus.setCategory("议论文");
        corpus.setSubcategory("高级表达");
        corpusService.save(corpus);

        mockMvc.perform(delete("/api/corpus/" + corpus.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
