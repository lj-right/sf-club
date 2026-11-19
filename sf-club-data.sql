/*
 Navicat Premium Data Transfer

 Source Server         : sf-club
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Schema         : sf-club

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 2026-11-16
 Description: sf-club 基础初始化数据与测试数据 (包含完整 Sa-Token 权限与用户关联配置)
 Total Tables: 25 张表
*/

USE `sf-club`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ====================================================================
-- 1. 认证与权限体系 (Sa-Token 权限配置 & 用户角色权限多对多关联)
-- ====================================================================

-- ----------------------------
-- Records of auth_role (角色表)
-- 包含 AuthConstant.java 定义的三大核心角色: super_user, normal_user, vip_user
-- ----------------------------
TRUNCATE TABLE `auth_role`;
INSERT INTO `auth_role` (`id`, `role_name`, `role_key`, `created_by`, `created_time`, `updated_by`, `updated_time`, `is_deleted`) VALUES
(1, '超级管理员', 'super_user', 'system', NOW(), 'system', NOW(), 0),
(2, '普通用户', 'normal_user', 'system', NOW(), 'system', NOW(), 0),
(3, 'VIP用户', 'vip_user', 'system', NOW(), 'system', NOW(), 0);

-- ----------------------------
-- Records of auth_permission (权限表)
-- 严格包含网关 SaTokenConfigure 配置的 'subject:add' 及各微服务业务权限
-- ----------------------------
TRUNCATE TABLE `auth_permission`;
INSERT INTO `auth_permission` (`id`, `name`, `parent_id`, `type`, `menu_url`, `status`, `show`, `icon`, `permission_key`, `created_by`, `created_time`, `updated_by`, `updated_time`, `is_deleted`) VALUES
-- 题目管理权限 (网关 StpUtil.checkPermission("subject:add") 对应点)
(1, '添加题目', 0, 2, '/subject/subject/add', 0, 0, 'el-icon-plus', 'subject:add', 'system', NOW(), 'system', NOW(), 0),
(2, '题目查询', 0, 2, '/subject/getSubjectPage', 0, 0, 'el-icon-search', 'subject:query', 'system', NOW(), 'system', NOW(), 0),
(3, '题目详情', 0, 2, '/subject/querySubjectInfo', 0, 0, 'el-icon-view', 'subject:detail', 'system', NOW(), 'system', NOW(), 0),
(4, '题目修改', 0, 2, '/subject/update', 0, 0, 'el-icon-edit', 'subject:update', 'system', NOW(), 'system', NOW(), 0),
(5, '题目删除', 0, 2, '/subject/delete', 0, 0, 'el-icon-delete', 'subject:delete', 'system', NOW(), 'system', NOW(), 0),
-- 分类与标签权限
(6, '分类管理', 0, 2, '/subject/category/**', 0, 0, 'el-icon-menu', 'subject:category', 'system', NOW(), 'system', NOW(), 0),
(7, '标签管理', 0, 2, '/subject/label/**', 0, 0, 'el-icon-collection-tag', 'subject:label', 'system', NOW(), 'system', NOW(), 0),
-- 刷题与练题权限
(8, '套题练习', 0, 2, '/practice/set/**', 0, 0, 'el-icon-reading', 'practice:set', 'system', NOW(), 'system', NOW(), 0),
(9, '答题提交', 0, 2, '/practice/detail/submit', 0, 0, 'el-icon-check', 'practice:submit', 'system', NOW(), 'system', NOW(), 0),
(10, '刷题报告', 0, 2, '/practice/detail/getReport', 0, 0, 'el-icon-data-analysis', 'practice:report', 'system', NOW(), 'system', NOW(), 0),
-- AI 模拟面试权限
(11, '模拟面试', 0, 2, '/interview/**', 0, 0, 'el-icon-microphone', 'interview:access', 'system', NOW(), 'system', NOW(), 0),
-- 圈子社交权限
(12, '圈子查看', 0, 2, '/circle/**', 0, 0, 'el-icon-chat-dot-round', 'circle:view', 'system', NOW(), 'system', NOW(), 0),
(13, '动态发布', 0, 2, '/circle/share/moment/save', 0, 0, 'el-icon-edit-outline', 'circle:moment:add', 'system', NOW(), 'system', NOW(), 0),
-- 系统管理权限
(14, '用户管理', 0, 2, '/user/**', 0, 0, 'el-icon-user', 'auth:user', 'system', NOW(), 'system', NOW(), 0),
(15, '权限分配', 0, 2, '/rolePermission/**', 0, 0, 'el-icon-lock', 'auth:permission', 'system', NOW(), 'system', NOW(), 0);

