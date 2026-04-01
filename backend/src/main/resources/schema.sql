-- =============================================
-- 宠物领养系统数据库建表脚本
-- 数据库: pet_adoption
-- =============================================

-- 设置字符编码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS pet_adoption DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_adoption;

-- 设置连接字符集
SET NAMES utf8mb4;

-- =============================================
-- 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role_type` TINYINT NOT NULL DEFAULT 3 COMMENT '角色类型: 1管理员 2救助方 3领养人',
  `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `occupation` VARCHAR(50) DEFAULT NULL COMMENT '职业',
  `pet_experience` TEXT DEFAULT NULL COMMENT '养宠经验',
  `living_environment` TEXT DEFAULT NULL COMMENT '居住环境描述',
  `org_name` VARCHAR(100) DEFAULT NULL COMMENT '机构名称(救助方)',
  `org_license` VARCHAR(255) DEFAULT NULL COMMENT '机构资质证明(救助方)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常 2黑名单',
  `verify_status` TINYINT NOT NULL DEFAULT 0 COMMENT '认证状态: 0未认证 1已认证',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 宠物表
-- =============================================
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `species` VARCHAR(20) NOT NULL COMMENT '物种: 猫/狗/其他',
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种',
  `age_months` INT DEFAULT NULL COMMENT '年龄(月)',
  `gender` TINYINT DEFAULT NULL COMMENT '性别: 1公 2母',
  `health_status` VARCHAR(255) DEFAULT NULL COMMENT '健康状况',
  `personality_tags` TEXT DEFAULT NULL COMMENT '性格标签(JSON数组)',
  `photos` TEXT DEFAULT NULL COMMENT '照片(JSON数组)',
  `videos` TEXT DEFAULT NULL COMMENT '视频(JSON数组)',
  `adoption_requirements` TEXT DEFAULT NULL COMMENT '领养要求描述',
  `allow_single_living` TINYINT DEFAULT 1 COMMENT '是否允许独居: 0否 1是',
  `require_experience` TINYINT DEFAULT 0 COMMENT '是否要求有养宠经验: 0否 1是',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '所在地区',
  `adopter_id` BIGINT DEFAULT NULL COMMENT '领养人ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1待领养 2审核中 3已领养 4暂不领养',
  `remark` TEXT DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_species` (`species`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物表';

-- =============================================
-- 领养申请表
-- =============================================
DROP TABLE IF EXISTS `adoption_application`;
CREATE TABLE `adoption_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `self_introduction` TEXT DEFAULT NULL COMMENT '自我介绍',
  `residence_proof` VARCHAR(255) DEFAULT NULL COMMENT '居住证明(文件路径)',
  `income_proof` VARCHAR(255) DEFAULT NULL COMMENT '收入证明(文件路径)',
  `rescue_review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '救助方审核状态: 0待审核 1通过 2拒绝',
  `rescue_review_comment` TEXT DEFAULT NULL COMMENT '救助方审核意见',
  `rescue_review_time` DATETIME DEFAULT NULL COMMENT '救助方审核时间',
  `admin_review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '管理员复核状态: 0待审核 1通过 2拒绝',
  `admin_review_comment` TEXT DEFAULT NULL COMMENT '管理员复核意见',
  `admin_review_time` DATETIME DEFAULT NULL COMMENT '管理员复核时间',
  `home_visit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '家访状态: 0未安排 1已安排 2已完成 3不通过',
  `home_visit_comment` TEXT DEFAULT NULL COMMENT '家访备注',
  `home_visit_time` DATETIME DEFAULT NULL COMMENT '家访时间',
  `final_status` TINYINT NOT NULL DEFAULT 0 COMMENT '最终状态: 0进行中 1成功 2失败',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_final_status` (`final_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养申请表';

-- =============================================
-- 跟进记录表
-- =============================================
DROP TABLE IF EXISTS `follow_up_record`;
CREATE TABLE `follow_up_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `adopter_id` BIGINT NOT NULL COMMENT '领养人ID',
  `application_id` BIGINT DEFAULT NULL COMMENT '领养申请ID',
  `content` TEXT DEFAULT NULL COMMENT '跟进内容',
  `photos` TEXT DEFAULT NULL COMMENT '照片(JSON数组)',
  `videos` TEXT DEFAULT NULL COMMENT '视频(JSON数组)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1正常 2异常',
  `admin_comment` TEXT DEFAULT NULL COMMENT '管理员评论',
  `due_date` DATETIME DEFAULT NULL COMMENT '应提交日期',
  `submit_time` DATETIME DEFAULT NULL COMMENT '实际提交时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_adopter_id` (`adopter_id`),
  KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跟进记录表';

-- =============================================
-- 黑名单表
-- =============================================
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '类型: 1领养人 2救助方',
  `reason` TEXT DEFAULT NULL COMMENT '拉黑原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- =============================================
-- 消息表
-- =============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '消息标题',
  `content` TEXT DEFAULT NULL COMMENT '消息内容',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1系统通知 2审核结果 3跟进提醒',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 知识库表
-- =============================================
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` LONGTEXT DEFAULT NULL COMMENT '内容',
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT '分类: 1养宠常识 2领养须知 3疾病护理',
  `author_id` BIGINT DEFAULT NULL COMMENT '作者ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0草稿 1发布',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- =============================================
-- 操作日志表
-- =============================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `action` VARCHAR(100) DEFAULT NULL COMMENT '操作动作',
  `params` TEXT DEFAULT NULL COMMENT '请求参数',
  `result` VARCHAR(50) DEFAULT NULL COMMENT '执行结果',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 初始化管理员账号
-- 用户名: admin  密码: 123456 (MD5加密)
-- =============================================
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role_type`, `status`, `verify_status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000000', 1, 1, 1);

-- =============================================
-- 初始化救助方账号
-- 用户名: rescuer  密码: 123456 (MD5加密)
-- =============================================
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role_type`, `status`, `verify_status`, `org_name`) VALUES
('rescuer', 'e10adc3949ba59abbe56e057f20f883e', '爱心救助站', '13800000001', 2, 1, 1, '爱心宠物救助站');

