package com.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Favorite;
import com.student.entity.LikeRecord;
import com.student.entity.Post;
import com.student.entity.User;
import com.student.entity.dto.PostDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.CommentMapper;
import com.student.mapper.FavoriteMapper;
import com.student.mapper.LikeRecordMapper;
import com.student.mapper.PostMapper;
import com.student.mapper.UserMapper;
import com.student.service.NotificationService;
import com.student.service.PostService;
import com.student.vo.PageVO;
import com.student.vo.PostVO;
import com.student.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    @Override
    public Object getPostList(Long userId, String category, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Post::getCategory, category);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        IPage<Post> pageResult = page(new Page<>(page, size), wrapper);
        List<PostVO> list = pageResult.getRecords().stream()
                .map(p -> toVO(p, userId))
                .collect(Collectors.toList());

        // 用真实的评论数覆盖
        fillRealCommentCounts(list);

        PageVO<PostVO> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return vo;
    }

    @Override
    public PostVO getPostDetail(Long userId, Long postId) {
        Post post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        // 增加浏览量
        baseMapper.incrViews(postId);
        post.setViews(post.getViews() + 1);
        PostVO vo = toVO(post, userId);
        // 真实评论数
        vo.setComments(commentMapper.countByPostId(postId));
        return vo;
    }

    @Override
    @Transactional
    public PostVO createPost(Long userId, PostDTO dto) {
        Post post = BeanUtil.copyProperties(dto, Post.class);
        post.setUserId(userId);
        post.setImages(dto.getImages() != null ? JSONUtil.toJsonStr(dto.getImages()) : null);
        post.setTags(dto.getTags() != null ? JSONUtil.toJsonStr(dto.getTags()) : null);
        post.setLikes(0);
        post.setComments(0);
        post.setViews(0);
        post.setStatus(1);
        save(post);
        return toVO(post, userId);
    }

    @Override
    @Transactional
    public void updatePost(Long userId, Long postId, PostDTO dto) {
        Post post = getById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);

        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        if (dto.getCategory() != null) post.setCategory(dto.getCategory());
        if (dto.getImages() != null) post.setImages(JSONUtil.toJsonStr(dto.getImages()));
        if (dto.getTags() != null) post.setTags(JSONUtil.toJsonStr(dto.getTags()));
        updateById(post);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = getById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        if (!post.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        post.setStatus(0);
        updateById(post);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        Post post = getById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);

        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, postId)
                .eq(LikeRecord::getTargetType, 1);

        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null) {
            likeRecordMapper.deleteById(record.getId());
            baseMapper.incrLikes(postId, -1);
            return false;
        } else {
            LikeRecord newRecord = new LikeRecord();
            newRecord.setUserId(userId);
            newRecord.setTargetId(postId);
            newRecord.setTargetType(1);
            likeRecordMapper.insert(newRecord);
            baseMapper.incrLikes(postId, 1);

            // 发送通知
            if (!post.getUserId().equals(userId)) {
                User liker = userMapper.selectById(userId);
                String likerName = liker != null ? (liker.getNickname() != null ? liker.getNickname() : liker.getUsername()) : "有人";
                notificationService.createNotification(post.getUserId(), 1,
                        "新的点赞", likerName + " 赞了你的帖子「" + post.getTitle() + "」", postId, 1);
            }
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long postId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetId, postId)
                .eq(Favorite::getTargetType, 1);

        Favorite fav = favoriteMapper.selectOne(wrapper);
        if (fav != null) {
            favoriteMapper.deleteById(fav.getId());
            return false;
        } else {
            Favorite newFav = new Favorite();
            newFav.setUserId(userId);
            newFav.setTargetId(postId);
            newFav.setTargetType(1);
            favoriteMapper.insert(newFav);
            return true;
        }
    }

    @Override
    public Object searchPosts(String keyword, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword))
                .orderByDesc(Post::getCreatedAt);

        IPage<Post> pageResult = page(new Page<>(page, size), wrapper);
        List<PostVO> list = pageResult.getRecords().stream()
                .map(p -> toVO(p, null))
                .collect(Collectors.toList());

        fillRealCommentCounts(list);

        PageVO<PostVO> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return vo;
    }

    @Override
    public Object getHotPosts(int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .orderByDesc(Post::getLikes)
                .orderByDesc(Post::getComments);

        IPage<Post> pageResult = page(new Page<>(page, size), wrapper);
        List<PostVO> list = pageResult.getRecords().stream()
                .map(p -> toVO(p, null))
                .collect(Collectors.toList());

        fillRealCommentCounts(list);

        PageVO<PostVO> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return vo;
    }

    /**
     * 用 comment 表中的真实数量覆盖 PostVO.comments
     */
    private void fillRealCommentCounts(List<PostVO> list) {
        if (list == null || list.isEmpty()) return;
        List<Long> postIds = list.stream().map(PostVO::getId).collect(Collectors.toList());
        List<Map<String, Object>> counts = commentMapper.countByPostIds(postIds);
        Map<Long, Integer> countMap = new java.util.HashMap<>();
        for (Map<String, Object> row : counts) {
            Long postId = ((Number) row.get("postId")).longValue();
            int cnt = ((Number) row.get("cnt")).intValue();
            countMap.put(postId, cnt);
        }
        for (PostVO vo : list) {
            vo.setComments(countMap.getOrDefault(vo.getId(), 0));
        }
    }

    private PostVO toVO(Post post, Long currentUserId) {
        PostVO vo = BeanUtil.copyProperties(post, PostVO.class);
        // 解析 JSON
        if (post.getImages() != null) {
            vo.setImages(JSONUtil.toList(post.getImages(), String.class));
        }
        if (post.getTags() != null) {
            vo.setTags(JSONUtil.toList(post.getTags(), String.class));
        }

        // 用户信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            UserVO uv = BeanUtil.copyProperties(user, UserVO.class);
            vo.setUser(uv);
        }

        // 是否点赞/收藏
        if (currentUserId != null) {
            vo.setIsLiked(likeRecordMapper.selectCount(
                    new LambdaQueryWrapper<LikeRecord>()
                            .eq(LikeRecord::getUserId, currentUserId)
                            .eq(LikeRecord::getTargetId, post.getId())
                            .eq(LikeRecord::getTargetType, 1)) > 0);

            vo.setIsFavorited(favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUserId)
                            .eq(Favorite::getTargetId, post.getId())
                            .eq(Favorite::getTargetType, 1)) > 0);
        }

        return vo;
    }
}
