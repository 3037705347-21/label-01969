-- =============================================
-- 初始化测试数据（补充插入）
-- 如果数据库已存在，执行此脚本添加测试数据
-- =============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE pet_adoption;

-- 插入救助方账号（如果不存在）
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `phone`, `role_type`, `status`, `verify_status`, `org_name`) VALUES
('rescuer', 'e10adc3949ba59abbe56e057f20f883e', '爱心救助站', '13800000001', 2, 1, 1, '爱心宠物救助站');

-- 插入领养人账号（如果不存在）
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `phone`, `role_type`, `status`, `verify_status`, `address`, `occupation`) VALUES
('adopter', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800000002', 3, 1, 1, '北京市朝阳区', '程序员');

-- 获取救助方用户ID并插入宠物数据
SET @rescuer_id = (SELECT id FROM `user` WHERE username = 'rescuer' LIMIT 1);

-- 插入宠物数据（如果不存在）
INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '小橘', '猫', '中华田园猫', 8, 1, '已绝育、已驱虫、已打疫苗', '["粘人","活泼","亲人"]', '["https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=300&fit=crop"]', '北京市朝阳区', 1, '有固定住所，能接受定期回访', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '小橘');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '豆豆', '狗', '金毛犬', 24, 2, '已绝育、健康状况良好', '["活泼","亲人"]', '["https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=300&fit=crop"]', '北京市海淀区', 1, '有养狗经验优先，需要有足够的活动空间', 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '豆豆');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '咪咪', '猫', '英国短毛猫', 12, 2, '已驱虫、已打疫苗', '["独立","安静"]', '["https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=400&h=300&fit=crop"]', '上海市浦东新区', 1, '适合安静的家庭环境', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '咪咪');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '旺财', '狗', '柯基犬', 18, 1, '已绝育、已打疫苗', '["活泼","粘人"]', '["https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&h=300&fit=crop"]', '广州市天河区', 1, '需要每天遛狗，有耐心的主人', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '旺财');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '花花', '猫', '布偶猫', 6, 2, '健康状况良好', '["粘人","亲人","安静"]', '["https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=400&h=300&fit=crop"]', '深圳市南山区', 1, '需要室内饲养，定期梳毛', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '花花');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '大黄', '狗', '拉布拉多', 36, 1, '已绝育、已驱虫', '["活泼","亲人"]', '["https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=400&h=300&fit=crop"]', '杭州市西湖区', 1, '有养大型犬经验，有独立院子优先', 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '大黄');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '小白', '猫', '美国短毛猫', 10, 1, '已驱虫、已打疫苗', '["独立","胆小"]', '["https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=400&h=300&fit=crop"]', '成都市武侯区', 1, '需要耐心，不适合有小孩的家庭', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '小白');

INSERT INTO `pet` (`publisher_id`, `name`, `species`, `breed`, `age_months`, `gender`, `health_status`, `personality_tags`, `photos`, `location`, `status`, `adoption_requirements`, `allow_single_living`, `require_experience`)
SELECT @rescuer_id, '乐乐', '狗', '泰迪', 14, 2, '已绝育、健康', '["粘人","活泼"]', '["https://images.unsplash.com/photo-1596492784531-6e6eb5ea9993?w=400&h=300&fit=crop"]', '南京市鼓楼区', 1, '适合公寓饲养，需要定期美容', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `pet` WHERE name = '乐乐');

SELECT '数据初始化完成！' AS message;
