package com.student.controller;

import com.student.entity.dto.TradeDTO;
import com.student.service.TradeService;
import com.student.utils.SecurityUtil;
import com.student.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public Result<?> getTrades(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return tradeService.getTradeList(category, keyword, sort, page, size);
    }

    @GetMapping("/{id}")
    public Result<?> getTradeDetail(@PathVariable Long id) {
        return tradeService.getTradeDetail(id);
    }

    @PostMapping
    public Result<?> createTrade(@Valid @RequestBody TradeDTO dto) {
        return tradeService.createTrade(securityUtil.getCurrentUserId(), dto);
    }

    @PutMapping("/{id}")
    public Result<Void> updateTrade(@PathVariable Long id, @Valid @RequestBody TradeDTO dto) {
        tradeService.updateTrade(securityUtil.getCurrentUserId(), id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(securityUtil.getCurrentUserId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/favorite")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        return Result.ok(tradeService.toggleFavorite(securityUtil.getCurrentUserId(), id));
    }

    @GetMapping("/categories")
    public Result<?> getCategories() {
        return Result.ok(java.util.List.of(
                java.util.Map.of("key", "book", "label", "书籍", "icon", "📖"),
                java.util.Map.of("key", "electronics", "label", "电子", "icon", "💻"),
                java.util.Map.of("key", "daily", "label", "日用品", "icon", "🏷️"),
                java.util.Map.of("key", "clothing", "label", "衣物", "icon", "👔"),
                java.util.Map.of("key", "other", "label", "其他", "icon", "📦")
        ));
    }

    @GetMapping("/search")
    public Result<?> searchTrades(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return tradeService.searchTrades(keyword, page, size);
    }
}
