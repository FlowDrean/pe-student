package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Notification;
import com.student.vo.Result;

public interface NotificationService extends IService<Notification> {

    /** 获取通知列表 */
    Result<?> getNotifications(Long userId, int page, int size);

    /** 标记通知已读 */
    void markRead(Long notificationId);

    /** 全部标记已读 */
    void markAllRead(Long userId);

    /** 删除通知 */
    void deleteNotification(Long notificationId);

    /** 获取未读数量 */
    int getUnreadCount(Long userId);

    /** 创建通知 */
    void createNotification(Long userId, Integer type, String title, String content, Long targetId, Integer targetType);
}
