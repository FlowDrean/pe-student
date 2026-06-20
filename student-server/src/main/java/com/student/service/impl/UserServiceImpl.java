package com.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Favorite;
import com.student.entity.Post;
import com.student.entity.Trade;
import com.student.entity.User;
import com.student.entity.dto.LoginDTO;
import com.student.entity.dto.UserDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.FavoriteMapper;
import com.student.mapper.PostMapper;
import com.student.mapper.TradeMapper;
import com.student.mapper.UserMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PostMapper postMapper;
    private final TradeMapper tradeMapper;
    private final FavoriteMapper favoriteMapper;

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
        // TODO: 查询是否关注
        vo.setIsFollowed(false);
        return vo;
    }

    @Override
    public boolean toggleFollow(Long userId, Long targetUserId) {
        // TODO: 实现关注/取消关注逻辑
        return true;
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

        // 关注/粉丝暂未实现 follow 表
        vo.setFollowCount(0);
        vo.setFansCount(0);
        return vo;
    }
}
