-- 插入模拟数据到pet_project数据库
USE pet_project;

-- 插入收容所数据
INSERT INTO shelters (sid, name, location, capacity) VALUES
(1, '阳光宠物收容所', '北京市朝阳区建国路88号', 100),
(2, '爱心动物之家', '上海市浦东新区世纪大道200号', 150),
(3, '小动物救助中心', '广州市天河区珠江新城', 80),
(4, '流浪动物之家', '深圳市南山区科技园', 120),
(5, '宠物保护协会', '成都市高新区天府大道', 90);

-- 插入用户数据
INSERT INTO users (id, username, phone, password_hash, introduce, headpic, email, created_at) VALUES
(1, 'zhangsan', '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa', '我是一名宠物爱好者，有5年的养宠经验', 'https://picsum.photos/seed/user1/200/200.jpg', 'zhangsan@example.com', NOW()),
(2, 'lisi', '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa', '喜欢小动物，希望给它们一个温暖的家', 'https://picsum.photos/seed/user2/200/200.jpg', 'lisi@example.com', NOW()),
(3, 'wangwu', '13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa', '兽医专业毕业，现在从事宠物护理工作', 'https://picsum.photos/seed/user3/200/200.jpg', 'wangwu@example.com', NOW()),
(4, 'zhaoliu', '13800138004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa', '家里有3只狗，2只猫，经验丰富', 'https://picsum.photos/seed/user4/200/200.jpg', 'zhaoliu@example.com', NOW()),
(5, 'chenqi', '13800138005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa', '喜欢救助流浪动物，希望能给它们一个家', 'https://picsum.photos/seed/user5/200/200.jpg', 'chenqi@example.com', NOW());

-- 插入宠物数据 - 狗狗
INSERT INTO pets (pid, name, species, breed, age, gender, status, shelter_id, img_url) VALUES
(1, '小黄', '狗', '中华田园犬', 2, '雄', 'UNADOPTED', 1, 'https://picsum.photos/seed/dog1/400/300.jpg'),
(2, '旺财', '狗', '金毛寻回犬', 3, '雄', 'UNADOPTED', 1, 'https://picsum.photos/seed/dog2/400/300.jpg'),
(3, '贝贝', '狗', '拉布拉多', 1, '雌', 'UNADOPTED', 2, 'https://picsum.photos/seed/dog3/400/300.jpg'),
(4, '豆豆', '狗', '柯基', 2, '雄', 'UNADOPTED', 2, 'https://picsum.photos/seed/dog4/400/300.jpg'),
(5, '毛毛', '狗', '萨摩耶', 4, '雌', 'UNADOPTED', 3, 'https://picsum.photos/seed/dog5/400/300.jpg'),
(6, '球球', '狗', '哈士奇', 3, '雄', 'UNADOPTED', 3, 'https://picsum.photos/seed/dog6/400/300.jpg'),
(7, '花花', '狗', '边牧', 2, '雌', 'UNADOPTED', 4, 'https://picsum.photos/seed/dog7/400/300.jpg'),
(8, '黑子', '狗', '德国牧羊犬', 5, '雄', 'UNADOPTED', 4, 'https://picsum.photos/seed/dog8/400/300.jpg'),
(9, '小白', '狗', '比熊', 1, '雌', 'UNADOPTED', 5, 'https://picsum.photos/seed/dog9/400/300.jpg'),
(10, '大壮', '狗', '罗威纳', 4, '雄', 'UNADOPTED', 5, 'https://picsum.photos/seed/dog10/400/300.jpg'),
(11, '妞妞', '狗', '泰迪', 2, '雌', 'UNADOPTED', 1, 'https://picsum.photos/seed/dog11/400/300.jpg'),
(12, '阿福', '狗', '柴犬', 3, '雄', 'UNADOPTED', 1, 'https://picsum.photos/seed/dog12/400/300.jpg'),
(13, '可乐', '狗', '法国斗牛犬', 2, '雄', 'UNADOPTED', 2, 'https://picsum.photos/seed/dog13/400/300.jpg'),
(14, '奶茶', '狗', '比熊', 1, '雌', 'UNADOPTED', 2, 'https://picsum.photos/seed/dog14/400/300.jpg'),
(15, '馒头', '狗', '松狮', 3, '雄', 'UNADOPTED', 3, 'https://picsum.photos/seed/dog15/400/300.jpg');

