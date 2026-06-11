package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Trade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TradeMapper extends BaseMapper<Trade> {

    @Update("UPDATE trade SET views = views + 1 WHERE id = #{tradeId}")
    int incrViews(@Param("tradeId") Long tradeId);
}
