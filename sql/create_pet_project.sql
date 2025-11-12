-- 创建pet_project数据库
CREATE DATABASE IF NOT EXISTS pet_project DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pet_project;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    phone VARCHAR(20) NOT NULL COMMENT '电话号码',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    introduce VARCHAR(200) COMMENT '自我介绍',
    headpic VARCHAR(200) COMMENT '头像URL',
    email VARCHAR(100) COMMENT '电子邮箱',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 收容所表
CREATE TABLE IF NOT EXISTS shelters (
    sid INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '收容所名称',
    location VARCHAR(200) COMMENT '收容所地址',
    capacity INT COMMENT '容量',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收容所表';

-- 宠物表
CREATE TABLE IF NOT EXISTS pets (
    pid INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '宠物名称',
    species VARCHAR(50) NOT NULL COMMENT '物种',
    breed VARCHAR(50) COMMENT '品种',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别',
    status ENUM('UNADOPTED','ADOPTED','FOSTERING','DEAD','FOSTER_END') COMMENT '状态',
    shelter_id INT NULL COMMENT '收容所ID',
    img_url VARCHAR(50) COMMENT '图片URL',
    INDEX idx_species (species),
    INDEX idx_status (status),
    INDEX idx_shelter_id (shelter_id),
    FOREIGN KEY (shelter_id) REFERENCES shelters(sid) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- 宠物健康表
CREATE TABLE IF NOT EXISTS pet_health (
    health_id INT AUTO_INCREMENT PRIMARY KEY,
    pid INT NOT NULL COMMENT '宠物ID',
    check_date DATETIME NOT NULL COMMENT '检查日期',
    health_type VARCHAR(50) COMMENT '健康类型',
    description TEXT COMMENT '描述',
    reminder_time DATETIME COMMENT '提醒时间',
    status VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_pid (pid),
    INDEX idx_check_date (check_date),
    INDEX idx_reminder_time (reminder_time),
    FOREIGN KEY (pid) REFERENCES pets(pid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物健康表';

-- 领养表
CREATE TABLE IF NOT EXISTS adoptions (
    aid INT AUTO_INCREMENT PRIMARY KEY,
    pid INT NOT NULL COMMENT '宠物ID',
    uid INT NOT NULL COMMENT '用户ID',
    adopt_date DATETIME NOT NULL COMMENT '领养日期',
    INDEX idx_pid (pid),
    INDEX idx_uid (uid),
    UNIQUE KEY uk_pid (pid),
    FOREIGN KEY (pid) REFERENCES pets(pid) ON DELETE CASCADE,
    FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养表';

-- 寄养表
CREATE TABLE IF NOT EXISTS fosters (
    fid INT AUTO_INCREMENT PRIMARY KEY,
    pid INT NOT NULL COMMENT '宠物ID',
    uid INT NOT NULL COMMENT '用户ID',
    sid INT COMMENT '收容所ID',
    start_date DATETIME NOT NULL COMMENT '寄养开始日期',
    end_date DATETIME COMMENT '寄养结束日期',
    INDEX idx_pid (pid),
    INDEX idx_uid (uid),
    INDEX idx_sid (sid),
    FOREIGN KEY (pid) REFERENCES pets(pid) ON DELETE CASCADE,
    FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sid) REFERENCES shelters(sid) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寄养表';

-- 文章表
CREATE TABLE IF NOT EXISTS articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    summary VARCHAR(500) COMMENT '摘要',
    author VARCHAR(100) COMMENT '作者',
    cover_image VARCHAR(300) COMMENT '封面图片',
    view_count INT COMMENT '浏览次数',
    status ENUM('PUBLISHED','DRAFT') COMMENT '状态(PUBLISHED,DRAFT)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 宠物记录表
CREATE TABLE IF NOT EXISTS pet_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    pid INT NOT NULL COMMENT '宠物ID',
    uid INT COMMENT '用户ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    mood VARCHAR(20) COMMENT '心情',
    description TEXT COMMENT '描述',
    location VARCHAR(200) COMMENT '位置',
    record_time DATETIME NOT NULL COMMENT '记录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_pid (pid),
    INDEX idx_uid (uid),
    INDEX idx_record_time (record_time),
    FOREIGN KEY (pid) REFERENCES pets(pid) ON DELETE CASCADE,
    FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物记录表';

-- 媒体文件表
CREATE TABLE IF NOT EXISTS media_files (
    mid INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NULL COMMENT '记录ID',
    uid INT NOT NULL COMMENT '用户ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    media_type VARCHAR(20) NOT NULL COMMENT '媒体类型',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_record_id (record_id),
    INDEX idx_uid (uid),
    FOREIGN KEY (record_id) REFERENCES pet_records(record_id) ON DELETE SET NULL,
    FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件表';

-- 登录历史表
CREATE TABLE IF NOT EXISTS login_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    device VARCHAR(200) COMMENT '设备',
    location VARCHAR(100) COMMENT '位置',
    status ENUM('SUCCESS','FAILED') NOT NULL COMMENT '登录状态(SUCCESS,FAILED)',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录历史表';