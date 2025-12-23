-- 添加寄养业务字段（start_date/end_date/status/deleted）
ALTER TABLE fosters
    ADD COLUMN `start_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '寄养开始时间' AFTER `sid`,
    ADD COLUMN `end_date` DATETIME NULL COMMENT '寄养结束时间' AFTER `start_date`,
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '寄养状态' AFTER `end_date`,
    ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记' AFTER `status`;

-- 初始化新增字段的历史数据
UPDATE fosters
SET start_date = COALESCE(start_date, create_time),
    end_date = CASE WHEN update_time IS NULL OR update_time = create_time THEN NULL ELSE update_time END,
    status = CASE WHEN update_time IS NULL OR update_time = create_time THEN 'ACTIVE' ELSE 'COMPLETED' END;

-- 索引优化
CREATE INDEX idx_fosters_status ON fosters(status);
CREATE INDEX idx_fosters_start_date ON fosters(start_date);