-- ----------------------------
-- Records of auth_role_permission (角色权限关联表)
-- 角色1(超级管理员): 拥有全部权限 1~15
-- 角色2(普通用户): 拥有基础浏览、刷题、面试、发帖权限，以及 subject:add 允许出题贡献
-- 角色3(VIP用户): 拥有高级面试、套题训练、出题等多项特权
-- ----------------------------
TRUNCATE TABLE `auth_role_permission`;
INSERT INTO `auth_role_permission` (`id`, `role_id`, `permission_id`, `created_by`, `created_time`, `updated_by`, `updated_time`, `is_deleted`) VALUES
-- 1. 超级管理员拥有全部权限 1 ~ 15
(1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2, 1, 2, 'system', NOW(), 'system', NOW(), 0),
(3, 1, 3, 'system', NOW(), 'system', NOW(), 0),
(4, 1, 4, 'system', NOW(), 'system', NOW(), 0),
(5, 1, 5, 'system', NOW(), 'system', NOW(), 0),
(6, 1, 6, 'system', NOW(), 'system', NOW(), 0),
(7, 1, 7, 'system', NOW(), 'system', NOW(), 0),
(8, 1, 8, 'system', NOW(), 'system', NOW(), 0),
(9, 1, 9, 'system', NOW(), 'system', NOW(), 0),
(10, 1, 10, 'system', NOW(), 'system', NOW(), 0),
(11, 1, 11, 'system', NOW(), 'system', NOW(), 0),
(12, 1, 12, 'system', NOW(), 'system', NOW(), 0),
(13, 1, 13, 'system', NOW(), 'system', NOW(), 0),
(14, 1, 14, 'system', NOW(), 'system', NOW(), 0),
(15, 1, 15, 'system', NOW(), 'system', NOW(), 0),
-- 2. 普通用户角色权限: 题目浏览(2,3)、添加题目(1)、练题(8,9,10)、面试(11)、圈子(12,13)
(16, 2, 1, 'system', NOW(), 'system', NOW(), 0),
(17, 2, 2, 'system', NOW(), 'system', NOW(), 0),
(18, 2, 3, 'system', NOW(), 'system', NOW(), 0),
(19, 2, 8, 'system', NOW(), 'system', NOW(), 0),
(20, 2, 9, 'system', NOW(), 'system', NOW(), 0),
(21, 2, 10, 'system', NOW(), 'system', NOW(), 0),
(22, 2, 11, 'system', NOW(), 'system', NOW(), 0),
(23, 2, 12, 'system', NOW(), 'system', NOW(), 0),
(24, 2, 13, 'system', NOW(), 'system', NOW(), 0),
-- 3. VIP 用户角色权限: 包含普通用户所有权限，外加分类与标签查看
(25, 3, 1, 'system', NOW(), 'system', NOW(), 0),
(26, 3, 2, 'system', NOW(), 'system', NOW(), 0),
(27, 3, 3, 'system', NOW(), 'system', NOW(), 0),
(28, 3, 6, 'system', NOW(), 'system', NOW(), 0),
(29, 3, 7, 'system', NOW(), 'system', NOW(), 0),
(30, 3, 8, 'system', NOW(), 'system', NOW(), 0),
(31, 3, 9, 'system', NOW(), 'system', NOW(), 0),
(32, 3, 10, 'system', NOW(), 'system', NOW(), 0),
(33, 3, 11, 'system', NOW(), 'system', NOW(), 0),
(34, 3, 12, 'system', NOW(), 'system', NOW(), 0),
(35, 3, 13, 'system', NOW(), 'system', NOW(), 0);

