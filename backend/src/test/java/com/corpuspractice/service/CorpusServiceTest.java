package com.corpuspractice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.corpuspractice.entity.Corpus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CorpusServiceTest {

    @Autowired
    private CorpusService corpusService;

    @Test
    @DisplayName("保存语料后可通过 ID 查询")
    void shouldSaveAndFindCorpus() {
        Corpus corpus = new Corpus();
        corpus.setChinese("测试中文");
        corpus.setEnglish("Test English");
        corpus.setCategory("描写续写");
        corpus.setSubcategory("优秀表达");

        corpusService.save(corpus);
        assertThat(corpus.getId()).isNotNull();

        Corpus found = corpusService.getById(corpus.getId());
        assertThat(found.getChinese()).isEqualTo("测试中文");
        assertThat(found.getEnglish()).isEqualTo("Test English");
    }

    @Test
    @DisplayName("分页查询可按分类筛选")
    void shouldListByPageWithFilter() {
        Corpus corpus = new Corpus();
        corpus.setChinese("分页测试");
        corpus.setEnglish("Pagination test");
        corpus.setCategory("议论文");
        corpus.setSubcategory("高级表达");
        corpusService.save(corpus);

        IPage<Corpus> page = corpusService.listByPage("议论文", null, null, 1, 10);
        assertThat(page.getRecords()).isNotEmpty();
        assertThat(page.getRecords()).allMatch(c -> "议论文".equals(c.getCategory()));
    }

    @Test
    @DisplayName("关键词搜索匹配中文或英文")
    void shouldSearchByKeyword() {
        Corpus corpus = new Corpus();
        corpus.setChinese("独一无二的搜索词");
        corpus.setEnglish("Unique search keyword");
        corpus.setCategory("描写续写");
        corpus.setSubcategory("环境");
        corpusService.save(corpus);

        IPage<Corpus> result = corpusService.listByPage(null, null, "独一无二", 1, 10);
        assertThat(result.getRecords()).hasSize(1);
    }
}
