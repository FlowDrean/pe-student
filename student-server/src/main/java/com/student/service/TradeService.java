package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Trade;
import com.student.entity.dto.TradeDTO;
import com.student.vo.Result;

public interface TradeService extends IService<Trade> {

    /** 获取商品列表 */
    Result<?> getTradeList(String category, String keyword, String sort, int page, int size);

    /** 获取商品详情 */
    Result<?> getTradeDetail(Long tradeId);

    /** 发布商品 */
    Result<?> createTrade(Long userId, TradeDTO dto);

    /** 编辑商品 */
    void updateTrade(Long userId, Long tradeId, TradeDTO dto);

    /** 删除商品 */
    void deleteTrade(Long userId, Long tradeId);

    /** 收藏商品 */
    boolean toggleFavorite(Long userId, Long tradeId);

    /** 搜索商品 */
    Result<?> searchTrades(String keyword, int page, int size);
}
