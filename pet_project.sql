SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for adoptions
-- ----------------------------
DROP TABLE IF EXISTS `adoptions`;
CREATE TABLE `adoptions`  (
  `aid` int NOT NULL AUTO_INCREMENT COMMENT '领养ID',
  `pid` int NOT NULL COMMENT '宠物ID',
  `uid` int NOT NULL COMMENT '用户ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `reviewer_id` int NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `adopt_date` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领养时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`aid`) USING BTREE,
  UNIQUE INDEX `uk_adoptions_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_adoptions_uid`(`uid` ASC) USING BTREE,
  CONSTRAINT `fk_adoptions_pet` FOREIGN KEY (`pid`) REFERENCES `pets` (`pid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_adoptions_user` FOREIGN KEY (`uid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of adoptions
-- ----------------------------

-- ----------------------------
-- Table structure for articles
-- ----------------------------
DROP TABLE IF EXISTS `articles`;
CREATE TABLE `articles`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `author` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `view_count` int NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLISHED',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `shelter_id` int NULL DEFAULT NULL COMMENT '作者id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of articles
-- ----------------------------
INSERT INTO `articles` VALUES (1, '领养须知：给流浪动物一个家', '领养流程、准备清单与注意事项示例内容。', '管理员', 'https://images.unsplash.com/photo-1517849845537-4d257902454a', 0, 'PUBLISHED', '2025-12-16 15:30:13', '2025-12-16 15:30:13', NULL);
INSERT INTO `articles` VALUES (2, '寄养指南：如何选择合适的收容所', '寄养流程、费用与注意事项示例内容。', '管理员', 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131', 0, 'PUBLISHED', '2025-12-16 15:30:13', '2025-12-16 15:30:13', NULL);

-- ----------------------------
-- Table structure for species
-- ----------------------------
DROP TABLE IF EXISTS `species`;
CREATE TABLE `species`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of species
-- ----------------------------
INSERT INTO `species` VALUES (1, '狗', '犬科宠物', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `species` VALUES (2, '猫', '猫科宠物', '2025-12-16 15:30:13', '2025-12-16 15:30:13');

-- ----------------------------
-- Table structure for breed
-- ----------------------------
DROP TABLE IF EXISTS `breed`;
CREATE TABLE `breed`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `species_id` int NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_breed_species`(`species_id` ASC) USING BTREE,
  CONSTRAINT `fk_breed_species` FOREIGN KEY (`species_id`) REFERENCES `species` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of breed
-- ----------------------------
INSERT INTO `breed` VALUES (1, 1, '金毛寻回犬', '友好聪明的大型犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (2, 1, '柴犬', '警觉独立的中型犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (3, 1, '柯基', '活泼亲人的短腿牧牛犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (4, 1, '拉布拉多', '温顺友善的寻回犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (5, 1, '哈士奇', '精力旺盛的雪橇犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (6, 1, '博美', '体型小巧的伴侣犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (7, 1, '泰迪', '卷毛活泼的伴侣犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (8, 1, '边牧', '高智商的牧羊犬', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (9, 2, '布偶猫', '温柔黏人的长毛猫', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (10, 2, '橘猫', '家养短毛猫（常见毛色）', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (11, 2, '狸花猫', '常见家猫花色（中华田园猫）', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (12, 2, '英短蓝猫', '英国短毛猫的蓝色被毛型', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (13, 2, '英国短毛猫', '经典短毛家猫品种', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (14, 2, '美短虎斑', '美国短毛猫的虎斑花纹型', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (15, 2, '美国短毛猫', '适应性强的短毛猫品种', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (16, 2, '暹罗猫', '重点色短毛猫品种', '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `breed` VALUES (17, 2, '波斯猫', '长毛扁脸品种猫', '2025-12-16 15:30:13', '2025-12-16 15:30:13');

-- ----------------------------
-- Table structure for fosters
-- ----------------------------
DROP TABLE IF EXISTS `fosters`;
CREATE TABLE `fosters`  (
  `fid` int NOT NULL AUTO_INCREMENT COMMENT '寄养记录ID',
  `pid` int NOT NULL COMMENT '宠物ID',
  `uid` int NOT NULL COMMENT '拥有者ID',
  `sid` int NOT NULL COMMENT '寄养收容所',
  `start_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '寄养开始',
  `end_date` datetime NULL DEFAULT NULL COMMENT '寄养结束',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/ONGOING/COMPLETED/REJECTED',
  `reviewer_id` int NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`fid`) USING BTREE,
  INDEX `idx_fosters_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_fosters_uid`(`uid` ASC) USING BTREE,
  INDEX `idx_fosters_status`(`status` ASC) USING BTREE,
  INDEX `idx_fosters_start_date`(`start_date` ASC) USING BTREE,
  INDEX `fk_fosters_shelter`(`sid` ASC) USING BTREE,
  CONSTRAINT `fk_fosters_pet` FOREIGN KEY (`pid`) REFERENCES `pets` (`pid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_fosters_shelter` FOREIGN KEY (`sid`) REFERENCES `shelters` (`sid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_fosters_user` FOREIGN KEY (`uid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fosters
-- ----------------------------

-- ----------------------------
-- Table structure for login_history
-- ----------------------------
DROP TABLE IF EXISTS `login_history`;
CREATE TABLE `login_history`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '用户ID',
  `login_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUCCESS',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_login_history_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_login_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of login_history
-- ----------------------------

-- ----------------------------
-- Table structure for media_files
-- ----------------------------
DROP TABLE IF EXISTS `media_files`;
CREATE TABLE `media_files`  (
  `mid` int NOT NULL AUTO_INCREMENT COMMENT '媒体ID',
  `record_id` int NOT NULL COMMENT '关联记录ID',
  `uid` int NOT NULL COMMENT '上传用户ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访问路径',
  `media_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型(image/video)',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`mid`) USING BTREE,
  INDEX `idx_media_record`(`record_id` ASC) USING BTREE,
  INDEX `idx_media_user`(`uid` ASC) USING BTREE,
  CONSTRAINT `fk_media_record` FOREIGN KEY (`record_id`) REFERENCES `pet_records` (`record_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_media_user` FOREIGN KEY (`uid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of media_files
-- ----------------------------

-- ----------------------------
-- Table structure for pet_health
-- ----------------------------
DROP TABLE IF EXISTS `pet_health`;
CREATE TABLE `pet_health`  (
  `health_id` int NOT NULL AUTO_INCREMENT COMMENT '健康记录ID',
  `pid` int NOT NULL COMMENT '宠物ID',
  `check_date` datetime NOT NULL COMMENT '检查时间',
  `health_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '健康类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '描述',
  `reminder_time` datetime NULL DEFAULT NULL COMMENT '提醒时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`health_id`) USING BTREE,
  INDEX `idx_pet_health_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_pet_health_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_pet_health_pet` FOREIGN KEY (`pid`) REFERENCES `pets` (`pid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_health
-- ----------------------------

-- ----------------------------
-- Table structure for pet_records
-- ----------------------------
DROP TABLE IF EXISTS `pet_records`;
CREATE TABLE `pet_records`  (
  `record_id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `pid` int NOT NULL COMMENT '宠物ID',
  `uid` int NOT NULL COMMENT '用户ID',
  `event_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
  `mood` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '心情',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发生地点',
  `record_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_pet_records_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_pet_records_uid`(`uid` ASC) USING BTREE,
  CONSTRAINT `fk_pet_records_pet` FOREIGN KEY (`pid`) REFERENCES `pets` (`pid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pet_records_user` FOREIGN KEY (`uid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_records
-- ----------------------------

-- ----------------------------
-- Table structure for pets
-- ----------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets`  (
  `pid` int NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `species_id` int NOT NULL COMMENT '物种ID',
  `breed_id` int NOT NULL COMMENT '品种ID',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `img_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL（OSS外链）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/ADOPTED/FOSTERING',
  `shelter_id` int NULL DEFAULT NULL COMMENT '所在收容所',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`pid`) USING BTREE,
  INDEX `idx_pets_species`(`species_id` ASC) USING BTREE,
  INDEX `idx_pets_breed`(`breed_id` ASC) USING BTREE,
  INDEX `idx_pets_shelter`(`shelter_id` ASC) USING BTREE,
  INDEX `idx_pets_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_pets_species` FOREIGN KEY (`species_id`) REFERENCES `species` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pets_breed` FOREIGN KEY (`breed_id`) REFERENCES `breed` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pets_shelter` FOREIGN KEY (`shelter_id`) REFERENCES `shelters` (`sid`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pets
-- ----------------------------
INSERT INTO `pets` VALUES (1, '巴迪', 1, 1, 2, '雄', 'https://images.unsplash.com/photo-1507149833265-60c372daea22?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (2, '小橘', 2, 10, 1, '雌', 'https://images.unsplash.com/photo-1511044568932-338cba0ad803?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (3, '可可', 1, 2, 3, '雌', 'https://images.unsplash.com/photo-1557979619-445218f32645?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (4, '雪球', 2, 9, 2, '雄', 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (5, '奶茶', 2, 10, 1, '雌', 'https://images.unsplash.com/photo-1472491235688-bdc81a63246e?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (6, '黑豆', 2, 9, 2, '雄', 'https://images.unsplash.com/photo-1516384981290-8383f43c1ee1?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (7, '豆包', 1, 2, 2, '雄', 'https://images.unsplash.com/photo-1507146426996-ef05306b995a?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (8, '球球', 1, 1, 4, '雌', 'https://images.unsplash.com/photo-1504595403659-9088ce801e29?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (9, '花生', 1, 2, 5, '雄', 'https://images.unsplash.com/photo-1518717758536-85ae29035b6d?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (10, '奶酪', 2, 10, 3, '雄', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (11, '布丁', 2, 12, 2, '雌', 'https://images.unsplash.com/photo-1494256997604-768d1f608cac?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (12, '萌萌', 1, 3, 1, '雌', 'https://images.unsplash.com/photo-1505628346881-b72b27e84530?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (13, '旺财', 1, 5, 3, '雄', 'https://images.unsplash.com/photo-1517840933442-d2cbff63b0ec?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (14, '豆花', 2, 11, 2, '雌', 'https://images.unsplash.com/photo-1491485880348-85d48a9e531d?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (15, '团子', 1, 6, 2, '雄', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (16, '雪球2', 2, 9, 1, '雌', 'https://images.unsplash.com/photo-1508672019048-805c876b67e2?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (17, '可乐', 1, 4, 4, '雌', 'https://images.unsplash.com/photo-1525253086316-d0c936c814f8?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (18, '胖虎', 2, 14, 3, '雄', 'https://images.unsplash.com/photo-1511203390095-9f7b85b97914?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (19, '小米', 1, 7, 1, '雌', 'https://images.unsplash.com/photo-1508948956644-00107b6a0252?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (20, '布鲁', 1, 8, 2, '雄', 'https://images.unsplash.com/photo-1528892952291-009c663ce843?auto=format&fit=crop&w=1600&q=80', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (21, '饼干', 1, 3, 1, '雄', 'https://upload.wikimedia.org/wikipedia/commons/9/99/Welsh_Pembroke_Corgi.jpg', 'AVAILABLE', 3, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (22, '麦麦', 1, 4, 2, '雌', 'https://upload.wikimedia.org/wikipedia/commons/3/34/Labrador_on_Quantock_%282175262184%29.jpg', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (23, '北北', 1, 5, 3, '雄', 'https://upload.wikimedia.org/wikipedia/commons/8/8b/Husky_L.jpg', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (24, '团团', 1, 2, 2, '雌', 'https://upload.wikimedia.org/wikipedia/commons/6/6b/Taka_Shiba.jpg', 'AVAILABLE', 3, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (25, '阿金', 1, 1, 4, '雄', 'https://upload.wikimedia.org/wikipedia/commons/b/bd/Golden_Retriever_Dukedestiny01_drvd.jpg', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (26, '云朵', 2, 13, 1, '雌', 'https://upload.wikimedia.org/wikipedia/commons/2/2d/Mystica_from_British_Empire_Cattery.jpg', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (27, '可颂', 2, 15, 2, '雄', 'https://upload.wikimedia.org/wikipedia/commons/2/2a/Jewelkatz_Romeo_Of_Stalker-Bars.jpg', 'AVAILABLE', 3, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (28, '奶昔', 2, 9, 2, '雌', 'https://upload.wikimedia.org/wikipedia/commons/6/64/Ragdoll_from_Gatil_Ragbelas.jpg', 'AVAILABLE', 1, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (29, '芝麻', 2, 16, 3, '雄', 'https://upload.wikimedia.org/wikipedia/commons/2/25/Siam_lilacpoint.jpg', 'AVAILABLE', 3, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `pets` VALUES (30, '奶盖', 2, 17, 4, '雌', 'https://upload.wikimedia.org/wikipedia/commons/8/81/Persialainen.jpg', 'AVAILABLE', 2, '2025-12-16 15:30:13', '2025-12-16 15:30:13');

-- ----------------------------
-- Table structure for shelter_admins
-- ----------------------------
DROP TABLE IF EXISTS `shelter_admins`;
CREATE TABLE `shelter_admins`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `shelter_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shelter_admins_shelter`(`shelter_id` ASC) USING BTREE,
  INDEX `idx_shelter_admins_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_shelter_admins_shelter` FOREIGN KEY (`shelter_id`) REFERENCES `shelters` (`sid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_shelter_admins_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shelter_admins
-- ----------------------------
INSERT INTO `shelter_admins` VALUES (1, 1, 4);
INSERT INTO `shelter_admins` VALUES (2, 2, 5);
INSERT INTO `shelter_admins` VALUES (3, 3, 6);

-- ----------------------------
-- Table structure for shelters
-- ----------------------------
DROP TABLE IF EXISTS `shelters`;
CREATE TABLE `shelters`  (
  `sid` int NOT NULL AUTO_INCREMENT COMMENT '收容所ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收容所名称',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `capacity` int NULL DEFAULT NULL COMMENT '可容纳数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shelters
-- ----------------------------
INSERT INTO `shelters` VALUES (1, '城市宠物之家', '北京 朝阳区', 120, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `shelters` VALUES (2, '幸福毛孩中心', '上海 浦东新区', 90, '2025-12-16 15:30:13', '2025-12-16 15:30:13');
INSERT INTO `shelters` VALUES (3, '海湾流浪动物救助站', '广州 天河区', 110, '2025-12-16 15:30:13', '2025-12-16 15:30:13');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `introduce` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '自我介绍',
  `headpic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `admin_shelter_id` int NULL DEFAULT NULL COMMENT '收容所管理端',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users_username`(`username` ASC) USING BTREE,
  INDEX `idx_users_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_users_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '13800000001', '$2a$12$ZWgCRiHCvPmhfrK9NLC2quv/xs.ulsLUNHBBZSSh6lzXMv9p02A5u', '系统管理员', NULL, 'admin@example.com', 'ADMIN', '2025-12-16 15:30:13', '2025-12-16 16:00:12', NULL);
INSERT INTO `users` VALUES (2, '123', '13800000002', '$2a$12$ZWgCRiHCvPmhfrK9NLC2quv/xs.ulsLUNHBBZSSh6lzXMv9p02A5u', '热爱毛孩子', NULL, 'user@example.com', 'USER', '2025-12-16 15:30:13', '2025-12-16 15:30:13', NULL);
INSERT INTO `users` VALUES (3, '111', '15159666666', '$2a$12$ZWgCRiHCvPmhfrK9NLC2quv/xs.ulsLUNHBBZSSh6lzXMv9p02A5u', NULL, NULL, '333@qq.com', 'USER', '2025-12-16 15:59:45', '2025-12-16 15:59:45', NULL);
INSERT INTO `users` VALUES (4, 'admin1', '15158888888', '$2a$12$ZWgCRiHCvPmhfrK9NLC2quv/xs.ulsLUNHBBZSSh6lzXMv9p02A5u', '城市宠物之家管理员', NULL, 'admin-bj@example.com', 'ADMIN', '2025-12-20 21:40:02', '2025-12-20 21:44:41', 1);
INSERT INTO `users` VALUES (5, 'admin2', '15159990001', '$2a$12$ZWgCRiHCvPmhfrK9NLC2quv/xs.ulsLUNHBBZSSh6lzXMv9p02A5u', '幸福毛孩中心管理员', NULL, 'admin-sh@example.com', 'ADMIN', '2025-12-20 21:40:02', '2025-12-20 21:44:41', 2);
INSERT INTO `users` VALUES (6, 'admin3', '15159990002', '$2a$12$ZWgCRiHCvPmhfrK9NLC2quv/xs.ulsLUNHBBZSSh6lzXMv9p02A5u', '海湾流浪动物救助站管理员', NULL, 'admin-gz@example.com', 'ADMIN', '2025-12-20 21:40:02', '2025-12-20 21:44:41', 3);

SET FOREIGN_KEY_CHECKS = 1;
