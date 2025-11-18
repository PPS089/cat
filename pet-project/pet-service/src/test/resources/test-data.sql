-- 测试数据初始化脚本
-- 用于PetHealthReminderTask的集成测试

-- 创建测试用户表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建测试宠物表
CREATE TABLE IF NOT EXISTS pets (
    pid INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(50),
    age INT,
    gender VARCHAR(10),
    weight DECIMAL(5,2),
    color VARCHAR(50),
    description TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建测试领养表
CREATE TABLE IF NOT EXISTS adoptions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    pet_id INT NOT NULL,
    adoption_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'adopted',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (pet_id) REFERENCES pets(pid)
);

-- 创建测试健康提醒表
CREATE TABLE IF NOT EXISTS health_alerts (
    health_id INT PRIMARY KEY AUTO_INCREMENT,
    pet_id INT NOT NULL,
    user_id INT NOT NULL,
    check_date TIMESTAMP NOT NULL,
    health_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    reminder_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'normal',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(pid),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入测试用户数据
INSERT IGNORE INTO users (id, username, password, email, phone) VALUES
(1, 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test1@example.com', '13800138001'),
(2, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'test2@example.com', '13800138002');

-- 插入测试宠物数据
INSERT IGNORE INTO pets (pid, name, species, breed, age, gender, weight, color, description) VALUES
(1, '小白', '狗', '金毛', 2, '雄性', 25.50, '金黄色', '活泼可爱的金毛犬'),
(2, '小黑', '猫', '英短', 1, '雌性', 4.20, '黑色', '安静的英短猫'),
(3, '花花', '兔子', '垂耳兔', 1, '雌性', 2.10, '花白', '温顺的垂耳兔');

-- 插入测试领养数据
INSERT IGNORE INTO adoptions (user_id, pet_id, adoption_date, status) VALUES
(1, 1, '2023-01-15 10:00:00', 'adopted'),
(1, 2, '2023-02-20 14:30:00', 'adopted'),
(2, 3, '2023-03-10 09:15:00', 'adopted');

-- 插入测试健康提醒数据 - 用于测试定时提醒
INSERT IGNORE INTO health_alerts (health_id, pet_id, user_id, check_date, health_type, description, reminder_time, status) VALUES
-- 即将到期的提醒（当前时间前后30秒内）
(1, 1, 1, '2023-11-01 10:00:00', '疫苗接种', '狂犬疫苗', DATE_ADD(NOW(), INTERVAL 10 SECOND), 'normal'),
(2, 2, 1, '2023-11-05 15:30:00', '驱虫', '体内驱虫', DATE_ADD(NOW(), INTERVAL 20 SECOND), 'attention'),
-- 未到期的提醒
(3, 1, 1, '2023-11-10 09:00:00', '体检', '年度体检', DATE_ADD(NOW(), INTERVAL 2 DAY), 'normal'),
(4, 3, 2, '2023-11-15 16:45:00', '洗澡', '常规洗澡', DATE_ADD(NOW(), INTERVAL 3 DAY), 'normal'),
-- 已提醒的记录
(5, 2, 1, '2023-10-20 11:00:00', '疫苗接种', '六联疫苗', DATE_SUB(NOW(), INTERVAL 1 DAY), 'reminded');

-- 插入测试健康提醒数据 - 用于测试过期清理
INSERT IGNORE INTO health_alerts (health_id, pet_id, user_id, check_date, health_type, description, reminder_time, status) VALUES
-- 正常但已过期的记录（超过默认过期天数）
(101, 1, 1, '2023-10-01 10:00:00', '疫苗接种', '狂犬疫苗', DATE_SUB(NOW(), INTERVAL 8 DAY), 'normal'),
(102, 2, 1, '2023-10-05 15:30:00', '驱虫', '体外驱虫', DATE_SUB(NOW(), INTERVAL 6 DAY), 'attention'),
-- 已过期但需要归档的记录（超过过期天数+归档阈值）
(201, 1, 1, '2023-09-01 10:00:00', '体检', '心脏检查', DATE_SUB(NOW(), INTERVAL 40 DAY), 'expired'),
(202, 2, 1, '2023-09-05 15:30:00', '疫苗接种', '六联疫苗', DATE_SUB(NOW(), INTERVAL 35 DAY), 'expired'),
-- 已归档但需要删除的记录（超过过期天数+归档阈值+删除阈值）
(301, 1, 1, '2022-10-01 10:00:00', '洗澡', '药浴', DATE_SUB(NOW(), INTERVAL 400 DAY), 'archived'),
(302, 2, 1, '2022-09-05 15:30:00', '驱虫', '体内驱虫', DATE_SUB(NOW(), INTERVAL 420 DAY), 'archived'),
-- 已提醒但已过期的记录
(401, 3, 2, '2023-10-20 11:00:00', '疫苗接种', '狂犬疫苗', DATE_SUB(NOW(), INTERVAL 8 DAY), 'reminded');

-- 创建测试视图，方便查询
CREATE OR REPLACE VIEW test_health_reminders AS
SELECT 
    ha.health_id,
    ha.pet_id,
    p.name AS pet_name,
    ha.user_id,
    u.username,
    ha.check_date,
    ha.health_type,
    ha.description,
    ha.reminder_time,
    ha.status,
    ha.created_at,
    ha.updated_at,
    CASE 
        WHEN ha.reminder_time IS NULL THEN '无提醒时间'
        WHEN ha.reminder_time < NOW() THEN '已过期'
        WHEN ha.reminder_time BETWEEN DATE_SUB(NOW(), INTERVAL 30 SECOND) AND DATE_ADD(NOW(), INTERVAL 30 SECOND) THEN '即将提醒'
        ELSE '未到期'
    END AS reminder_status
FROM health_alerts ha
JOIN pets p ON ha.pet_id = p.pid
JOIN users u ON ha.user_id = u.id;

-- 查询测试数据的SQL语句
-- 即将到期的提醒
-- SELECT * FROM test_health_reminders WHERE reminder_status = '即将提醒';

-- 已过期的提醒
-- SELECT * FROM test_health_reminders WHERE reminder_status = '已过期';

-- 需要归档的提醒（已过期超过30天）
-- SELECT * FROM test_health_reminders 
-- WHERE status = 'expired' AND reminder_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 需要删除的提醒（已归档超过365天）
-- SELECT * FROM test_health_reminders 
-- WHERE status = 'archived' AND reminder_time < DATE_SUB(NOW(), INTERVAL 395 DAY);