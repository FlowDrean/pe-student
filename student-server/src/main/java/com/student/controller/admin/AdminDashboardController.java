package com.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.*;
import com.student.mapper.*;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final TradeMapper tradeMapper;
    private final CommentMapper commentMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 总数统计
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalPosts", postMapper.selectCount(null));
        data.put("totalTrades", tradeMapper.selectCount(null));
        data.put("totalComments", commentMapper.selectCount(null));

        // 今日新增
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayNewUsers", countNew(userMapper, todayStart));
        data.put("todayNewPosts", countNew(postMapper, todayStart));
        data.put("todayNewTrades", countNew(tradeMapper, todayStart));

        // 昨日新增（用于环比）
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        data.put("yesterdayNewUsers", countNew(userMapper, yesterdayStart));
        data.put("yesterdayNewPosts", countNew(postMapper, yesterdayStart));
        data.put("yesterdayNewTrades", countNew(tradeMapper, yesterdayStart));

        // 状态分布
        data.put("activeUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        data.put("disabledUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 0)));
        data.put("normalPosts", postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1)));
        data.put("sellingTrades", tradeMapper.selectCount(
                new LambdaQueryWrapper<Trade>().eq(Trade::getStatus, 1)));

        return Result.ok(data);
    }

    @GetMapping("/trends")
    public Result<Map<String, Object>> trends(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> userTrend = new ArrayList<>();
        List<Long> postTrend = new ArrayList<>();
        List<Long> tradeTrend = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(fmt));

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            userTrend.add(countBetween(userMapper, dayStart, dayEnd));
            postTrend.add(countBetween(postMapper, dayStart, dayEnd));
            tradeTrend.add(countBetween(tradeMapper, dayStart, dayEnd));
        }

        data.put("dates", dates);
        data.put("userTrend", userTrend);
        data.put("postTrend", postTrend);
        data.put("tradeTrend", tradeTrend);
        return Result.ok(data);
    }

    /** Count records created on or after given time */
    private long countNew(UserMapper mapper, LocalDateTime after) {
        return mapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, after));
    }
    private long countNew(PostMapper mapper, LocalDateTime after) {
        return mapper.selectCount(
                new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, after));
    }
    private long countNew(TradeMapper mapper, LocalDateTime after) {
        return mapper.selectCount(
                new LambdaQueryWrapper<Trade>().ge(Trade::getCreatedAt, after));
    }

    /** Count records created between two times */
    private long countBetween(UserMapper mapper, LocalDateTime start, LocalDateTime end) {
        return mapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreatedAt, start).lt(User::getCreatedAt, end));
    }
    private long countBetween(PostMapper mapper, LocalDateTime start, LocalDateTime end) {
        return mapper.selectCount(new LambdaQueryWrapper<Post>()
                .ge(Post::getCreatedAt, start).lt(Post::getCreatedAt, end));
    }
    private long countBetween(TradeMapper mapper, LocalDateTime start, LocalDateTime end) {
        return mapper.selectCount(new LambdaQueryWrapper<Trade>()
                .ge(Trade::getCreatedAt, start).lt(Trade::getCreatedAt, end));
    }
}
