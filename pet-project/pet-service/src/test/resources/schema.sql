-- 测试用数据库模式
CREATE TABLE IF NOT EXISTS pet_records (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pid BIGINT,
    uid BIGINT,
    event_type VARCHAR(50),
    mood VARCHAR(50),
    description TEXT,
    location VARCHAR(255),
    record_time TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    media_url VARCHAR(500),
    media_type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS media_files (
    mid BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT,
    uid BIGINT,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_record_id ON media_files(record_id);