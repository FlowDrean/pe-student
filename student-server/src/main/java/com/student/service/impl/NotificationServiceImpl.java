package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Notification;
import com.student.exception.BusinessException;
import com.student.mapper.NotificationMapper;
import com.student.service.NotificationService;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public Result<?> getNotifications(Long userId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        var pageResult = page(new Page<>(page, size), wrapper);
        return Result.ok(PageVO.from(pageResult));
    }

    @Override
    @Transactional
    public void markRead(Long notificationId) {
        Notification noti = getById(notificationId);
        if (noti == null) throw new BusinessException("通知不存在");
        noti.setIsRead(1);
        updateById(noti);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        baseMapper.markAllRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        removeById(notificationId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void createNotification(Long userId, Integer type, String title, String content, Long targetId, Integer targetType) {
        Notification noti = new Notification();
        noti.setUserId(userId);
        noti.setType(type);
        noti.setTitle(title);
        noti.setContent(content);
        noti.setTargetId(targetId);
        noti.setTargetType(targetType);
        noti.setIsRead(0);
        save(noti);
    }
}