-- 插入宠物数据 - 猫咪
INSERT INTO pets (pid, name, species, breed, age, gender, status, shelter_id, img_url) VALUES
(16, '咪咪', '猫', '英国短毛猫', 1, '雌', 'UNADOPTED', 1, 'https://picsum.photos/seed/cat1/400/300.jpg'),
(17, '小白', '猫', '波斯猫', 2, '雌', 'UNADOPTED', 1, 'https://picsum.photos/seed/cat2/400/300.jpg'),
(18, '黑猫警长', '猫', '美国短毛猫', 3, '雄', 'UNADOPTED', 2, 'https://picsum.photos/seed/cat3/400/300.jpg'),
(19, '橘子', '猫', '橘猫', 2, '雄', 'UNADOPTED', 2, 'https://picsum.photos/seed/cat4/400/300.jpg'),
(20, '花花', '猫', '三花猫', 1, '雌', 'UNADOPTED', 3, 'https://picsum.photos/seed/cat5/400/300.jpg'),
(21, '灰灰', '猫', '俄罗斯蓝猫', 2, '雄', 'UNADOPTED', 3, 'https://picsum.photos/seed/cat6/400/300.jpg'),
(22, '雪球', '猫', '布偶猫', 1, '雌', 'UNADOPTED', 4, 'https://picsum.photos/seed/cat7/400/300.jpg'),
(23, '虎子', '猫', '孟加拉豹猫', 3, '雄', 'UNADOPTED', 4, 'https://picsum.photos/seed/cat8/400/300.jpg'),
(24, '小黑', '猫', '黑猫', 2, '雄', 'UNADOPTED', 5, 'https://picsum.photos/seed/cat9/400/300.jpg'),
(25, '奶牛', '猫', '奶牛猫', 1, '雌', 'UNADOPTED', 5, 'https://picsum.photos/seed/cat10/400/300.jpg'),
(26, '蓝蓝', '猫', '英国短毛猫', 2, '雌', 'UNADOPTED', 1, 'https://picsum.photos/seed/cat11/400/300.jpg'),
(27, '胖胖', '猫', '橘猫', 3, '雄', 'UNADOPTED', 1, 'https://picsum.photos/seed/cat12/400/300.jpg'),
(28, '咪宝', '猫', '暹罗猫', 1, '雌', 'UNADOPTED', 2, 'https://picsum.photos/seed/cat13/400/300.jpg'),
(29, '小狸', '猫', '狸花猫', 2, '雄', 'UNADOPTED', 2, 'https://picsum.photos/seed/cat14/400/300.jpg'),
(30, '银虎', '猫', '美国短毛猫', 3, '雄', 'UNADOPTED', 3, 'https://picsum.photos/seed/cat15/400/300.jpg');

-- 插入宠物数据 - 其他小动物
INSERT INTO pets (pid, name, species, breed, age, gender, status, shelter_id, img_url) VALUES
(31, '雪球', '兔子', '垂耳兔', 1, '雌', 'UNADOPTED', 1, 'https://picsum.photos/seed/rabbit1/400/300.jpg'),
(32, '跳跳', '兔子', '荷兰兔', 2, '雄', 'UNADOPTED', 1, 'https://picsum.photos/seed/rabbit2/400/300.jpg'),
(33, '小灰', '兔子', '安哥拉兔', 1, '雄', 'UNADOPTED', 2, 'https://picsum.photos/seed/rabbit3/400/300.jpg'),
(34, '棉花', '兔子', '狮子兔', 2, '雌', 'UNADOPTED', 2, 'https://picsum.photos/seed/rabbit4/400/300.jpg'),
(35, '小刺', '刺猬', '非洲迷你刺猬', 1, '雄', 'UNADOPTED', 3, 'https://picsum.photos/seed/hedgehog1/400/300.jpg'),
(36, '球球', '刺猬', '四趾刺猬', 2, '雌', 'UNADOPTED', 3, 'https://picsum.photos/seed/hedgehog2/400/300.jpg'),
(37, '奇奇', '仓鼠', '金丝熊', 1, '雄', 'UNADOPTED', 4, 'https://picsum.photos/seed/hamster1/400/300.jpg'),
(38, '豆豆', '仓鼠', '三线仓鼠', 1, '雌', 'UNADOPTED', 4, 'https://picsum.photos/seed/hamster2/400/300.jpg'),
(39, '花花', '仓鼠', '布丁仓鼠', 1, '雄', 'UNADOPTED', 5, 'https://picsum.photos/seed/hamster3/400/300.jpg'),
(40, '小白', '仓鼠', '银狐仓鼠', 1, '雌', 'UNADOPTED', 5, 'https://picsum.photos/seed/hamster4/400/300.jpg');

