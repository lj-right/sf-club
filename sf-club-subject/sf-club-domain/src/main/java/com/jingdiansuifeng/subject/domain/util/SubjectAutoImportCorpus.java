package com.jingdiansuifeng.subject.domain.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 自动录取题目真题题库语料与清洗判定组件
 */
public class SubjectAutoImportCorpus {

    private static final Pattern CLEAN_PREFIX_PATTERN = Pattern.compile("^[#0-9.\\s⭐️★、\\-]+");
    private static final Pattern CLEAN_SYMBOLS_PATTERN = Pattern.compile("[⭐️★]");
    private static final Pattern CLEAN_INTERVIEWER_PATTERN = Pattern.compile("^面试官[：:]\\s*");
    private static final Pattern CLEAN_MARKDOWN_LINK_PATTERN = Pattern.compile("\\[(.*?)\\]\\(.*?\\)");

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionItem {
        private String title;
        private String answer;
        private Integer categoryId;
        private String defaultLabel;
    }

    /**
     * 清洗题目标题，去除特殊符号与多余前后缀
     */
    public static String cleanTitle(String rawTitle) {
        if (StringUtils.isBlank(rawTitle)) {
            return "";
        }
        String t = CLEAN_PREFIX_PATTERN.matcher(rawTitle).replaceAll("");
        t = CLEAN_SYMBOLS_PATTERN.matcher(t).replaceAll("");
        t = CLEAN_INTERVIEWER_PATTERN.matcher(t).replaceAll("");
        t = CLEAN_MARKDOWN_LINK_PATTERN.matcher(t).replaceAll("$1");
        t = t.trim();
        if (t.length() > 125) {
            t = t.substring(0, 122) + "...";
        }
        return t;
    }

    /**
     * 智能推断题目难度等级 (1: 简单, 2: 中等, 3: 困难)
     */
    public static int determineDifficulty(String title) {
        if (StringUtils.isBlank(title)) {
            return 2;
        }
        // 简单级别特征词
        String[] easyKeywords = {"区别", "是什么", "概念", "优缺点", "有哪些", "定义", "理解", "应用场景"};
        for (String kw : easyKeywords) {
            if (title.contains(kw)) {
                return 1;
            }
        }
        // 困难级别特征词
        String[] hardKeywords = {
            "底层", "源码", "原理", "优化", "OOM", "崩溃", "高并发", "深度",
            "Diff", "Fiber", "AQS", "Watermark", "零拷贝", "两阶段提交", "CrashLoopBackOff"
        };
        for (String kw : hardKeywords) {
            if (title.contains(kw)) {
                return 3;
            }
        }
        return 2;
    }

