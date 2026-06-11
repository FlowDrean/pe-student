-- ============================================
-- 校园互助平台 — 演示数据
-- 包含带图片的帖子和商品
-- ============================================

USE student_platform;

-- 演示用户（密码都是 123456，bcrypt加密）
INSERT IGNORE INTO `user` (`id`, `username`, `nickname`, `avatar`, `email`, `school`, `major`, `bio`, `gender`, `status`) VALUES
(1, 'alice', '小艾同学', 'https://picsum.photos/200?random=1', 'alice@campus.edu', 'XX大学', '计算机科学', '热爱生活，喜欢分享~', 2, 1),
(2, 'bob', 'Bob学弟', 'https://picsum.photos/200?random=2', 'bob@campus.edu', 'XX大学', '软件工程', '二手达人，物尽其用', 1, 1),
(3, 'cathy', 'Cathy学姐', 'https://picsum.photos/200?random=3', 'cathy@campus.edu', 'XX大学', '设计艺术', '摄影爱好者', 2, 1);

-- 演示帖子（带图片）
INSERT INTO `post` (`id`, `user_id`, `title`, `content`, `images`, `category`, `tags`, `likes`, `comments`, `views`, `status`) VALUES
(1, 1, '📚 推荐几本特别好用的编程入门书', '最近在图书馆发现了几本特别适合新手的编程书，图文并茂，讲解很清晰。推荐给大家！\n\n1. 《Python编程：从入门到实践》\n2. 《JavaScript高级程序设计》\n3. 《算法图解》\n\n有需要的同学可以找我借阅~',
'["https://picsum.photos/400/300?random=10","https://picsum.photos/400/300?random=11","https://picsum.photos/400/300?random=12"]',
'study', '["编程","书籍","推荐"]', 42, 8, 230, 1),

(2, 2, '🏀 周末篮球约起！', '周六下午3点，东区篮球场，有没有一起打球的？不限水平，开心就好！\n\n上周来了十几个同学，氛围超好~',
'["https://picsum.photos/400/300?random=20","https://picsum.photos/400/300?random=21"]',
'life', '["篮球","运动","周末"]', 28, 15, 180, 1),

(3, 3, '🌸 校园春日摄影合集', '整理了这学期拍的校园风景照，春天的校园真的太美了！\n\n拍摄地点：樱花大道、图书馆前、人工湖畔\n设备：手机拍摄\n后期：Lightroom调色\n\n欢迎交流摄影技巧~',
'["https://picsum.photos/400/500?random=30","https://picsum.photos/400/500?random=31","https://picsum.photos/400/500?random=32","https://picsum.photos/400/500?random=33"]',
'life', '["摄影","校园","春天"]', 67, 23, 450, 1),

(4, 1, '🍜 食堂新窗口测评 — 麻辣香锅', '今天试了二食堂新开的麻辣香锅，点了中辣，味道很正宗！\n\n价格：人均25元\n推荐菜：牛肉、藕片、豆腐皮\n辣度：中辣就挺辣了，不太能吃辣的建议微辣\n\n附图是今天的战果，吃撑了😂',
'["https://picsum.photos/400/300?random=40","https://picsum.photos/400/300?random=41"]',
'food', '["美食","食堂","测评"]', 35, 12, 200, 1),

(5, 2, '💡 求助：数据库课程设计有人组队吗', '大三的数据库课设，想找2-3个队友一起做。\n\n选题方向：校园二手交易平台（正好和我们学的MySQL+Spring Boot相关）\n\n要求：认真负责，每周能抽出时间讨论\n\n有意向的同学评论区留言或私信我~',
'["https://picsum.photos/400/300?random=50"]',
'help', '["求助","组队","课设"]', 15, 20, 320, 1),

(6, 3, '🎨 免费分享设计素材和字体资源', '整理了一些常用的设计素材网站和免费字体，分享给大家。\n\n素材网站：\n- Unsplash（免费高清图片）\n- Pexels（免费视频+图片）\n- Freepik（矢量素材）\n\n免费字体：\n- 思源黑体/宋体\n- 阿里巴巴普惠体\n- 霞鹜文楷\n\n需要的同学评论区留邮箱，我发网盘链接~',
'["https://picsum.photos/400/300?random=60","https://picsum.photos/400/300?random=61","https://picsum.photos/400/300?random=62"]',
'study', '["设计","素材","免费"]', 53, 18, 380, 1);

-- 演示商品（带图片）
INSERT INTO `trade` (`id`, `user_id`, `title`, `description`, `price`, `original_price`, `images`, `category`, `condition`, `location`, `contact`, `status`, `views`) VALUES
(1, 1, '📕 高等数学第七版（同济）', '上学期买的，只用了一学期，基本全新。\n\n有几页用荧光笔画了重点，不影响阅读。\n附赠我自己整理的复习笔记一份！\n\n同济大学出版社，考研标配。',
25.00, 58.00,
'["https://picsum.photos/400/400?random=100","https://picsum.photos/400/400?random=101"]',
'book', 2, '东区宿舍2号楼', '微信：alice_study', 1, 120),

(2, 2, '💻 MacBook Air M1 8+256G', '2023年购入，使用一年多，电池循环120次。\n\n银色，无磕碰无划痕，一直贴膜带壳使用。\n\n原装充电器和数据线都在。\n\n适合日常办公学习，续航超强。\n\n毕业换新机故出。',
3200.00, 7999.00,
'["https://picsum.photos/400/400?random=200","https://picsum.photos/400/400?random=201","https://picsum.photos/400/400?random=202"]',
'electronics', 2, '西区宿舍5号楼', 'QQ：123456789', 1, 560),

(3, 3, '👗 全新连衣裙 M码', '上个月买的，试穿了一下发现不太适合我的风格。\n\n吊牌还没拆，全新！\n\n米白色，M码，适合160-165cm的女生。\n\n面料很舒服，春天的款式。',
55.00, 129.00,
'["https://picsum.photos/400/400?random=300","https://picsum.photos/400/400?random=301"]',
'clothing', 1, '南区女生宿舍', '微信：cathy_fashion', 1, 85),

(4, 1, '🪑 折叠书桌+台灯套装', '宿舍神器！折叠书桌很稳，在床上学习办公超方便。\n\n台灯三档调光，暖光护眼。\n\n两件打包出，不单卖。\n\n桌面有小划痕但不影响使用，介意勿拍。',
35.00, 99.00,
'["https://picsum.photos/400/400?random=400","https://picsum.photos/400/400?random=401","https://picsum.photos/400/400?random=402"]',
'daily', 3, '东区宿舍2号楼', '电话：13800138000', 1, 95),

(5, 2, '🎧 Sony WH-1000XM4 降噪耳机', '去年双十一买的，国行正品。\n\n殿堂级降噪效果，图书馆自习神器！\n\n箱说全，配件齐全。\n\n续航依然给力，充满电能用一个多星期。\n\n轻微使用痕迹，功能完好。',
850.00, 2299.00,
'["https://picsum.photos/400/400?random=500","https://picsum.photos/400/400?random=501"]',
'electronics', 2, '西区宿舍5号楼', 'QQ：123456789', 1, 340),

(6, 3, '📦 闲置化妆品收纳盒+小物件', '收纳盒九成新，隔层可自由调整。\n\n还有一些用不完的小物件：\n- 全新化妆刷一套\n- 发带三条\n- 小镜子一个\n\n打包价，超值！',
18.00, 68.00,
'["https://picsum.photos/400/400?random=600"]',
'daily', 2, '南区女生宿舍', '微信：cathy_fashion', 1, 62);