-- 插入宠物健康记录
INSERT INTO pet_health (health_id, pid, check_date, health_type, description, reminder_time, status, created_at, updated_at) VALUES
(1, 1, '2023-01-15 10:00:00', '常规体检', '身体健康，无异常', '2023-07-15 10:00:00', 'normal', NOW(), NOW()),
(2, 2, '2023-02-20 14:30:00', '疫苗接种', '完成狂犬疫苗接种', '2024-02-20 14:30:00', 'normal', NOW(), NOW()),
(3, 3, '2023-03-10 09:15:00', '驱虫', '完成体内外驱虫', '2023-06-10 09:15:00', 'normal', NOW(), NOW()),
(4, 4, '2023-01-25 16:00:00', '常规体检', '轻微皮肤过敏，已用药', '2023-04-25 16:00:00', 'treatment', NOW(), NOW()),
(5, 5, '2023-02-05 11:30:00', '疫苗接种', '完成六联疫苗接种', '2024-02-05 11:30:00', 'normal', NOW(), NOW()),
(6, 6, '2023-03-15 13:45:00', '常规体检', '身体健康，活力良好', '2023-09-15 13:45:00', 'normal', NOW(), NOW()),
(7, 7, '2023-01-30 10:20:00', '驱虫', '完成体内外驱虫', '2023-04-30 10:20:00', 'normal', NOW(), NOW()),
(8, 8, '2023-02-14 15:10:00', '常规体检', '关节轻微不适，建议控制运动量', '2023-05-14 15:10:00', 'attention', NOW(), NOW()),
(9, 9, '2023-03-20 09:30:00', '疫苗接种', '完成三联疫苗接种', '2024-03-20 09:30:00', 'normal', NOW(), NOW()),
(10, 10, '2023-01-10 14:00:00', '常规体检', '身体健康，体重正常', '2023-07-10 14:00:00', 'normal', NOW(), NOW());

-- 插入宠物记录
INSERT INTO pet_records (record_id, pid, uid, event_type, mood, description, location, record_time, created_at, updated_at) VALUES
(1, 1, 1, '日常记录', '开心', '今天小黄学会了握手，非常聪明', '收容所活动区', '2023-05-01 10:30:00', NOW(), NOW()),
(2, 2, 2, '医疗记录', '正常', '旺财今天完成了疫苗接种，状态良好', '宠物医院', '2023-05-02 14:20:00', NOW(), NOW()),
(3, 3, 3, '训练记录', '兴奋', '贝贝学会了坐下和趴下，进步很快', '训练场', '2023-05-03 16:45:00', NOW(), NOW()),
(4, 4, 4, '社交记录', '友好', '豆豆和其他狗狗相处融洽，性格温和', '收容所社交区', '2023-05-04 11:15:00', NOW(), NOW()),
(5, 5, 5, '日常记录', '放松', '毛毛喜欢在阳光下打盹，非常可爱', '收容所花园', '2023-05-05 15:30:00', NOW(), NOW()),
(6, 16, 1, '日常记录', '慵懒', '咪咪今天很懒，大部分时间都在睡觉', '猫咪休息区', '2023-05-06 13:20:00', NOW(), NOW()),
(7, 17, 2, '医疗记录', '不适', '小白今天食欲不振，已经安排医生检查', '宠物医院', '2023-05-07 09:45:00', NOW(), NOW()),
(8, 18, 3, '玩耍记录', '活跃', '黑猫警长今天玩逗猫棒很兴奋', '猫咪活动区', '2023-05-08 16:10:00', NOW(), NOW()),
(9, 19, 4, '饮食记录', '满足', '橘子今天吃了罐头，很满足的样子', '喂食区', '2023-05-09 12:30:00', NOW(), NOW()),
(10, 20, 5, '日常记录', '好奇', '花花对新玩具很感兴趣，一直在探索', '猫咪活动区', '2023-05-10 14:50:00', NOW(), NOW());

