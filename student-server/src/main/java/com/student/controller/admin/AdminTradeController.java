package com.student.controller.admin;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Trade;
import com.student.entity.User;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.TradeMapper;
import com.student.mapper.UserMapper;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/trades")
@RequiredArgsConstructor
public class AdminTradeController {

    private final TradeMapper tradeMapper;
    private final UserMapper userMapper;

    @GetMapping
    public Result<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Trade::getTitle, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Trade::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Trade::getStatus, status);
        }
        wrapper.orderByDesc(Trade::getCreatedAt);

        IPage<Trade> result = tradeMapper.selectPage(new Page<>(page, size), wrapper);

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(result.getRecords().stream().map(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("price", t.getPrice());
            map.put("originalPrice", t.getOriginalPrice());
            map.put("category", t.getCategory());
            map.put("condition", t.getCondition());
            map.put("location", t.getLocation());
            map.put("contact", t.getContact());
            map.put("images", t.getImages() != null ? JSONUtil.toList(t.getImages(), String.class) : null);
            map.put("views", t.getViews());
            map.put("status", t.getStatus());
            map.put("userId", t.getUserId());
            map.put("createdAt", t.getCreatedAt());
            map.put("updatedAt", t.getUpdatedAt());
            User seller = userMapper.selectById(t.getUserId());
            if (seller != null) {
                Map<String, Object> sellerMap = new LinkedHashMap<>();
                sellerMap.put("id", seller.getId());
                sellerMap.put("username", seller.getUsername());
                sellerMap.put("nickname", seller.getNickname());
                sellerMap.put("avatar", seller.getAvatar());
                map.put("seller", sellerMap);
            }
            return map;
        }).toList());
        vo.setTotal(result.getTotal());
        vo.setPage(result.getCurrent());
        vo.setSize(result.getSize());
        vo.setPages(result.getPages());
        return Result.ok(vo);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Trade trade = tradeMapper.selectById(id);
        if (trade == null) {
            throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", trade.getId());
        map.put("title", trade.getTitle());
        map.put("description", trade.getDescription());
        map.put("price", trade.getPrice());
        map.put("originalPrice", trade.getOriginalPrice());
        map.put("category", trade.getCategory());
        map.put("condition", trade.getCondition());
        map.put("location", trade.getLocation());
        map.put("contact", trade.getContact());
        map.put("images", trade.getImages() != null ? JSONUtil.toList(trade.getImages(), String.class) : null);
        map.put("views", trade.getViews());
        map.put("status", trade.getStatus());
        map.put("createdAt", trade.getCreatedAt());
        map.put("updatedAt", trade.getUpdatedAt());
        User seller = userMapper.selectById(trade.getUserId());
        if (seller != null) {
            Map<String, Object> sellerMap = new LinkedHashMap<>();
            sellerMap.put("id", seller.getId());
            sellerMap.put("username", seller.getUsername());
            sellerMap.put("nickname", seller.getNickname());
            sellerMap.put("avatar", seller.getAvatar());
            map.put("seller", sellerMap);
        }
        return Result.ok(map);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Trade trade = tradeMapper.selectById(id);
        if (trade == null) {
            throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        }
        trade.setStatus(body.get("status"));
        tradeMapper.updateById(trade);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Trade trade = tradeMapper.selectById(id);
        if (trade == null) {
            throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        }
        if (body.containsKey("title")) trade.setTitle((String) body.get("title"));
        if (body.containsKey("description")) trade.setDescription((String) body.get("description"));
        if (body.containsKey("price")) trade.setPrice(new java.math.BigDecimal(body.get("price").toString()));
        if (body.containsKey("originalPrice") && body.get("originalPrice") != null)
            trade.setOriginalPrice(new java.math.BigDecimal(body.get("originalPrice").toString()));
        if (body.containsKey("category")) trade.setCategory((String) body.get("category"));
        if (body.containsKey("condition")) trade.setCondition((Integer) body.get("condition"));
        if (body.containsKey("location")) trade.setLocation((String) body.get("location"));
        if (body.containsKey("contact")) trade.setContact((String) body.get("contact"));
        if (body.containsKey("status")) trade.setStatus((Integer) body.get("status"));
        tradeMapper.updateById(trade);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Trade trade = tradeMapper.selectById(id);
        if (trade == null) {
            throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        }
        tradeMapper.deleteById(id);
        return Result.ok();
    }
}
