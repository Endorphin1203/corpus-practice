package com.corpuspractice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.corpuspractice.dto.ImportResult;
import com.corpuspractice.entity.Corpus;
import com.corpuspractice.mapper.CorpusMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CorpusService extends ServiceImpl<CorpusMapper, Corpus> {

    private static final Map<String, String> SHEET_CATEGORY_MAP = new HashMap<>();
    static {
        SHEET_CATEGORY_MAP.put("优秀表达", "描写续写");
        SHEET_CATEGORY_MAP.put("动作", "描写续写");
        SHEET_CATEGORY_MAP.put("情绪", "描写续写");
        SHEET_CATEGORY_MAP.put("环境", "描写续写");
        SHEET_CATEGORY_MAP.put("外貌", "描写续写");
        SHEET_CATEGORY_MAP.put("好句段落", "议论文");
        SHEET_CATEGORY_MAP.put("开头", "议论文");
        SHEET_CATEGORY_MAP.put("主体", "议论文");
        SHEET_CATEGORY_MAP.put("观点库", "议论文");
        SHEET_CATEGORY_MAP.put("高级表达", "议论文");
        SHEET_CATEGORY_MAP.put("主题词", "议论文");
    }

    public IPage<Corpus> listByPage(String category, String subcategory, String keyword,
                                     int page, int size) {
        LambdaQueryWrapper<Corpus> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Corpus::getCategory, category);
        }
        if (subcategory != null && !subcategory.isEmpty()) {
            wrapper.eq(Corpus::getSubcategory, subcategory);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Corpus::getChinese, keyword)
                            .or().like(Corpus::getEnglish, keyword));
        }
        wrapper.orderByDesc(Corpus::getId);
        return page(new Page<>(page, size), wrapper);
    }

    public boolean delete(Long id) {
        return removeById(id);
    }

    public boolean batchDelete(List<Long> ids) {
        return removeBatchByIds(ids);
    }

    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName().trim();
                String category = resolveCategory(sheetName);
                if (category == null) {
                    result.getErrors().add("Sheet '" + sheetName + "' 未识别，已跳过");
                    continue;
                }
                processSheet(sheet, category, sheetName, result);
            }
        } catch (Exception e) {
            log.error("Excel 导入失败", e);
            result.getErrors().add("文件解析失败: " + e.getMessage());
        }
        return result;
    }

    private String resolveCategory(String sheetName) {
        return SHEET_CATEGORY_MAP.get(sheetName);
    }

    private void processSheet(Sheet sheet, String category, String subcategory,
                               ImportResult result) {
        if (sheet.getLastRowNum() < 1) return;

        Row headerRow = sheet.getRow(0);
        int chineseCol = findColumnIndex(headerRow, "中文");
        int englishCol = findColumnIndex(headerRow, "英文");
        int notesCol = findColumnIndex(headerRow, "备注");

        if (chineseCol < 0 || englishCol < 0) {
            result.getErrors().add("Sheet '" + subcategory + "' 缺少中文或英文列，已跳过");
            return;
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String chinese = getCellString(row, chineseCol);
                String english = getCellString(row, englishCol);
                String notes = notesCol >= 0 ? getCellString(row, notesCol) : null;

                if (chinese == null || chinese.isEmpty() || english == null || english.isEmpty()) {
                    continue;
                }

                // 跳过标题行（纯中文分类行如"走🦶"、"恐惧😱"）
                if (english.trim().isEmpty() || isCategoryHeader(chinese, english)) {
                    continue;
                }

                result.setTotalRows(result.getTotalRows() + 1);

                // 去重检查
                long exists = count(new LambdaQueryWrapper<Corpus>()
                        .eq(Corpus::getChinese, chinese.trim()));
                if (exists > 0) {
                    result.setSkipCount(result.getSkipCount() + 1);
                    continue;
                }

                Corpus corpus = new Corpus();
                corpus.setChinese(chinese.trim());
                corpus.setEnglish(english.trim());
                corpus.setNotes(notes != null ? notes.trim() : null);
                corpus.setCategory(category);
                corpus.setSubcategory(subcategory);
                save(corpus);
                result.setNewCount(result.getNewCount() + 1);

            } catch (Exception e) {
                result.getErrors().add("Sheet '" + subcategory + "' 第" + (i + 1) + "行: " + e.getMessage());
            }
        }
    }

    private boolean isCategoryHeader(String chinese, String english) {
        // 分类标题行如"走🦶" / "恐惧😱" — 只有中文列有值，英文列是分类标识
        return english.length() <= 3 && (english.contains("🦶") || english.contains("😱"));
    }

    private int findColumnIndex(Row headerRow, String keyword) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.toString().contains(keyword)) {
                return i;
            }
        }
        return -1;
    }

    private String getCellString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return value != null ? value.trim() : null;
    }
}