-- 插入媒体文件
INSERT INTO media_files (mid, record_id, uid, file_name, file_path, media_type, file_size, created_at) VALUES
(1, 1, 1, '小黄握手.jpg', '/uploads/media/小黄握手.jpg', 'image', 1024576, NOW()),
(2, 2, 2, '旺财疫苗.jpg', '/uploads/media/旺财疫苗.jpg', 'image', 987654, NOW()),
(3, 3, 3, '贝贝训练.jpg', '/uploads/media/贝贝训练.jpg', 'image', 1123456, NOW()),
(4, 4, 4, '豆豆社交.jpg', '/uploads/media/豆豆社交.jpg', 'image', 1234567, NOW()),
(5, 5, 5, '毛毛打盹.jpg', '/uploads/media/毛毛打盹.jpg', 'image', 987654, NOW()),
(6, 6, 1, '咪咪睡觉.jpg', '/uploads/media/咪咪睡觉.jpg', 'image', 876543, NOW()),
(7, 7, 2, '小白检查.jpg', '/uploads/media/小白检查.jpg', 'image', 1098765, NOW()),
(8, 8, 3, '黑猫玩耍.jpg', '/uploads/media/黑猫玩耍.jpg', 'image', 1187654, NOW()),
(9, 9, 4, '橘子吃饭.jpg', '/uploads/media/橘子吃饭.jpg', 'image', 965432, NOW()),
(10, 10, 5, '花花玩具.jpg', '/uploads/media/花花玩具.jpg', 'image', 1076543, NOW());

-- 插入领养记录
INSERT INTO adoptions (aid, pid, uid, adopt_date) VALUES
(1, 11, 1, '2023-04-15 10:00:00'),
(2, 12, 2, '2023-04-20 14:30:00'),
(3, 13, 3, '2023-04-25 16:00:00'),
(4, 14, 4, '2023-05-01 09:30:00'),
(5, 15, 5, '2023-05-05 11:00:00');

-- 插入寄养记录
INSERT INTO fosters (fid, pid, uid, sid, start_date, end_date) VALUES
(1, 21, 1, 1, '2023-04-10 10:00:00', '2023-04-20 10:00:00'),
(2, 22, 2, 2, '2023-04-15 14:30:00', '2023-04-25 14:30:00'),
(3, 23, 3, 3, '2023-04-20 16:00:00', NULL),
(4, 24, 4, 4, '2023-04-25 09:30:00', '2023-05-05 09:30:00'),
(5, 25, 5, 5, '2023-05-01 11:00:00', NULL);

