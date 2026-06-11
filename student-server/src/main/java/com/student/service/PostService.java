package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Post;
import com.student.entity.dto.PostDTO;
import com.student.vo.PostVO;

public interface PostService extends IService<Post> {

    /** 分页获取帖子列表 */
    Object getPostList(Long userId, String category, int page, int size);

    /** 获取帖子详情 */
    PostVO getPostDetail(Long userId, Long postId);

    /** 发布帖子 */
    PostVO createPost(Long userId, PostDTO dto);

    /** 编辑帖子 */
    void updatePost(Long userId, Long postId, PostDTO dto);

    /** 删除帖子 */
    void deletePost(Long userId, Long postId);

    /** 点赞/取消点赞 */
    boolean toggleLike(Long userId, Long postId);

    /** 收藏/取消收藏 */
    boolean toggleFavorite(Long userId, Long postId);

    /** 获取热门帖子 */
    Object getHotPosts(int page, int size);

    /** 搜索帖子 */
    Object searchPosts(String keyword, int page, int size);
}