-- =============================================
-- 初始化领养人账号
-- 用户名: adopter  密码: 123456 (MD5加密)
-- =============================================
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role_type`, `status`, `verify_status`, `address`, `occupation`) VALUES
('adopter', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800000002', 3, 1, 1, '北京市朝阳区', '程序员');

-- =============================================
-- 初始化宠物数据
-- =============================================
INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`) VALUES
(2, '小橘', '猫', '中华田园猫', 8, 1, '已绝育、已驱虫、已打疫苗', '["粘人","活泼","亲人"]', '["https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=300&fit=crop"]', '北京市朝阳区', 1, '有固定住所，能接受定期回访', 1, 0),
(2, '豆豆', '狗', '金毛犬', 24, 2, '已绝育、健康状况良好', '["活泼","亲人"]', '["https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=300&fit=crop"]', '北京市海淀区', 1, '有养狗经验优先，需要有足够的活动空间', 0, 1),
(2, '咪咪', '猫', '英国短毛猫', 12, 2, '已驱虫、已打疫苗', '["独立","安静"]', '["https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=400&h=300&fit=crop"]', '上海市浦东新区', 1, '适合安静的家庭环境', 1, 0),
(2, '旺财', '狗', '柯基犬', 18, 1, '已绝育、已打疫苗', '["活泼","粘人"]', '["https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&h=300&fit=crop"]', '广州市天河区', 1, '需要每天遛狗，有耐心的主人', 0, 0),
(2, '花花', '猫', '布偶猫', 6, 2, '健康状况良好', '["粘人","亲人","安静"]', '["https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=400&h=300&fit=crop"]', '深圳市南山区', 1, '需要室内饲养，定期梳毛', 1, 0),
(2, '大黄', '狗', '拉布拉多', 36, 1, '已绝育、已驱虫', '["活泼","亲人"]', '["https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=400&h=300&fit=crop"]', '杭州市西湖区', 1, '有养大型犬经验，有独立院子优先', 0, 1),
(2, '小白', '猫', '美国短毛猫', 10, 1, '已驱虫、已打疫苗', '["独立","胆小"]', '["https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=400&h=300&fit=crop"]', '成都市武侯区', 1, '需要耐心，不适合有小孩的家庭', 1, 0),
(2, '乐乐', '狗', '泰迪', 14, 2, '已绝育、健康', '["粘人","活泼"]', '["https://images.unsplash.com/photo-1596492784531-6e6eb5ea9993?w=400&h=300&fit=crop"]', '南京市鼓楼区', 1, '适合公寓饲养，需要定期美容', 1, 0);

-- =============================================
-- 初始化知识库文章
-- =============================================
INSERT INTO `knowledge` (`title`, `content`, `category`, `author_id`, `status`, `view_count`) VALUES
('新手养猫必读指南', '<p>养猫是一件需要耐心和责任心的事情。以下是一些新手养猫需要注意的事项：</p><h3>1. 准备必需品</h3><p>猫粮、猫砂盆、猫砂、食盆、水盆、猫抓板、猫窝等。</p><h3>2. 定期体检</h3><p>每年至少进行一次全面体检，及时接种疫苗。</p><h3>3. 科学喂养</h3><p>选择优质猫粮，不要喂食人类食物，特别是洋葱、巧克力等有毒食物。</p>', 1, 1, 1, 128),
('领养前必须考虑的问题', '<p>领养宠物是一个重要的决定，请在领养前认真考虑以下问题：</p><h3>1. 时间精力</h3><p>您是否有足够的时间陪伴和照顾宠物？</p><h3>2. 经济能力</h3><p>宠物的食物、医疗、用品等都需要持续投入。</p><h3>3. 居住条件</h3><p>您的居住环境是否适合养宠？房东是否允许？</p><h3>4. 家人意见</h3><p>所有家庭成员是否都同意养宠？是否有人过敏？</p>', 2, 1, 1, 256),
('常见宠物疾病及预防', '<p>了解常见疾病，做好预防工作，让您的宠物更健康。</p><h3>1. 感冒</h3><p>症状：打喷嚏、流鼻涕。预防：保持环境温暖干燥。</p><h3>2. 肠胃炎</h3><p>症状：呕吐、腹泻。预防：规律饮食，避免喂食变质食物。</p><h3>3. 皮肤病</h3><p>症状：瘙痒、脱毛。预防：定期驱虫，保持清洁。</p>', 3, 1, 1, 89);