-- 插入文章
INSERT INTO articles (id, title, content, summary, author, cover_image, view_count, status, created_at, updated_at) VALUES
(1, '如何照顾新领养的宠物', '领养宠物是一件令人兴奋的事情，但也需要做好充分的准备。首先，您需要为宠物准备基本的生活用品，如食物、水盆、床铺等。其次，要为宠物创造一个安全舒适的环境。新宠物到家后，给它一些时间适应新环境，不要强迫它与人互动。定期带宠物去兽医那里检查，确保它的健康状况良好。最重要的是，给予宠物足够的关爱和耐心，建立信任关系。', '本文介绍了如何照顾新领养的宠物，包括准备工作、环境适应和日常护理。', '张医生', 'https://picsum.photos/seed/article1/800/400.jpg', 1250, 'PUBLISHED', NOW(), NOW()),
(2, '宠物训练的基本技巧', '宠物训练是建立良好人宠关系的重要环节。正向强化是最有效的训练方法，通过奖励良好行为来鼓励宠物重复这些行为。训练时要保持一致性，使用相同的指令和手势。短时间、高频率的训练比长时间、低频率的训练更有效。始终保持耐心，不要对宠物使用暴力或惩罚。记住，每只宠物的学习速度不同，要根据宠物的个性调整训练方法。', '本文分享了宠物训练的基本技巧，包括正向强化、一致性和耐心的重要性。', '李训练师', 'https://picsum.photos/seed/article2/800/400.jpg', 980, 'PUBLISHED', NOW(), NOW()),
(3, '宠物常见健康问题及预防', '宠物健康是每个宠物主人关心的话题。定期体检可以及早发现潜在健康问题。疫苗接种是预防传染病的重要措施，要按照兽医建议的时间表进行。寄生虫防治也不容忽视，定期进行体内外驱虫。注意观察宠物的食欲、精神状态和排泄情况，这些是判断健康状况的重要指标。提供均衡的饮食和适量的运动，有助于维持宠物的整体健康。', '本文介绍了宠物常见健康问题及预防措施，包括定期体检、疫苗接种和寄生虫防治。', '王兽医', 'https://picsum.photos/seed/article3/800/400.jpg', 1520, 'PUBLISHED', NOW(), NOW()),
(4, '如何选择适合的宠物食品', '选择适合的宠物食品对宠物健康至关重要。首先要考虑宠物的年龄、体型和活动水平，不同阶段和体型的宠物需要不同的营养配比。查看食品成分表，优质蛋白质应该排在第一位。避免含有过多填充物和人工添加剂的食品。湿粮和干粮各有优缺点，可以根据宠物的喜好和健康状况选择。换食时要循序渐进，避免引起肠胃不适。如有特殊健康问题，可咨询兽医选择处方粮。', '本文指导如何选择适合的宠物食品，包括考虑因素、成分分析和换食技巧。', '赵营养师', 'https://picsum.photos/seed/article4/800/400.jpg', 860, 'PUBLISHED', NOW(), NOW()),
(5, '宠物行为解读：了解你的宠物', '宠物的行为是它们与人类沟通的方式。尾巴摆动不一定表示开心，狗狗的尾巴摆动方式和位置不同，含义也不同。猫咪的呼噜声通常表示满足，但也可能在紧张或疼痛时发出。宠物舔舐主人可能是表示喜爱，也可能是寻求关注。了解宠物的肢体语言和日常习惯，有助于更好地理解它们的需求和情绪。当宠物行为突然改变时，可能是健康问题的信号，需要及时关注。', '本文解读了宠物的常见行为，帮助主人更好地理解自己的宠物。', '孙行为学家', 'https://picsum.photos/seed/article5/800/400.jpg', 1100, 'PUBLISHED', NOW(), NOW());

-- 插入登录历史
INSERT INTO login_history (id, user_id, login_time, ip_address, user_agent, device, location, status) VALUES
(1, 1, '2023-06-01 09:00:00', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Windows PC', '北京', 'SUCCESS'),
(2, 2, '2023-06-01 10:30:00', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'MacBook', '上海', 'SUCCESS'),
(3, 3, '2023-06-01 14:15:00', '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15', 'iPhone', '广州', 'SUCCESS'),
(4, 4, '2023-06-01 16:45:00', '192.168.1.103', 'Mozilla/5.0 (Android 12; Mobile; rv:68.0) Gecko/68.0 Firefox/88.0', 'Android手机', '深圳', 'SUCCESS'),
(5, 5, '2023-06-01 18:20:00', '192.168.1.104', 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15', 'iPad', '成都', 'SUCCESS'),
(6, 1, '2023-06-02 08:30:00', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'Windows PC', '北京', 'SUCCESS'),
(7, 2, '2023-06-02 11:00:00', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'MacBook', '上海', 'SUCCESS'),
(8, 3, '2023-06-02 15:30:00', '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15', 'iPhone', '广州', 'SUCCESS'),
(9, 4, '2023-06-02 17:45:00', '192.168.1.103', 'Mozilla/5.0 (Android 12; Mobile; rv:68.0) Gecko/68.0 Firefox/88.0', 'Android手机', '深圳', 'SUCCESS'),
(10, 5, '2023-06-02 19:15:00', '192.168.1.104', 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15', 'iPad', '成都', 'SUCCESS');