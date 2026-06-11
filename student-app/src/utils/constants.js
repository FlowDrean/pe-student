/* ============================================
   常量定义
   ============================================ */

// 帖子分类 — icon 对应 src/utils/icons.js 中的图标名称
export const POST_CATEGORIES = [
  { key: 'help', label: '求助', icon: 'help', color: '#f05a28' },
  { key: 'food', label: '美食', icon: 'food', color: '#ff7a4d' },
  { key: 'study', label: '学习', icon: 'study', color: '#07C160' },
  { key: 'life', label: '生活', icon: 'life', color: '#14b88a' },
  { key: 'other', label: '其他', icon: 'chat', color: '#868e96' },
]

// 二手商品分类 — icon 对应 src/utils/icons.js 中的图标名称
export const TRADE_CATEGORIES = [
  { key: 'book', label: '书籍', icon: 'book' },
  { key: 'electronics', label: '电子', icon: 'electronics' },
  { key: 'daily', label: '日用品', icon: 'daily' },
  { key: 'clothing', label: '衣物', icon: 'clothing' },
  { key: 'other', label: '其他', icon: 'package' },
]

// 商品成色
export const CONDITIONS = [
  { key: 1, label: '全新' },
  { key: 2, label: '九成新' },
  { key: 3, label: '八成新' },
  { key: 4, label: '七成新及以下' },
]

// 交易状态
export const TRADE_STATUS = {
  OFFLINE: 0,
  SELLING: 1,
  SOLD: 2,
  DEALING: 3,
}

export const TRADE_STATUS_LABELS = {
  0: '已下架',
  1: '在售',
  2: '已售',
  3: '交易中',
}

// 消息类型
export const MSG_TYPE = {
  TEXT: 1,
  IMAGE: 2,
  SYSTEM: 3,
}

// 通知类型
export const NOTI_TYPE = {
  LIKE: 1,
  COMMENT: 2,
  FOLLOW: 3,
  SYSTEM: 4,
  TRADE: 5,
}

// 点赞目标类型
export const LIKE_TARGET = {
  POST: 1,
  COMMENT: 2,
}

// 收藏目标类型
export const FAVORITE_TARGET = {
  POST: 1,
  TRADE: 2,
}

// 用户性别
export const GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2,
}

// 帖子状态
export const POST_STATUS = {
  DELETED: 0,
  NORMAL: 1,
  REVIEWING: 2,
}

// 分页默认值
export const PAGE_DEFAULT = 1
export const SIZE_DEFAULT = 10

// 上传限制
export const UPLOAD_MAX_IMAGES = 9
export const UPLOAD_MAX_SIZE = 10 * 1024 * 1024 // 10MB

// TabBar 配置
export const TAB_BARS = [
  { key: 'home', label: '首页', icon: 'home', path: '/' },
  { key: 'trade', label: '市场', icon: 'shop', path: '/trade' },
  { key: 'create', label: '发布', icon: 'plus', path: '/post/create', special: true },
  { key: 'message', label: '消息', icon: 'chat', path: '/message' },
  { key: 'user', label: '我的', icon: 'user', path: '/user' },
]
