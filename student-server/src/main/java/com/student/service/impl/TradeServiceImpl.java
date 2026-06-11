package com.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Favorite;
import com.student.entity.Trade;
import com.student.entity.User;
import com.student.entity.dto.TradeDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.FavoriteMapper;
import com.student.mapper.TradeMapper;
import com.student.mapper.UserMapper;
import com.student.service.TradeService;
import com.student.vo.PageVO;
import com.student.vo.Result;
import com.student.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl extends ServiceImpl<TradeMapper, Trade> implements TradeService {

    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public Result<?> getTradeList(String category, String keyword, String sort, int page, int size) {
        LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trade::getStatus, 1);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(Trade::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Trade::getTitle, keyword);
        }

        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Trade::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Trade::getPrice);
        } else {
            wrapper.orderByDesc(Trade::getCreatedAt);
        }

        var pageResult = page(new Page<>(page, size), wrapper);
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(t -> {
            Map<String, Object> map = BeanUtil.beanToMap(t);
            if (t.getImages() != null) map.put("images", JSONUtil.toList(t.getImages(), String.class));
            User user = userMapper.selectById(t.getUserId());
            if (user != null) map.put("user", BeanUtil.copyProperties(user, UserVO.class));
            return map;
        }).collect(Collectors.toList());

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return Result.ok(vo);
    }

    @Override
    public Result<?> getTradeDetail(Long tradeId) {
        Trade trade = getById(tradeId);
        if (trade == null || trade.getStatus() == 0) throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        baseMapper.incrViews(tradeId);

        Map<String, Object> map = BeanUtil.beanToMap(trade);
        if (trade.getImages() != null) map.put("images", JSONUtil.toList(trade.getImages(), String.class));
        User user = userMapper.selectById(trade.getUserId());
        if (user != null) map.put("user", BeanUtil.copyProperties(user, UserVO.class));
        return Result.ok(map);
    }

    @Override
    @Transactional
    public Result<?> createTrade(Long userId, TradeDTO dto) {
        Trade trade = BeanUtil.copyProperties(dto, Trade.class);
        trade.setUserId(userId);
        trade.setImages(dto.getImages() != null ? JSONUtil.toJsonStr(dto.getImages()) : null);
        trade.setStatus(1);
        trade.setViews(0);
        save(trade);
        return Result.ok(trade);
    }

    @Override
    @Transactional
    public void updateTrade(Long userId, Long tradeId, TradeDTO dto) {
        Trade trade = getById(tradeId);
        if (trade == null) throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        if (!trade.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        BeanUtil.copyProperties(dto, trade, "images");
        if (dto.getImages() != null) trade.setImages(JSONUtil.toJsonStr(dto.getImages()));
        updateById(trade);
    }

    @Override
    @Transactional
    public void deleteTrade(Long userId, Long tradeId) {
        Trade trade = getById(tradeId);
        if (trade == null) throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        if (!trade.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        trade.setStatus(0);
        updateById(trade);
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long tradeId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetId, tradeId)
                .eq(Favorite::getTargetType, 2);

        Favorite fav = favoriteMapper.selectOne(wrapper);
        if (fav != null) {
            favoriteMapper.deleteById(fav.getId());
            return false;
        } else {
            Favorite newFav = new Favorite();
            newFav.setUserId(userId);
            newFav.setTargetId(tradeId);
            newFav.setTargetType(2);
            favoriteMapper.insert(newFav);
            return true;
        }
    }

    @Override
    public Result<?> searchTrades(String keyword, int page, int size) {
        return getTradeList(null, keyword, null, page, size);
    }
}
