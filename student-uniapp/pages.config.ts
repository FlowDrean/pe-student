import { defineUniPages } from '@uni-helper/vite-plugin-uni-pages'

export default defineUniPages({
  globalStyle: {
    navigationBarTextStyle: 'black',
    navigationBarTitleText: 'PE-Student',
    navigationBarBackgroundColor: '#ffffff',
    backgroundColor: '#f4f4f6',
  },
  easycom: {
    autoscan: true,
    custom: {
      '^wd-(.*)': 'wot-design-uni/components/wd-$1/wd-$1.vue',
    },
  },
  pages: [
    {
      path: 'pages/index/index',
      type: 'home',
      style: { navigationBarTitleText: '校园互助' },
    },
    {
      path: 'pages/search/index',
      type: 'page',
      style: { navigationBarTitleText: '搜索' },
    },
    {
      path: 'pages/category/index',
      type: 'page',
      style: { navigationBarTitleText: '分类' },
    },
    // 认证
    {
      path: 'pages/auth/login',
      type: 'page',
      style: { navigationBarTitleText: '登录', navigationStyle: 'custom' },
    },
    {
      path: 'pages/auth/register',
      type: 'page',
      style: { navigationBarTitleText: '注册', navigationStyle: 'custom' },
    },
    {
      path: 'pages/auth/forgot-password',
      type: 'page',
      style: { navigationBarTitleText: '找回密码', navigationStyle: 'custom' },
    },
    // 帖子
    {
      path: 'pages/post/detail',
      type: 'page',
      style: { navigationBarTitleText: '帖子详情' },
    },
    {
      path: 'pages/post/create',
      type: 'page',
      style: { navigationBarTitleText: '发布帖子' },
    },
    {
      path: 'pages/post/edit',
      type: 'page',
      style: { navigationBarTitleText: '编辑帖子' },
    },
    // 交易
    {
      path: 'pages/trade/index',
      type: 'page',
      style: { navigationBarTitleText: '二手市场' },
    },
    {
      path: 'pages/trade/detail',
      type: 'page',
      style: { navigationBarTitleText: '商品详情' },
    },
    {
      path: 'pages/trade/create',
      type: 'page',
      style: { navigationBarTitleText: '发布商品' },
    },
    {
      path: 'pages/trade/edit',
      type: 'page',
      style: { navigationBarTitleText: '编辑商品' },
    },
    // 消息
    {
      path: 'pages/message/index',
      type: 'page',
      style: { navigationBarTitleText: '消息' },
    },
    {
      path: 'pages/message/chat',
      type: 'page',
      style: { navigationBarTitleText: '聊天' },
    },
    {
      path: 'pages/message/notification',
      type: 'page',
      style: { navigationBarTitleText: '通知中心' },
    },
    // 用户
    {
      path: 'pages/user/index',
      type: 'page',
      style: { navigationBarTitleText: '我的' },
    },
    {
      path: 'pages/user/profile',
      type: 'page',
      style: { navigationBarTitleText: '个人主页' },
    },
    {
      path: 'pages/user/edit-profile',
      type: 'page',
      style: { navigationBarTitleText: '编辑资料' },
    },
    {
      path: 'pages/user/my-posts',
      type: 'page',
      style: { navigationBarTitleText: '我的帖子' },
    },
    {
      path: 'pages/user/my-trades',
      type: 'page',
      style: { navigationBarTitleText: '我的商品' },
    },
    {
      path: 'pages/user/favorites',
      type: 'page',
      style: { navigationBarTitleText: '我的收藏' },
    },
    {
      path: 'pages/user/settings',
      type: 'page',
      style: { navigationBarTitleText: '设置' },
    },
  ],
  subPackages: [],
  preloadRule: {},
})
