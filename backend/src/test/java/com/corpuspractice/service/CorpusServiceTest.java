package com.corpuspractice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.corpuspractice.dto.ImportResult;
import com.corpuspractice.entity.Corpus;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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

    @Test
    @DisplayName("Excel 导入成功返回导入结果")
    void shouldImportFromExcel() throws Exception {
        // Create a test xlsx in memory
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("优秀表达");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("中文");
            header.createCell(1).setCellValue("英文");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("测试导入中文");
            row.createCell(1).setCellValue("Test import English");

            File tempFile = File.createTempFile("test-corpus", ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                wb.write(fos);
            }

            FileInputStream fis = new FileInputStream(tempFile);
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fis);

            ImportResult result = corpusService.importFromExcel(mockFile);
            assertThat(result.getNewCount()).isEqualTo(1);
            assertThat(result.getTotalRows()).isEqualTo(1);

            tempFile.delete();
        }
    }
}
