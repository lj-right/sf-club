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
 Description: 反推自 sf-club 后端 Java Bean 实体类及 MyBatis/MyBatis-Plus 映射配置
 Total Tables: 25 张表
*/

CREATE DATABASE IF NOT EXISTS `sf-club` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `sf-club`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. Table structure for auth_permission (权限表)
-- ----------------------------
DROP TABLE IF EXISTS `auth_permission`;
CREATE TABLE `auth_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) DEFAULT NULL COMMENT '权限名称',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父级id',
  `type` tinyint(4) DEFAULT NULL COMMENT '权限类型 1菜单 2操作/按钮',
  `menu_url` varchar(255) DEFAULT NULL COMMENT '菜单路由/访问路径',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态 0启用 1禁用',
  `show` tinyint(4) DEFAULT 0 COMMENT '是否展示 0展示 1隐藏',
  `icon` varchar(128) DEFAULT NULL COMMENT '图标',
  `permission_key` varchar(64) DEFAULT NULL COMMENT '权限唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_key` (`permission_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ----------------------------
-- 2. Table structure for auth_role (角色表)
-- ----------------------------
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE `auth_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(64) DEFAULT NULL COMMENT '角色名称',
  `role_key` varchar(64) DEFAULT NULL COMMENT '角色唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. Table structure for auth_role_permission (角色权限关联表)
-- ----------------------------
DROP TABLE IF EXISTS `auth_role_permission`;
CREATE TABLE `auth_role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `permission_id` bigint(20) NOT NULL COMMENT '权限id',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  KEY `idx_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ----------------------------
-- 4. Table structure for auth_user (用户信息表)
-- ----------------------------
DROP TABLE IF EXISTS `auth_user`;
CREATE TABLE `auth_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(64) NOT NULL COMMENT '用户名',
  `nick_name` varchar(64) DEFAULT NULL COMMENT '昵称',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `password` varchar(128) DEFAULT NULL COMMENT '密码',
  `sex` tinyint(4) DEFAULT NULL COMMENT '性别 0女 1男',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态 0启用 1禁用',
  `introduce` varchar(255) DEFAULT NULL COMMENT '个人介绍',
  `ext_json` text DEFAULT NULL COMMENT '扩展字段json',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ----------------------------
-- 5. Table structure for auth_user_role (用户角色关联表)
-- ----------------------------
DROP TABLE IF EXISTS `auth_user_role`;
CREATE TABLE `auth_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 6. Table structure for interview_history (面试汇总记录表)
-- ----------------------------
DROP TABLE IF EXISTS `interview_history`;
CREATE TABLE `interview_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `avg_score` decimal(5,2) DEFAULT NULL COMMENT '平均分',
  `key_words` varchar(255) DEFAULT NULL COMMENT '面试关键字',
  `tip` varchar(255) DEFAULT NULL COMMENT '面试评价',
  `interview_url` varchar(255) DEFAULT NULL COMMENT '简历地址',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试汇总记录表';

-- ----------------------------
-- 7. Table structure for interview_question_history (面试题目记录表)
-- ----------------------------
DROP TABLE IF EXISTS `interview_question_history`;
CREATE TABLE `interview_question_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `interview_id` bigint(20) NOT NULL COMMENT '面试场次ID',
  `score` decimal(5,2) DEFAULT NULL COMMENT '均分/得分',
  `key_words` varchar(255) DEFAULT NULL COMMENT '面试关键字',
  `question` text DEFAULT NULL COMMENT '问题',
  `answer` text DEFAULT NULL COMMENT '标准答案',
  `user_answer` text DEFAULT NULL COMMENT '用户答案',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_interview_id` (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试题目记录表';

-- ----------------------------
-- 8. Table structure for practice_detail (练题详情表)
-- ----------------------------
DROP TABLE IF EXISTS `practice_detail`;
CREATE TABLE `practice_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `practice_id` bigint(20) NOT NULL COMMENT '练题id',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `subject_type` tinyint(4) DEFAULT NULL COMMENT '题目类型 1单选 2多选 3判断 4简答',
  `answer_status` tinyint(4) DEFAULT NULL COMMENT '是否正确 1正确 0错误',
  `answer_content` varchar(500) DEFAULT NULL COMMENT '答案内容',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_practice_id` (`practice_id`),
  KEY `idx_practice_subject` (`practice_id`, `subject_id`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练题详情表';

-- ----------------------------
-- 9. Table structure for practice_info (练题汇总记录表)
-- ----------------------------
DROP TABLE IF EXISTS `practice_info`;
CREATE TABLE `practice_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `set_id` bigint(20) NOT NULL COMMENT '套题id',
  `complete_status` tinyint(4) DEFAULT 0 COMMENT '完成情况 1完成 0未完成',
  `time_use` varchar(32) DEFAULT NULL COMMENT '所用时间',
  `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
  `correct_rate` decimal(5,2) DEFAULT NULL COMMENT '正确率',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_set_id` (`set_id`),
  KEY `idx_created_by` (`created_by`, `complete_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练题汇总记录表';

-- ----------------------------
-- 10. Table structure for practice_set (套题信息表)
-- ----------------------------
DROP TABLE IF EXISTS `practice_set`;
CREATE TABLE `practice_set` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `set_name` varchar(128) DEFAULT NULL COMMENT '套题名称',
  `set_type` tinyint(4) DEFAULT NULL COMMENT '1实时生成 2预设套题',
  `set_heat` int(11) DEFAULT 0 COMMENT '套题热度',
  `set_desc` varchar(255) DEFAULT NULL COMMENT '套题描述',
  `primary_category_id` bigint(20) DEFAULT NULL COMMENT '大类id',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type_cat` (`set_type`, `primary_category_id`),
  KEY `idx_set_name` (`set_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套题信息表';

-- ----------------------------
-- 11. Table structure for practice_set_detail (套题题目详情表)
-- ----------------------------
DROP TABLE IF EXISTS `practice_set_detail`;
CREATE TABLE `practice_set_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `set_id` bigint(20) NOT NULL COMMENT '套题id',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `subject_type` tinyint(4) DEFAULT NULL COMMENT '题目类型 1单选 2多选 3判断 4简答',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_set_id` (`set_id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套题题目详情表';

-- ----------------------------
-- 12. Table structure for sensitive_words (敏感词表)
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `words` varchar(255) NOT NULL COMMENT '内容',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1=黑名单 2=白名单',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_words` (`words`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ----------------------------
-- 13. Table structure for share_circle (圈子信息表)
-- ----------------------------
DROP TABLE IF EXISTS `share_circle`;
CREATE TABLE `share_circle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '圈子ID',
  `parent_id` bigint(20) DEFAULT -1 COMMENT '父级ID,-1为大类',
  `circle_name` varchar(64) DEFAULT NULL COMMENT '圈子名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '圈子图片',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子信息表';

-- ----------------------------
-- 14. Table structure for share_comment_reply (评论及回复信息表)
-- ----------------------------
DROP TABLE IF EXISTS `share_comment_reply`;
CREATE TABLE `share_comment_reply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `moment_id` bigint(20) NOT NULL COMMENT '原始动态ID',
  `reply_type` tinyint(4) NOT NULL COMMENT '回复类型 1评论 2回复',
  `to_id` bigint(20) DEFAULT NULL COMMENT '评论目标id',
  `to_user` varchar(64) DEFAULT NULL COMMENT '评论人',
  `to_user_author` tinyint(4) DEFAULT 0 COMMENT '评论人是否作者 1=是 0=否',
  `reply_id` bigint(20) DEFAULT NULL COMMENT '回复目标id',
  `reply_user` varchar(64) DEFAULT NULL COMMENT '回复人',
  `reply_author` tinyint(4) DEFAULT 0 COMMENT '回复人是否作者 1=是 0=否',
  `content` varchar(500) DEFAULT NULL COMMENT '内容',
  `pic_urls` varchar(1024) DEFAULT NULL COMMENT '图片内容',
  `parent_id` bigint(20) DEFAULT -1 COMMENT '父级评论ID',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_moment_id` (`moment_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论及回复信息表';

-- ----------------------------
-- 15. Table structure for share_message (消息表)
-- ----------------------------
DROP TABLE IF EXISTS `share_message`;
CREATE TABLE `share_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `from_id` varchar(64) DEFAULT NULL COMMENT '来自人',
  `to_id` varchar(64) DEFAULT NULL COMMENT '送达人',
  `content` varchar(500) DEFAULT NULL COMMENT '消息内容',
  `is_read` tinyint(4) DEFAULT 2 COMMENT '是否被阅读 1是 2否',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_to_read` (`to_id`, `is_read`),
  KEY `idx_from_id` (`from_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ----------------------------
-- 16. Table structure for share_moment (动态信息表)
-- ----------------------------
DROP TABLE IF EXISTS `share_moment`;
CREATE TABLE `share_moment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `circle_id` bigint(20) NOT NULL COMMENT '圈子ID',
  `content` text DEFAULT NULL COMMENT '动态内容',
  `pic_urls` varchar(1024) DEFAULT NULL COMMENT '动态图片内容',
  `reply_count` int(11) DEFAULT 0 COMMENT '回复数',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否被删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态信息表';

-- ----------------------------
-- 17. Table structure for subject_brief (简答题信息表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_brief`;
CREATE TABLE `subject_brief` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `subject_answer` text DEFAULT NULL COMMENT '题目答案',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简答题信息表';

-- ----------------------------
-- 18. Table structure for subject_category (题目分类表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_category`;
CREATE TABLE `subject_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_name` varchar(64) DEFAULT NULL COMMENT '分类名称',
  `category_type` tinyint(4) DEFAULT NULL COMMENT '分类类型 1岗位大类 2二级分类',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图标连接',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父级id',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '删除标志 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_type` (`category_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- ----------------------------
-- 19. Table structure for subject_info (题目信息表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_info`;
CREATE TABLE `subject_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_name` varchar(128) DEFAULT NULL COMMENT '题目名称',
  `subject_difficult` tinyint(4) DEFAULT NULL COMMENT '题目难度 1初级 2中级 3高级',
  `settle_name` varchar(64) DEFAULT NULL COMMENT '出题人名',
  `subject_type` tinyint(4) DEFAULT NULL COMMENT '题目类型 1单选 2多选 3判断 4简答',
  `subject_score` tinyint(4) DEFAULT 0 COMMENT '题目分数',
  `subject_parse` text DEFAULT NULL COMMENT '题目解析',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type_diff` (`subject_type`, `subject_difficult`),
  KEY `idx_settle_name` (`settle_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目信息表';

-- ----------------------------
-- 20. Table structure for subject_judge (判断题信息表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_judge`;
CREATE TABLE `subject_judge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `is_correct` tinyint(4) DEFAULT NULL COMMENT '是否正确 1正确 0错误',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='判断题信息表';

-- ----------------------------
-- 21. Table structure for subject_label (题目标签表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_label`;
CREATE TABLE `subject_label` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `label_name` varchar(64) DEFAULT NULL COMMENT '标签名称',
  `category_id` bigint(20) DEFAULT NULL COMMENT '标签分类id',
  `sort_num` int(11) DEFAULT 0 COMMENT '排序',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';

-- ----------------------------
-- 22. Table structure for subject_liked (题目点赞表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_liked`;
CREATE TABLE `subject_liked` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `like_user_id` varchar(64) NOT NULL COMMENT '点赞人id',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '点赞状态 1点赞 0不点赞',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_user` (`subject_id`, `like_user_id`),
  KEY `idx_user_liked` (`like_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目点赞表';

-- ----------------------------
-- 23. Table structure for subject_mapping (题目分类关系表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_mapping`;
CREATE TABLE `subject_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `category_id` bigint(20) NOT NULL COMMENT '分类id',
  `label_id` bigint(20) NOT NULL COMMENT '标签id',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_subject_cat_label` (`subject_id`, `category_id`, `label_id`),
  KEY `idx_label_cat` (`label_id`, `category_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类关系表';

-- ----------------------------
-- 24. Table structure for subject_multiple (多选题信息表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_multiple`;
CREATE TABLE `subject_multiple` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `option_type` tinyint(4) DEFAULT NULL COMMENT '选项类型 1-A 2-B 3-C 4-D...',
  `option_content` varchar(500) DEFAULT NULL COMMENT '选项内容',
  `is_correct` tinyint(4) DEFAULT NULL COMMENT '是否正确 1正确 0错误',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多选题信息表';

-- ----------------------------
-- 25. Table structure for subject_radio (单选题信息表)
-- ----------------------------
DROP TABLE IF EXISTS `subject_radio`;
CREATE TABLE `subject_radio` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_id` bigint(20) NOT NULL COMMENT '题目id',
  `option_type` tinyint(4) DEFAULT NULL COMMENT '选项类型 1-A 2-B 3-C 4-D...',
  `option_content` varchar(500) DEFAULT NULL COMMENT '选项内容',
  `is_correct` tinyint(4) DEFAULT NULL COMMENT '是否正确 1正确 0错误',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单选题信息表';

SET FOREIGN_KEY_CHECKS = 1;
