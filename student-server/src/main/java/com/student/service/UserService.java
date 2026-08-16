package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.User;
import com.student.entity.dto.LoginDTO;
import com.student.entity.dto.UserDTO;
import com.student.vo.UserVO;

import java.util.Map;

public interface UserService extends IService<User> {

    /** 注册 */
    Map<String, String> register(UserDTO dto);

    /** 登录 */
    Map<String, String> login(LoginDTO dto);

    /** 刷新Token */
    Map<String, String> refreshToken(String refreshToken);

    /** 获取当前用户信息 */
    UserVO getProfile(Long userId);

    /** 更新用户信息 */
    void updateProfile(Long userId, UserDTO dto);

    /** 获取用户主页 */
    UserVO getUserById(Long currentUserId, Long targetUserId);

    /** 关注用户 */
    void follow(Long userId, Long targetUserId);

    /** 取消关注 */
    void unfollow(Long userId, Long targetUserId);

    /** 是否已关注 */
    boolean isFollowed(Long userId, Long targetUserId);

    /** 获取关注列表（用户关注了谁） */
    Object getFollowingList(Long currentUserId, Long userId, int page, int size);

    /** 获取粉丝列表（谁关注了用户） */
    Object getFansList(Long currentUserId, Long userId, int page, int size);

    /** 获取用户帖子列表 */
    Object getUserPosts(Long userId, Long targetUserId, int page, int size);

    /** 获取用户商品列表 */
    Object getUserTrades(Long userId, Long targetUserId, int page, int size);

    /** 获取用户收藏 */
    Object getUserFavorites(Long userId, int targetType, int page, int size);
}
