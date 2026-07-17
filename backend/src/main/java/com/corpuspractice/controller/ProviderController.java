package com.corpuspractice.controller;

import com.corpuspractice.common.Result;
import com.corpuspractice.config.CryptoUtil;
import com.corpuspractice.entity.AiProvider;
import com.corpuspractice.mapper.AiProviderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final AiProviderMapper providerMapper;
    private final CryptoUtil cryptoUtil;

    @GetMapping
    public Result<List<AiProvider>> list() {
        List<AiProvider> providers = providerMapper.selectList(null);
        // 不返回明文 API Key
        providers.forEach(p -> p.setApiKey("******"));
        return Result.ok(providers);
    }

    @PostMapping
    public Result<AiProvider> create(@RequestBody AiProvider provider) {
        // API Key 加密存储
        provider.setApiKey(cryptoUtil.encrypt(provider.getApiKey()));
        providerMapper.insert(provider);
        provider.setApiKey("******");
        return Result.ok(provider);
    }

    @PutMapping("/{id}")
    public Result<AiProvider> update(@PathVariable Long id, @RequestBody AiProvider provider) {
        AiProvider existing = providerMapper.selectById(id);
        if (existing == null) {
            return Result.fail("AI 后端不存在");
        }
        provider.setId(id);
        // 如果前端传了新的 API Key，重新加密
        if (provider.getApiKey() != null && !"******".equals(provider.getApiKey())) {
            provider.setApiKey(cryptoUtil.encrypt(provider.getApiKey()));
        } else {
            provider.setApiKey(existing.getApiKey());
        }
        providerMapper.updateById(provider);
        provider.setApiKey("******");
        return Result.ok(provider);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        providerMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    public Result<String> testConnection(@PathVariable Long id) {
        AiProvider provider = providerMapper.selectById(id);
        if (provider == null) {
            return Result.fail("AI 后端不存在");
        }

        try {
            String apiKey = cryptoUtil.decrypt(provider.getApiKey());
            String url = provider.getBaseUrl();
            if (!url.endsWith("/")) url += "/";
            url += "chat/completions";

            String body = """
                {"model":"%s","messages":[{"role":"user","content":"hello"}],"max_tokens":5}
                """.formatted(provider.getModelName());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Result.ok("连接成功");
            } else {
                return Result.fail("连接失败: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            return Result.fail("连接失败: " + e.getMessage());
        }
    }
}
