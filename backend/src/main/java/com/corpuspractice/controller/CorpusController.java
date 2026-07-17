package com.corpuspractice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.corpuspractice.common.Result;
import com.corpuspractice.dto.ImportResult;
import com.corpuspractice.entity.Corpus;
import com.corpuspractice.service.CorpusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/corpus")
@RequiredArgsConstructor
public class CorpusController {

    private final CorpusService corpusService;

    @GetMapping
    public Result<IPage<Corpus>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(corpusService.listByPage(category, subcategory, keyword, page, size));
    }

    @PostMapping
    public Result<Corpus> create(@RequestBody Corpus corpus) {
        corpusService.save(corpus);
        return Result.ok(corpus);
    }

    @PutMapping("/{id}")
    public Result<Corpus> update(@PathVariable Long id, @RequestBody Corpus corpus) {
        corpus.setId(id);
        corpusService.updateById(corpus);
        return Result.ok(corpusService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        corpusService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/import")
    public Result<ImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        ImportResult result = corpusService.importFromExcel(file);
        return Result.ok(result);
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        corpusService.removeByIds(ids);
        return Result.ok();
    }
}