-- ----------------------------
-- Records of auth_user (用户信息表)
-- 密码经 SaSecureUtil.md5BySalt("123456", "suifeng") 散列，对应明文均为 123456
-- 散列结果: 5df863503a34b823c34a9d462fdf0a8b
-- ----------------------------
TRUNCATE TABLE `auth_user`;
INSERT INTO `auth_user` (`id`, `user_name`, `nick_name`, `email`, `phone`, `password`, `sex`, `avatar`, `status`, `introduce`, `ext_json`, `created_by`, `created_time`, `updated_by`, `updated_time`, `is_deleted`) VALUES
(1, 'admin', '超级管理员', 'admin@sfclub.com', '13800000001', '5df863503a34b823c34a9d462fdf0a8b', 1, 'https://oss.sfclub.com/avatar/admin.png', 0, '系统最高权限超级管理员', '{"role":"admin","level":"top"}', 'system', NOW(), 'system', NOW(), 0),
(2, 'suifeng', '随风', 'suifeng@sfclub.com', '13800000002', '5df863503a34b823c34a9d462fdf0a8b', 1, 'https://oss.sfclub.com/avatar/suifeng.png', 0, '全栈架构师，sf-club 核心创作者', '{"github":"https://github.com"}', 'system', NOW(), 'system', NOW(), 0),
(3, 'test_user', '测试小伙伴', 'test@sfclub.com', '13800000003', '5df863503a34b823c34a9d462fdf0a8b', 0, 'https://oss.sfclub.com/avatar/test.png', 0, '热爱算法与后端开发的求职者', '{"grade":"junior"}', 'system', NOW(), 'system', NOW(), 0),
(4, 'vip_coder', '黄金VIP学员', 'vip@sfclub.com', '13800000004', '5df863503a34b823c34a9d462fdf0a8b', 1, 'https://oss.sfclub.com/avatar/vip.png', 0, '专注高频大厂面试突击', '{"memberLevel":"VIP2"}', 'system', NOW(), 'system', NOW(), 0);

-- ----------------------------
-- Records of auth_user_role (用户角色关联表)
-- admin -> super_user (1)
-- suifeng -> super_user (1) + vip_user (3)
-- test_user -> normal_user (2)
-- vip_coder -> vip_user (3) + normal_user (2)
-- ----------------------------
TRUNCATE TABLE `auth_user_role`;
INSERT INTO `auth_user_role` (`id`, `user_id`, `role_id`, `created_by`, `created_time`, `updated_by`, `updated_time`, `is_deleted`) VALUES
(1, 1, 1, 'system', NOW(), 'system', NOW(), 0),
(2, 2, 1, 'system', NOW(), 'system', NOW(), 0),
(3, 2, 3, 'system', NOW(), 'system', NOW(), 0),
(4, 3, 2, 'system', NOW(), 'system', NOW(), 0),
(5, 4, 2, 'system', NOW(), 'system', NOW(), 0),
(6, 4, 3, 'system', NOW(), 'system', NOW(), 0);


-- ====================================================================
-- 2. 题目与分类标签体系 (微服务 sf-club-subject)
-- ====================================================================

