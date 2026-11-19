# 随风 Club · 项目架构与规范指南 (agents.md)

## 题库三级分类核心架构规范

在本项目中，题库分类遵循严格的三级层次体系设计：

1. **一级分类：职位 (Job Roles / Positions)**
   - 代表求职者或学习者的技术职业方向与专业通道。
   - 示例：
     - `后端开发` (Backend Developer)
     - `Web 前端` (Frontend Developer)
     - `移动端开发` (Mobile iOS/Android)
     - `算法与数据结构` (Algorithm & Data Structures)
     - `高并发系统架构` (Distributed Architecture)
     - `大数据与数仓` (Big Data & Data Warehouse)
     - `测试与质量保障` (QA & Test Development)
     - `运维与DevOps` (DevOps & SRE)
     - `人工智能与大模型` (AI & LLM Engineer)
     - `云原生与微服务` (Cloud Native & Kubernetes)
   - 展现规范：支持行内快捷展示及「展开/收起」切换更多职位。本级别**不需要**图标。

2. **二级分类：技能与中间件 (Skills & Middlewares)**
   - 代表对应职位下必须掌握的核心编程语言、技术栈、核心中间件与关键组件。
   - 示例（以「后端开发」为例）：
     - `Java基础` (Java Core / JVM)
     - `Spring生态` (Spring Boot / Spring Cloud)
     - `MySQL数据库` (MySQL / InnoDB)
     - `Redis缓存` (Redis / Cache)
     - `多线程与并发` (JUC / Multithreading)
     - `Kafka/RocketMQ` (Message Queue)
     - `微服务架构` (Dubbo / Nacos / Gateway)
   - 示例（以「Web 前端」为例）：
     - `JavaScript / TypeScript`
     - `Vue.js 生态`
     - `React.js 生态`
     - `Node.js 全栈`
     - `网络协议与安全`
   - **图标规范**：**只有二级分类需要展示对应的技术/中间件 SVG 图标**（如 Java 咖啡杯、Spring 绿色叶子、MySQL 海豚、Redis 红色魔方等），以便学员直观识别技能板块。

3. **三级分类：知识点标签 (Knowledge Point Labels)**
   - 代表具体技能或中间件深度细分后的知识点、高频考点与源码机制。
   - 示例（以「Java基础 / 集合框架」为例）：
     - `HashMap 底层实现`
     - `ConcurrentHashMap`
     - `垃圾回收GC`
     - `类加载双亲委派`
     - `事务与MVCC`
     - `B+树索引机制`
     - `AQS与ReentrantLock`
     - `线程池核心参数与调优`
   - 展现规范：胶囊 Pill 药丸式过滤标签，支持多选或单选高亮。本级别**不需要**图标。