    /**
     * 根据答案生成前几句题目概要
     */
    public static String generateSummary(String answer) {
        if (StringUtils.isBlank(answer)) {
            return "考查该技术点的底层原理与核心机制，掌握其实现细节与高频应用场景。";
        }
        String[] lines = answer.split("\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (StringUtils.isNotBlank(trimmed) && !trimmed.startsWith("#") && !trimmed.startsWith("```")) {
                sb.append(trimmed).append(" ");
                count++;
                if (count >= 3) {
                    break;
                }
            }
        }
        String summary = sb.toString().replaceAll("[*_`#]", "").trim();
        if (summary.length() > 180) {
            summary = summary.substring(0, 177) + "...";
        }
        return StringUtils.isNotBlank(summary) ? summary : "考查该技术点的底层原理与核心机制，掌握其实现细节与高频应用场景。";
    }

    /**
     * 获取候选真题库
     */
    public static List<QuestionItem> getCandidateQuestions(String source, int limit) {
        List<QuestionItem> all = new ArrayList<>();

        // 1. 后端技术栈 (Java, JVM, Spring, MySQL, Redis, JUC, 高并发架构)
        if (isMatchSource(source, "BACKEND")) {
            all.add(new QuestionItem(
                "HashMap 底层数据结构与扩容机制详解，1.7 与 1.8 有何重大区别？",
                "1. 结构演进：JDK 1.7 采用数组 + 单向链表（头插法，并发下易导致死循环形成环形链表）；JDK 1.8 采用数组 + 链表 + 红黑树（尾插法）。\n" +
                "2. 树化阈值：当链表长度达到 8 且数组容量大于等于 64 时转换为红黑树；若节点数下降至 6 则退化回链表。\n" +
                "3. 扩容设计：负载因子为 0.75，容量始终保持 2 的幂次方，便于通过按位与（hash & (n-1)）快速取模定位桶位。\n" +
                "4. 1.8 扩容优化：节点在扩容后的位置要么是原位置，要么是原位置加上原容量（高位 bit 是否为 1），无需重新计算 hash。",
                101, "集合框架"
            ));

            all.add(new QuestionItem(
                "ConcurrentHashMap 在 JDK 1.7 与 1.8 中的锁实现机制有何本质不同？",
                "1. JDK 1.7 分段锁设计：基于 Segment 数组（继承自 ReentrantLock）实现分段锁，并发度取决于 Segment 数量（默认 16），不同段之间可以并发写。\n" +
                "2. JDK 1.8 锁粒度细化：废弃 Segment 分段锁，改用 Node 数组 + CAS + synchronized 锁住链表或红黑树的头节点，锁粒度进一步降低到每个 Hash 桶首节点。\n" +
                "3. 扩容协助：引入 ForwardingNode 标记迁移中节点，多线程可协同参与数据迁移，大幅提升高并发读写吞吐。",
                101, "集合框架"
            ));

            all.add(new QuestionItem(
                "JVM 运行时数据区构成与垃圾回收 GC 经典算法及垃圾收集器演进",
                "1. 内存区域：线程共享区包含堆内存（Heap）和方法区（元空间 Metaspace）；线程私有区包含虚拟机栈、本地方法栈和程序计数器。\n" +
                "2. 垃圾标记：采用根搜索算法（GC Roots Tracing）判定对象存活性，杜绝引用计数算法的循环引用问题。\n" +
                "3. 垃圾收集器演进：Serial -> Parallel Scavenge/Old -> CMS（并发标记清除）-> G1（分区化 Region 架构）-> ZGC（基于染色指针与读屏障，实现毫秒级停顿）。",
                102, "垃圾回收GC"
            ));

            all.add(new QuestionItem(
                "JVM 类加载的双亲委派机制是什么？如何打破双亲委派机制？",
                "1. 双亲委派模型：当类加载器收到类加载请求时，先委托给父类加载器处理，逐层向上直到引导类加载器（Bootstrap ClassLoader），只有父加载器无法完成时才由子类尝试加载。\n" +
                "2. 核心作用：避免核心类库被篡改，保证 JVM 运行时环境的安全与唯一性。\n" +
                "3. 打破双亲委派：重写 ClassLoader 的 loadClass 方法（如 Tomcat 隔离 Web 应用类库、OSGi 热部署模型、SPI 机制中通过线程上下文类加载器 ThreadContextClassLoader 反向加载）。",
                102, "类加载与双亲委派"
            ));

            all.add(new QuestionItem(
                "Spring 框架三级缓存机制是如何解决单例 Bean 循环依赖的？",
                "1. 第一级缓存（singletonObjects）：存放完全初始化好的成品单例 Bean。\n" +
                "2. 第二级缓存（earlySingletonObjects）：存放提前暴露的半成品 Bean（尚未填充属性和初始化）。\n" +
                "3. 第三级缓存（singletonFactories）：存放 ObjectFactory 工厂对象，用于在发生循环依赖且 Bean 涉及 AOP 时，提前生成代理对象。\n" +
                "4. 限制场景：Spring 无法解决基于构造器注入的循环依赖，亦无法直接解决非单例（Prototype）Bean 的循环依赖。",
                103, "三级缓存循环依赖"
            ));

            all.add(new QuestionItem(
                "Spring Boot 自动装配原理（Auto-Configuration）底层源码实现解析",
                "1. 核心注解：@SpringBootApplication 组合了 @EnableAutoConfiguration。\n" +
                "2. 加载机制：通过 SpringFactoriesLoader（或 2.7+ 的 AutoConfiguration.imports）读取 META-INF/spring.factories 中配置的自动配置类列表。\n" +
                "3. 条件装配：结合各类 @ConditionalOnClass、@ConditionalOnMissingBean、@ConditionalOnProperty 等条件注解，实现按需自动装配。",
                103, "Spring Boot自动装配"
            ));

            all.add(new QuestionItem(
                "MySQL InnoDB 引擎索引底层 B+ 树数据结构与为什么不选用 B 树或红黑树？",
                "1. 为什么不选红黑树：红黑树为二叉树，数据量巨大时树高太深，导致磁盘 I/O 次数过多。\n" +
                "2. 为什么不选 B 树：B 树所有节点都存储行数据，非叶子节点可容纳的索引键较少，扇出小、树高较高；且区间查询需中序遍历。\n" +
                "3. B+ 树优势：非叶子节点仅存储索引键和指针，扇出极大（通常 3 层即可容纳两千多万数据），叶子节点通过双向链表相连，极大优化了范围扫描与顺序 I/O。",
                104, "索引优化"
            ));

            all.add(new QuestionItem(
                "MySQL 事务四大特性与多版本并发控制 MVCC 实现底层原理",
                "1. ACID 保证：原子性由 undo log 保证，持久性由 redo log 与 WAL 保证，隔离性由锁与 MVCC 保证，一致性由前三者共同达成。\n" +
                "2. MVCC 原理：依赖每行记录的隐藏列（trx_id 事务版本号、roll_pointer 回滚指针）、undo log 历史版本链与 Read View 一致性视图。\n" +
                "3. Read View 规则：通过比较记录的 trx_id 与当前事务 Read View 中的 min_trx_id、max_trx_id 及活跃事务列表 m_ids，实现非阻塞无锁读（快照读）。",
                104, "事务与MVCC"
            ));

            all.add(new QuestionItem(
                "Redis 核心底层数据结构（Sds、Dict、Quicklist、Skiplist）深度解析",
                "1. 字符串 SDS：带长度 len、空间预分配与惰性释放，二进制安全，常数复杂度获取长度。\n" +
                "2. 字典 Dict：双哈希表结构，采用渐进式 rehash 将缩容扩容开销分摊到每次操作。\n" +
                "3. 跳表 Skiplist：多层随机跳跃链表，支撑 ZSet 实现 O(log N) 复杂度的范围检索与排名。\n" +
                "4. 压缩列表与 Quicklist：连续内存块紧凑存储小数据，节约内存开销。",
                105, "核心数据结构底层"
            ));

            all.add(new QuestionItem(
                "Redis 缓存穿透、缓存击穿、缓存雪崩的成因与高可用解决方案",
                "1. 缓存穿透（查询不存在的数据）：采用布隆过滤器（BloomFilter）前置拦截，或对空结果缓存简短过期时间。\n" +
                "2. 缓存击穿（热点 Key 突发过期）：采用互斥分布式锁（SETNX / Redisson）保证仅一个线程回源 DB，或设置热点数据逻辑永不过期。\n" +
                "3. 缓存雪崩（大量 Key 同一时刻失效）：将缓存过期时间附加随机抖动偏差（random jitter），配合分布式多活架构与降级限流。",
                105, "缓存穿透与击穿"
            ));

            all.add(new QuestionItem(
                "AQS（AbstractQueuedSynchronizer）核心机制与自定义同步组件原理",
                "1. 核心状态：使用 volatile 修饰的 state 整数变量表示同步状态，通过 CAS 完成并发修改。\n" +
                "2. 队列模型：内置 FIFO 双向同步链表队列（Node 节点），维护因争抢锁失败而被阻塞排队的线程。\n" +
                "3. 独占与共享：支持独占模式（如 ReentrantLock）与共享模式（如 CountDownLatch、Semaphore、ReentrantReadWriteLock），子类通过重写 tryAcquire/tryRelease 扩展同步语义。",
                106, "AQS与Lock"
            ));

            all.add(new QuestionItem(
                "线程池 ThreadPoolExecutor 七大核心参数与拒绝策略底层源码解析",
                "1. 核心参数：corePoolSize（核心线程数）、maximumPoolSize（最大线程数）、keepAliveTime（存活时间）、unit（时间单位）、workQueue（阻塞队列）、threadFactory（线程工厂）、handler（拒绝策略）。\n" +
                "2. 执行流程：核心线程未满直接创建 -> 阻塞队列未满入队等待 -> 队列满且未达最大线程数则创建非核心线程 -> 达到最大线程且队列已满触发拒绝策略。\n" +
                "3. 常见策略：AbortPolicy（抛异常）、CallerRunsPolicy（由调用者线程直接执行）、DiscardPolicy（静默丢弃）、DiscardOldestPolicy（丢弃队列最早未处理任务）。",
                106, "线程池底层与拒绝策略"
            ));

            all.add(new QuestionItem(
                "Kafka 如何保证百万级消息的高吞吐？底层零拷贝机制是怎样的？",
                "1. 顺序写磁盘：采用 Append-Only 追加日志文件，利用操作系统磁盘顺序写性能。\n" +
                "2. Page Cache：利用操作系统的页缓存管理冷热数据，避免 JVM GC 开销与频繁磁盘 I/O。\n" +
                "3. 零拷贝技术：使用 Linux sendfile 系统调用，数据直接在内核空间由页缓存传递给网卡缓冲区，跳过用户态两次拷贝与上下文切换。\n" +
                "4. 批量发送与压缩：Producer 端攒批批量发送（batch.size 与 linger.ms），支持 Snappy、Gzip 等高压缩比算法。",
                502, "Kafka高吞吐底层设计"
            ));

            all.add(new QuestionItem(
                "RocketMQ 事务消息底层两阶段提交（2PC）机制与反查机制详解",
                "1. 第一阶段：Producer 发送 Half 消息到 Broker，Broker 放入内部队列 RMQ_SYS_TRANS_HALF_TOPIC，Consumer 无法看到。\n" +
                "2. 第二阶段：Producer 执行本地事务。若事务成功发送 Commit，Broker 将消息移至正式 Topic；若失败发送 Rollback 丢弃。\n" +
                "3. 反查补偿：若网络超时未收到二次确认，Broker 会主动发起事务状态回查，根据业务表真实状态推进最终一致性。",
                502, "RocketMQ事务消息"
            ));

            all.add(new QuestionItem(
                "Redisson 分布式锁看门狗（Watchdog）底层续期机制与红锁原理",
                "1. 看门狗机制：若未显式指定 lock leaseTime，默认开启 Watchdog（默认 30 秒超时），在加锁成功后启动定时任务每隔 10 秒重置一次过期时间，防止业务未完锁提前失效。\n" +
                "2. RedLock 红锁原理：在 N 个独立的物理 Redis 节点上依次获取锁，当在超过半数（N/2 + 1）节点上获取成功且总耗时小于锁有效时间时，才认为成功。",
                503, "Redisson分布式锁源码"
            ));
        }

        // 2. Web 前端技术栈 (Vue, React, JS/TS, 前端工程化)
        if (isMatchSource(source, "FRONTEND")) {
            all.add(new QuestionItem(
                "Vue 2 与 Vue 3 响应式原理深度对比：Object.defineProperty 与 Proxy",
                "1. Vue 2 局限：使用 Object.defineProperty 递归劫持对象属性的 getter/setter，无法原生监听新增/删除属性，以及数组索引直接修改（需借助 $set 及变异方法）。\n" +
                "2. Vue 3 Proxy 优势：基于 ES6 Proxy 原生拦截整个对象的读取、设置、删除及遍历等 13 种拦截操作，支持 Map/Set 等复杂集合。\n" +
                "3. 性能提升：惰性递归代理，只有访问到深层属性时才动态做 reactive 处理，大幅节省首屏初始化内存消耗。",
                201, "响应式原理与Proxy"
            ));

            all.add(new QuestionItem(
                "React 18 Fiber 架构设计思想与时间分片调度机制解析",
                "1. 解决痛点：React 15 以前的 Stack Reconciler 采用递归方式对比 Virtual DOM，一旦组件树深层嵌套则长时间占用主线程，导致浏览器掉帧卡顿。\n" +
                "2. Fiber 结构：将递归渲染转变为双向链表结构的可中断任务单元（Fiber Node），具备 return、child、sibling 指针。\n" +
                "3. 时间分片：基于 MessageChannel 调度在每一帧 16.6ms 内的空闲时间进行协调计算，若主线程有更高优先级事件（如用户点击或输入）则主动让出执行权。",
                202, "Fiber架构与协调机制"
            ));

            all.add(new QuestionItem(
                "JavaScript 事件循环机制（Event Loop）微任务与宏任务执行顺序详解",
                "1. 执行顺序：同步任务 -> 同步执行栈清空 -> 清空当前所有微任务队列 -> 渲染流水线 -> 取出宏任务队列首个任务执行 -> 再次检查微任务。\n" +
                "2. 常见微任务：Promise.then/catch/finally、MutationObserver、queueMicrotask、process.nextTick（Node.js）。\n" +
                "3. 常见宏任务：setTimeout、setInterval、setImmediate（Node.js）、I/O、UI 渲染事件。",
                203, "事件循环EventLoop"
            ));

            all.add(new QuestionItem(
                "前端性能优化核心指标（LCP、FID、CLS）与首屏加载提速方案",
                "1. Web Vitals 核心指标：LCP（最大内容绘制时间）、FID/INP（首次输入延迟/交互就绪）、CLS（累积布局位移）。\n" +
                "2. 资源瘦身：路由懒加载、Tree Shaking、Gzip/Brotli 压缩、精简第三方大包、WebP/AVIF 现代图片格式。\n" +
                "3. 传输加速：利用 CDN 边缘节点分发、HTTP/2 多路复用、强缓存与协商缓存策略、预加载 Link rel=preload。",
                204, "首屏加载性能优化"
            ));
        }

        // 3. 人工智能与大模型 (LLM, RAG, 微调, Agent)
        if (isMatchSource(source, "AI")) {
            all.add(new QuestionItem(
                "Transformer 架构自注意力机制（Self-Attention）与多头注意力（MHA）数学原理",
                "1. 核心公式：Attention(Q, K, V) = softmax(Q * K^T / sqrt(d_k)) * V。\n" +
                "2. 缩放因子：除以 sqrt(d_k) 避免点积结果过大导致 softmax 梯度处于饱和区（梯度趋近于 0，产生梯度消失）。\n" +
                "3. 多头机制：将 Q、K、V 投影到 h 个不同的低维子空间，并行捕捉不同层次的语义关联，最终拼接（Concat）并线性变换输出。",
                801, "Transformer与自注意力机制"
            ));

            all.add(new QuestionItem(
                "RAG 检索增强生成架构核心流程与向量检索准确率优化方案",
                "1. RAG 核心三步：检索（Retrieval）从外部知识库检索相关文档分块 -> 增强（Augmentation）拼接 Prompt 上下文 -> 生成（Generation）由大模型生成答复。\n" +
                "2. 检索优化：混合检索（BM25 关键词稀疏检索 + Dense 向量稠密检索结合）、重排序（Cross-Encoder Rerank）、文档切块优化（Chunking 分块重叠）。\n" +
                "3. 上下文工程：Parent-Child 检索、知识图谱混合检索（GraphRAG）。",
                802, "RAG检索增强生成架构"
            ));

            all.add(new QuestionItem(
                "参数高效微调技术 LoRA 与 QLoRA 底层矩阵低秩分解原理",
                "1. LoRA 原理：冻结预训练大模型权重 W_0，在旁边并联两个低秩可训练矩阵 A 和 B（W = W_0 + B * A），大幅降低显存消耗与可调参数量（降至 1% 以下）。\n" +
                "2. 初始化：A 矩阵高斯分布初始化，B 矩阵置零初始化，确保初始阶段无附加偏差。\n" +
                "3. QLoRA 升级：引入 NF4（4-bit NormalFloat）量化技术、双重分块量化与分页内存（Paged Optimizers），使得单张消费级显卡（如 24G RTX 4090）能够微调百亿参数模型。",
                803, "LoRA与QLoRA高效微调"
            ));
        }

        // 4. 算法与数据结构 (LeetCode, 动态规划, 二叉树, 双指针)
        if (isMatchSource(source, "ALGORITHM")) {
            all.add(new QuestionItem(
                "经典高频算法：LRU（最近最少使用）缓存机制设计与 O(1) 实现原理",
                "1. 核心数据结构：哈希表（HashMap）+ 双向链表（Doubly Linked List）。\n" +
                "2. 读操作 get(key)：哈希表 O(1) 定位节点，若存在则将其移动至双向链表头部（表示最近被访问），返回 value。\n" +
                "3. 写操作 put(key, value)：若 key 存在则更新 value 并移至头部；若不存在则新建节点插入头部，若容量超标则删除尾部节点并在哈希表中移除对应 key。",
                403, "双指针与滑动窗口"
            ));

            all.add(new QuestionItem(
                "动态规划核心解题框架：状态转移方程、最优子结构与重叠子问题",
                "1. 三大要素：最优子结构（原问题的最优解包含子问题的最优解）、重叠子问题（递归过程重复计算相同子状态）、无后效性（未来状态与过去路径无关）。\n" +
                "2. 解题步骤：明确 base case -> 确定状态（变量）-> 确定选择（状态转移）-> 定义 dp 数组含义。\n" +
                "3. 空间优化：利用状态压缩（滚动数组或一维优化）将 O(N^2) 空间复杂度降低至 O(N)。",
                401, "背包与状态机DP"
            ));

            all.add(new QuestionItem(
                "二叉树遍历系列：前序、中序、后序与层序遍历非递归迭代实现",
                "1. 深度优先 DFS：借助显式栈（Stack）模拟函数调用栈。前序遍历先入右子节点再入左子节点；中序遍历一路压入左节点到底后弹出访问并转向右节点。\n" +
                "2. 广度优先 BFS（层序遍历）：借助队列（Queue）按层入队，每层循环队列当前大小（queue.size()）实现分层收集。\n" +
                "3. 莫里斯遍历（Morris Traversal）：利用叶子节点空闲指针构建线索二叉树，达到 O(1) 常数空间复杂度。",
                402, "二叉树遍历与递归"
            ));
        }

        // 截取上限
        if (limit > 0 && all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    private static boolean isMatchSource(String source, String target) {
        if (StringUtils.isBlank(source) || "ALL".equalsIgnoreCase(source)) {
            return true;
        }
        return source.equalsIgnoreCase(target);
    }
}