-- ----------------------------
-- Records of subject_category (题目分类表)
-- category_type: 1-岗位大类, 2-二级分类
-- ----------------------------
TRUNCATE TABLE `subject_category`;
INSERT INTO `subject_category` (`id`, `category_name`, `category_type`, `image_url`, `parent_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, '后端开发', 1, 'https://oss.sfclub.com/category/backend.png', 0, 'admin', NOW(), 'admin', NOW(), 0),
(2, '前端开发', 1, 'https://oss.sfclub.com/category/frontend.png', 0, 'admin', NOW(), 'admin', NOW(), 0),
(3, '大数据与数仓', 1, 'https://oss.sfclub.com/category/bigdata.png', 0, 'admin', NOW(), 'admin', NOW(), 0),
(4, '算法与数据结构', 1, 'https://oss.sfclub.com/category/algorithm.png', 0, 'admin', NOW(), 'admin', NOW(), 0),
-- 二级分类 (关联 parent_id = 1 后端开发)
(101, 'Java基础', 2, 'https://oss.sfclub.com/category/java.png', 1, 'admin', NOW(), 'admin', NOW(), 0),
(102, 'JVM原理', 2, 'https://oss.sfclub.com/category/jvm.png', 1, 'admin', NOW(), 'admin', NOW(), 0),
(103, 'Spring生态', 2, 'https://oss.sfclub.com/category/spring.png', 1, 'admin', NOW(), 'admin', NOW(), 0),
(104, 'MySQL数据库', 2, 'https://oss.sfclub.com/category/mysql.png', 1, 'admin', NOW(), 'admin', NOW(), 0),
(105, 'Redis缓存', 2, 'https://oss.sfclub.com/category/redis.png', 1, 'admin', NOW(), 'admin', NOW(), 0),
(106, '多线程与并发', 2, 'https://oss.sfclub.com/category/concurrent.png', 1, 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of subject_label (题目标签表)
-- 关联 category_id
-- ----------------------------
TRUNCATE TABLE `subject_label`;
INSERT INTO `subject_label` (`id`, `label_name`, `category_id`, `sort_num`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, '集合框架', 101, 1, 'admin', NOW(), 'admin', NOW(), 0),
(2, '反射与动态代理', 101, 2, 'admin', NOW(), 'admin', NOW(), 0),
(3, '垃圾回收GC', 102, 1, 'admin', NOW(), 'admin', NOW(), 0),
(4, '内存模型JMM', 102, 2, 'admin', NOW(), 'admin', NOW(), 0),
(5, 'IOC与AOP', 103, 1, 'admin', NOW(), 'admin', NOW(), 0),
(6, 'Spring Boot自动装配', 103, 2, 'admin', NOW(), 'admin', NOW(), 0),
(7, '索引优化', 104, 1, 'admin', NOW(), 'admin', NOW(), 0),
(8, '事务与MVCC', 104, 2, 'admin', NOW(), 'admin', NOW(), 0),
(9, '缓存穿透与击穿', 105, 1, 'admin', NOW(), 'admin', NOW(), 0),
(10, 'AQS与Lock', 106, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of subject_info (题目信息表)
-- subject_type: 1-单选, 2-多选, 3-判断, 4-简答
-- subject_difficult: 1-初级, 2-中级, 3-高级
-- ----------------------------
TRUNCATE TABLE `subject_info`;
INSERT INTO `subject_info` (`id`, `subject_name`, `subject_difficult`, `settle_name`, `subject_type`, `subject_score`, `subject_parse`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, '在Java中，HashMap在JDK 1.8中的底层数据结构是以下哪种组合？', 1, 'suifeng', 1, 5, 'JDK 1.8 中 HashMap 采用数组+链表+红黑树实现，当链表长度大于8且数组容量大于64时树化。', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(2, '下列关于 Java 垃圾回收（GC）垃圾收集器的说法，哪些是正确的？', 2, 'suifeng', 2, 10, 'CMS收集器以获取最短回收停顿时间为目标，基于标记-清除算法；G1将堆划分为多个等大的Region；ZGC采用着色指针和读屏障技术。', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(3, 'MySQL中的InnoDB存储引擎，默认的事务隔离级别是可重复读（Repeatable Read）。', 1, 'admin', 3, 5, 'InnoDB默认隔离级别为REPEATABLE READ，并通过Next-Key Lock锁算法在很大程度上解决了幻读问题。', 'admin', NOW(), 'admin', NOW(), 0),
(4, '请简述 Spring 中 Bean 的生命周期流程。', 3, 'suifeng', 4, 15, '生命周期核心步骤包括：实例化(Instantiation) -> 属性赋值(Populate) -> Aware接口回调 -> BeanPostProcessor前置处理 -> 初始化(InitializingBean/init-method) -> BeanPostProcessor后置处理 -> 投入使用 -> 销毁(DisposableBean)。', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(5, '在多线程编程中，volatile 关键字能够保证什么特性？', 2, 'admin', 1, 5, 'volatile 保证线程间的变量可见性并禁止指令重排序，但不保证复合操作的原子性。', 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of subject_radio (单选题选项表 - 对应 subject_id = 1, 5)
-- option_type: 1-A, 2-B, 3-C, 4-D
-- is_correct: 1-正确, 0-错误
-- ----------------------------
TRUNCATE TABLE `subject_radio`;
INSERT INTO `subject_radio` (`id`, `subject_id`, `option_type`, `option_content`, `is_correct`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 1, '数组 + 链表 + 红黑树', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(2, 1, 2, '数组 + 双向链表', 0, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(3, 1, 3, '单纯的平衡二叉树', 0, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(4, 1, 4, '跳表 + 哈希散列表', 0, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(5, 5, 1, '可见性与有序性（禁止指令重排序）', 1, 'admin', NOW(), 'admin', NOW(), 0),
(6, 5, 2, '原子性与可见性', 0, 'admin', NOW(), 'admin', NOW(), 0),
(7, 5, 3, '原子性、可见性与有序性', 0, 'admin', NOW(), 'admin', NOW(), 0),
(8, 5, 4, '仅保证原子性', 0, 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of subject_multiple (多选题选项表 - 对应 subject_id = 2)
-- ----------------------------
TRUNCATE TABLE `subject_multiple`;
INSERT INTO `subject_multiple` (`id`, `subject_id`, `option_type`, `option_content`, `is_correct`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 2, 1, 'CMS 收集器以获取最短回收停顿时间为目标，采用标记-清除算法', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(2, 2, 2, 'G1 收集器将整个 Java 堆划分为多个大小相等的独立 Region 区域', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(3, 2, 3, 'Serial 收集器是单线程收集器，在进行垃圾回收时必须暂停其他所有工作线程', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(4, 2, 4, 'Parallel Scavenge 收集器是一款专门用于老年代的并发标记清除收集器', 0, 'suifeng', NOW(), 'suifeng', NOW(), 0);

-- ----------------------------
-- Records of subject_judge (判断题表 - 对应 subject_id = 3)
-- is_correct: 1-正确, 0-错误
-- ----------------------------
TRUNCATE TABLE `subject_judge`;
INSERT INTO `subject_judge` (`id`, `subject_id`, `is_correct`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 3, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of subject_brief (简答题答案表 - 对应 subject_id = 4)
-- ----------------------------
TRUNCATE TABLE `subject_brief`;
INSERT INTO `subject_brief` (`id`, `subject_id`, `subject_answer`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 4, 'Spring Bean 的生命周期大体经历以下几个阶段：
1. 实例化：通过反射创建 Bean 对象实例；
2. 属性填充：依赖注入（DI）注入相关属性与依赖；
3. Aware 接口回调：如 BeanNameAware、BeanFactoryAware、ApplicationContextAware 等；
4. 前置处理：执行 BeanPostProcessor.postProcessBeforeInitialization；
5. 初始化：执行 @PostConstruct、InitializingBean 的 afterPropertiesSet、以及自定义 init-method；
6. 后置处理：执行 BeanPostProcessor.postProcessAfterInitialization（此处可创建 AOP 代理）；
7. 使用中：应用上下文正常使用 Bean；
8. 销毁：容器关闭时执行 @PreDestroy、DisposableBean 的 destroy 及自定义 destroy-method。', 'suifeng', NOW(), 'suifeng', NOW(), 0);

-- ----------------------------
-- Records of subject_mapping (题目分类标签关联映射表)
-- 建立 subject_id 与 category_id、label_id 的多对多映射
-- ----------------------------
TRUNCATE TABLE `subject_mapping`;
INSERT INTO `subject_mapping` (`id`, `subject_id`, `category_id`, `label_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 101, 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(2, 2, 102, 3, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(3, 3, 104, 8, 'admin', NOW(), 'admin', NOW(), 0),
(4, 4, 103, 5, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(5, 5, 106, 10, 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of subject_liked (题目点赞表)
-- ----------------------------
TRUNCATE TABLE `subject_liked`;
INSERT INTO `subject_liked` (`id`, `subject_id`, `like_user_id`, `status`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 'admin', 1, 'admin', NOW(), 'admin', NOW(), 0),
(2, 1, 'suifeng', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(3, 2, 'admin', 1, 'admin', NOW(), 'admin', NOW(), 0),
(4, 4, 'test_user', 1, 'test_user', NOW(), 'test_user', NOW(), 0);


-- ====================================================================
-- 3. 刷题与套题体系 (微服务 sf-club-practice)
-- ====================================================================

-- ----------------------------
-- Records of practice_set (套题信息表)
-- set_type: 1-实时生成, 2-预设套题
-- ----------------------------
TRUNCATE TABLE `practice_set`;
INSERT INTO `practice_set` (`id`, `set_name`, `set_type`, `set_heat`, `set_desc`, `primary_category_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, '2024届Java核心技术高频考点必刷试卷', 2, 520, '精选一线互联网大厂Java基础、并发、JVM和框架高频考点，夯实基本功。', 1, 'admin', NOW(), 'admin', NOW(), 0),
(2, 'MySQL与Redis高并发高可用经典真题集', 2, 380, '覆盖InnoDB事务原理、MVCC、索引调优、Redis分布式缓存核心实战题目。', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0);

-- ----------------------------
-- Records of practice_set_detail (套题题目详情表)
-- 关联 set_id 与 subject_id
-- ----------------------------
TRUNCATE TABLE `practice_set_detail`;
INSERT INTO `practice_set_detail` (`id`, `set_id`, `subject_id`, `subject_type`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0),
(2, 1, 2, 2, 'admin', NOW(), 'admin', NOW(), 0),
(3, 1, 3, 3, 'admin', NOW(), 'admin', NOW(), 0),
(4, 1, 4, 4, 'admin', NOW(), 'admin', NOW(), 0),
(5, 2, 3, 3, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(6, 2, 5, 1, 'suifeng', NOW(), 'suifeng', NOW(), 0);

-- ----------------------------
-- Records of practice_info (练题汇总记录表)
-- complete_status: 1-已完成, 0-未完成
-- ----------------------------
TRUNCATE TABLE `practice_info`;
INSERT INTO `practice_info` (`id`, `set_id`, `complete_status`, `time_use`, `submit_time`, `correct_rate`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 1, '00:12:45', NOW(), 75.00, 'admin', NOW(), 'admin', NOW(), 0),
(2, 1, 1, '00:15:30', NOW(), 100.00, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(3, 2, 0, '00:05:20', NULL, 0.00, 'test_user', NOW(), 'test_user', NOW(), 0);

-- ----------------------------
-- Records of practice_detail (练题答题明细表)
-- answer_status: 1-正确, 0-错误
-- ----------------------------
TRUNCATE TABLE `practice_detail`;
INSERT INTO `practice_detail` (`id`, `practice_id`, `subject_id`, `subject_type`, `answer_status`, `answer_content`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 1, 1, 1, '1', 'admin', NOW(), 'admin', NOW(), 0),
(2, 1, 2, 2, 1, '1,2,3', 'admin', NOW(), 'admin', NOW(), 0),
(3, 1, 3, 3, 1, '1', 'admin', NOW(), 'admin', NOW(), 0),
(4, 1, 4, 4, 0, '简要回答了实例化与初始化...', 'admin', NOW(), 'admin', NOW(), 0),
(5, 2, 1, 1, 1, '1', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(6, 2, 2, 2, 1, '1,2,3', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(7, 2, 3, 3, 1, '1', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(8, 2, 4, 4, 1, '完整答出八个核心生命周期步骤', 'suifeng', NOW(), 'suifeng', NOW(), 0);


-- ====================================================================
-- 4. 模拟面试体系 (微服务 sf-club-interview)
-- ====================================================================

-- ----------------------------
-- Records of interview_history (面试汇总记录表)
-- ----------------------------
TRUNCATE TABLE `interview_history`;
INSERT INTO `interview_history` (`id`, `avg_score`, `key_words`, `tip`, `interview_url`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 88.50, 'Java核心,Spring生态,MySQL索引,Redis缓存', '基础概念清晰，能深入理解底层实现原理，表达流畅条理清晰。建议在分布式一致性协议方面多加积累。', 'https://oss.sfclub.com/interview/report/interview_001.pdf', 'admin', NOW(), 'admin', NOW(), 0),
(2, 92.00, 'JVM调优,并发编程,架构设计,分布式事务', '技术深度优异，工程实操经验丰富，对底层源码与调优参数掌握透彻。', 'https://oss.sfclub.com/interview/report/interview_002.pdf', 'suifeng', NOW(), 'suifeng', NOW(), 0);

-- ----------------------------
-- Records of interview_question_history (面试题目记录表)
-- 关联 interview_id
-- ----------------------------
TRUNCATE TABLE `interview_question_history`;
INSERT INTO `interview_question_history` (`id`, `interview_id`, `score`, `key_words`, `question`, `answer`, `user_answer`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 90.00, 'HashMap,扩容机制', '请谈谈 HashMap 在 JDK 1.8 中 put 操作的具体执行过程及扩容机制？', '先计算 key 的 hash 确定桶下标，若桶为空则直接插入；若为链表遍历比较，若达8且数组容量>=64则转红黑树；若达到阈值进行 resize 扩容2倍，高低位链表迁移避免 rehash。', '先算哈希值定位，为空直接放，冲突时用链表或红黑树存储，超过阈值进行2倍扩容。', 'admin', NOW(), 'admin', NOW(), 0),
(2, 1, 85.00, 'MVCC,UndoLog', 'MySQL 中多版本并发控制（MVCC）是如何通过 ReadView 和 Undo Log 实现非阻塞读的？', '每次修改都会生成 Undo Log 版本链，并在聚簇索引中保存最新事务ID和回滚指针。事务查询时生成 ReadView，通过对比活动事务ID范围判断哪个版本对当前事务可见。', '通过每行隐藏的事务版本号和回滚指针形成版本链，查询时创建快照判断可见性。', 'admin', NOW(), 'admin', NOW(), 0),
(3, 2, 95.00, 'JVM,GC停顿', 'G1 垃圾收集器是如何平衡吞吐量和最大延迟目标的？', 'G1 将内存划分为均匀的 Region，通过记忆集（RSet）解决跨代引用，并维护停顿预测模型，优先回收价值最高、垃圾最多的 Region。', '基于 Region 划分，并根据用户设定的期望停顿时间动态选取回收集合（CSet）。', 'suifeng', NOW(), 'suifeng', NOW(), 0);


-- ====================================================================
-- 5. 圈子互动与敏感词体系 (微服务 sf-club-circle)
-- ====================================================================

-- ----------------------------
-- Records of sensitive_words (敏感词表)
-- type: 1-黑名单, 2-白名单
-- ----------------------------
TRUNCATE TABLE `sensitive_words`;
INSERT INTO `sensitive_words` (`id`, `words`, `type`, `is_deleted`) VALUES
(1, '代考代刷', 1, 0),
(2, '作弊外挂', 1, 0),
(3, '泄露真题', 1, 0),
(4, '政治敏感违禁词', 1, 0),
(5, 'sf-club', 2, 0),
(6, '开源社区', 2, 0);

-- ----------------------------
-- Records of share_circle (圈子信息表)
-- parent_id: -1 为大类
-- ----------------------------
TRUNCATE TABLE `share_circle`;
INSERT INTO `share_circle` (`id`, `parent_id`, `circle_name`, `icon`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, -1, '技术前沿与研讨', 'https://oss.sfclub.com/circle/tech.png', 'admin', NOW(), 'admin', NOW(), 0),
(2, -1, '职场交流与招聘', 'https://oss.sfclub.com/circle/career.png', 'admin', NOW(), 'admin', NOW(), 0),
-- 子圈子
(101, 1, 'Java技术交流圈', 'https://oss.sfclub.com/circle/java_sub.png', 'admin', NOW(), 'admin', NOW(), 0),
(102, 1, '架构设计与微服务', 'https://oss.sfclub.com/circle/arch_sub.png', 'suifeng', NOW(), 'suifeng', NOW(), 0),
(103, 2, '秋招春招面经打卡', 'https://oss.sfclub.com/circle/interview_sub.png', 'admin', NOW(), 'admin', NOW(), 0);

-- ----------------------------
-- Records of share_moment (动态信息表)
-- 关联 circle_id
-- ----------------------------
TRUNCATE TABLE `share_moment`;
INSERT INTO `share_moment` (`id`, `circle_id`, `content`, `pic_urls`, `reply_count`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 101, '今天刷完了 Java 并发编程核心 30 题，把 AQS 的独占锁和共享锁模式理顺了，打卡加油！', 'https://oss.sfclub.com/moment/study01.jpg', 2, 'admin', NOW(), 'admin', NOW(), 0),
(2, 103, '分享刚刚美团后端二面的面经：重点深挖了 MySQL 联合索引最左匹配原则和慢 SQL 优化方案。', 'https://oss.sfclub.com/moment/interview01.jpg', 1, 'test_user', NOW(), 'test_user', NOW(), 0);

-- ----------------------------
-- Records of share_comment_reply (评论及回复信息表)
-- reply_type: 1-评论(parent_id=-1), 2-回复(parent_id=评论id)
-- ----------------------------
TRUNCATE TABLE `share_comment_reply`;
INSERT INTO `share_comment_reply` (`id`, `moment_id`, `reply_type`, `to_id`, `to_user`, `to_user_author`, `reply_id`, `reply_user`, `reply_author`, `content`, `pic_urls`, `parent_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 1, 1, 1, 'admin', 1, NULL, NULL, 0, '写得太棒了，AQS 的 Node 状态转换尤其是 SIGNAL 状态是理解的重中之重！', NULL, -1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(2, 1, 2, 1, 'suifeng', 0, 1, 'admin', 1, '谢谢随风大佬指点，马上回去把这一块在笔记里补充好！', NULL, 1, 'admin', NOW(), 'admin', NOW(), 0),
(3, 2, 1, 2, 'test_user', 1, NULL, NULL, 0, '求楼主分享慢 SQL 的 explain 执行计划排查经验！', NULL, -1, 'vip_coder', NOW(), 'vip_coder', NOW(), 0);

-- ----------------------------
-- Records of share_message (消息表)
-- is_read: 1-已读, 2-未读
-- ----------------------------
TRUNCATE TABLE `share_message`;
INSERT INTO `share_message` (`id`, `from_id`, `to_id`, `content`, `is_read`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1, 'suifeng', 'admin', '随风在你的动态中发表了一条精彩评论，快去看看吧！', 1, 'suifeng', NOW(), 'suifeng', NOW(), 0),
(2, 'system', 'test_user', '恭喜你！昨日刷题榜排名前 10%，获得系统成就徽章！', 2, 'system', NOW(), 'system', NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;
