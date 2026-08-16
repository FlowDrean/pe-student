package com.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Favorite;
import com.student.entity.Follow;
import com.student.entity.Post;
import com.student.entity.Trade;
import com.student.entity.User;
import com.student.entity.dto.LoginDTO;
import com.student.entity.dto.UserDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.FavoriteMapper;
import com.student.mapper.FollowMapper;
import com.student.mapper.PostMapper;
import com.student.mapper.TradeMapper;
import com.student.mapper.UserMapper;
import com.student.service.NotificationService;
import com.student.service.UserService;
import com.student.utils.JwtUtil;
import com.student.vo.PageVO;
import com.student.vo.PostVO;
import com.student.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PostMapper postMapper;
    private final TradeMapper tradeMapper;
    private final FavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Map<String, String> register(UserDTO dto) {
        // 检查用户名
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        // 检查手机号
        if (dto.getPhone() != null && lambdaQuery().eq(User::getPhone, dto.getPhone()).count() > 0) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }

        User user = BeanUtil.copyProperties(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        save(user);

        // 生成 Token
        String role = user.getRole() != null ? user.getRole() : "user";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", String.valueOf(user.getId()));
        return result;
    }

    @Override
    public Map<String, String> login(LoginDTO dto) {
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .or()
                .eq(User::getPhone, dto.getUsername())
                .one();

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String role = user.getRole() != null ? user.getRole() : "user";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", String.valueOf(user.getId()));
        return result;
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String role = user.getRole() != null ? user.getRole() : "user";
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getSchool() != null) user.setSchool(dto.getSchool());
        if (dto.getMajor() != null) user.setMajor(dto.getMajor());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        updateById(user);
    }

    @Override
    public UserVO getUserById(Long currentUserId, Long targetUserId) {
        User user = getById(targetUserId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserVO vo = toVO(user);
        vo.setIsFollowed(currentUserId != null && isFollowed(currentUserId, targetUserId));
        return vo;
    }

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能关注自己");
        }
        User target = getById(targetUserId);
        if (target == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (isFollowed(userId, targetUserId)) {
            return; // 已关注，幂等处理
        }
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowingId(targetUserId);
        followMapper.insert(follow);

        // 关注通知（type=3，targetId 为关注者，前端据此跳转其主页）
        User follower = getById(userId);
        String followerName = follower != null && follower.getNickname() != null
                ? follower.getNickname() : (follower != null ? follower.getUsername() : "有人");
        notificationService.createNotification(targetUserId, 3,
                "新的关注", followerName + " 关注了你", userId, 3);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        followMapper.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowingId, targetUserId));
    }

    @Override
    public boolean isFollowed(Long userId, Long targetUserId) {
        if (userId == null) return false;
        return followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowingId, targetUserId)) > 0;
    }

    @Override
    public Object getFollowingList(Long currentUserId, Long userId, int page, int size) {
        IPage<Follow> followPage = followMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId, userId)
                        .orderByDesc(Follow::getCreatedAt));

        List<UserVO> list = followPage.getRecords().stream()
                .map(f -> toBriefVO(getById(f.getFollowingId()), currentUserId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PageVO<UserVO> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(followPage.getTotal());
        vo.setPage(followPage.getCurrent());
        vo.setSize(followPage.getSize());
        vo.setPages(followPage.getPages());
        return vo;
    }

    @Override
    public Object getFansList(Long currentUserId, Long userId, int page, int size) {
        IPage<Follow> fansPage = followMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowingId, userId)
                        .orderByDesc(Follow::getCreatedAt));

        List<UserVO> list = fansPage.getRecords().stream()
                .map(f -> toBriefVO(getById(f.getUserId()), currentUserId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PageVO<UserVO> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(fansPage.getTotal());
        vo.setPage(fansPage.getCurrent());
        vo.setSize(fansPage.getSize());
        vo.setPages(fansPage.getPages());
        return vo;
    }

    /** 关注/粉丝列表项：轻量用户信息 + 当前用户是否已关注 */
    private UserVO toBriefVO(User user, Long currentUserId) {
        if (user == null || user.getStatus() == null || user.getStatus() == 0) return null;
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setSchool(user.getSchool());
        vo.setMajor(user.getMajor());
        vo.setBio(user.getBio());
        vo.setIsFollowed(currentUserId != null && !currentUserId.equals(user.getId())
                && isFollowed(currentUserId, user.getId()));
        return vo;
    }

    @Override
    public Object getUserPosts(Long currentUserId, Long targetUserId, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, targetUserId)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt);

        IPage<Post> pageResult = postMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostVO> list = pageResult.getRecords().stream()
                .map(p -> postToVO(p, currentUserId))
                .collect(Collectors.toList());

        PageVO<PostVO> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return vo;
    }

    @Override
    public Object getUserTrades(Long currentUserId, Long targetUserId, int page, int size) {
        LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trade::getUserId, targetUserId)
                .eq(Trade::getStatus, 1)
                .orderByDesc(Trade::getCreatedAt);

        IPage<Trade> pageResult = tradeMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(t -> {
            Map<String, Object> map = BeanUtil.beanToMap(t);
            if (t.getImages() != null) map.put("images", JSONUtil.toList(t.getImages(), String.class));
            User user = getById(t.getUserId());
            if (user != null) map.put("user", BeanUtil.copyProperties(user, UserVO.class));
            return map;
        }).collect(Collectors.toList());

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return vo;
    }

    @Override
    public Object getUserFavorites(Long userId, int targetType, int page, int size) {
        // 查询收藏记录
        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .orderByDesc(Favorite::getCreatedAt);

        IPage<Favorite> favPage = favoriteMapper.selectPage(new Page<>(page, size), favWrapper);
        List<Long> targetIds = favPage.getRecords().stream()
                .map(Favorite::getTargetId)
                .collect(Collectors.toList());

        if (targetIds.isEmpty()) {
            PageVO<Object> emptyVo = new PageVO<>();
            emptyVo.setList(List.of());
            emptyVo.setTotal(0L);
            emptyVo.setPages(0L);
            return emptyVo;
        }

        if (targetType == 1) {
            // 收藏的帖子
            List<Post> posts = postMapper.selectBatchIds(targetIds);
            List<PostVO> list = posts.stream()
                    .filter(p -> p.getStatus() == 1)
                    .map(p -> postToVO(p, userId))
                    .collect(Collectors.toList());

            PageVO<PostVO> vo = new PageVO<>();
            vo.setList(list);
            vo.setTotal(favPage.getTotal());
            vo.setPage(favPage.getCurrent());
            vo.setSize(favPage.getSize());
            vo.setPages(favPage.getPages());
            return vo;
        } else {
            // 收藏的商品
            List<Trade> trades = tradeMapper.selectBatchIds(targetIds);
            List<Map<String, Object>> list = trades.stream()
                    .filter(t -> t.getStatus() == 1)
                    .map(t -> {
                        Map<String, Object> map = BeanUtil.beanToMap(t);
                        if (t.getImages() != null) map.put("images", JSONUtil.toList(t.getImages(), String.class));
                        User user = getById(t.getUserId());
                        if (user != null) map.put("user", BeanUtil.copyProperties(user, UserVO.class));
                        return map;
                    }).collect(Collectors.toList());

            PageVO<Map<String, Object>> vo = new PageVO<>();
            vo.setList(list);
            vo.setTotal(favPage.getTotal());
            vo.setPage(favPage.getCurrent());
            vo.setSize(favPage.getSize());
            vo.setPages(favPage.getPages());
            return vo;
        }
    }

    private PostVO postToVO(Post post, Long currentUserId) {
        PostVO vo = BeanUtil.copyProperties(post, PostVO.class);
        if (post.getImages() != null) {
            vo.setImages(JSONUtil.toList(post.getImages(), String.class));
        }
        if (post.getTags() != null) {
            vo.setTags(JSONUtil.toList(post.getTags(), String.class));
        }
        User user = getById(post.getUserId());
        if (user != null) {
            vo.setUser(BeanUtil.copyProperties(user, UserVO.class));
        }
        return vo;
    }

    private UserVO toVO(User user) {
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);

        // 真实帖子数
        Long postCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, user.getId())
                        .eq(Post::getStatus, 1));
        vo.setPostCount(postCount.intValue());

        // 真实获赞数（所有帖子点赞数之和）
        vo.setLikedCount(postMapper.sumLikesByUserId(user.getId()));

        // 真实关注/粉丝数
        vo.setFollowCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, user.getId())).intValue());
        vo.setFansCount(followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowingId, user.getId())).intValue());
        return vo;
    }
}
