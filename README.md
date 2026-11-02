# xinghuo

星火实验室 2026 纳新任务 #30:**后端 · 课程表入库(MySQL + JDBC)**

河北科技大学 计算机2502 付桂豪

## 项目简介

STAGE 01 控制台课程表(Java 内存版)的数据库升级:课程数据从 `ArrayList` 迁移到 MySQL,使用 JDBC 完成增删查改,新增统计、连表查询与事务调课,并加入连接池(HikariCP)与索引优化。

## 功能菜单

```
1. 添加课程          2. 查看所有课程
3. 查找课程(模糊)    4. 按星期统计课程数(GROUP BY)
5. 连表查询展示(JOIN) 6. 调课(事务)
7. 退出
```

## 任务要求对照

| 要求 | 实现位置 |
|---|---|
| ① 设计创建数据库与表(拆时间段/教室表 + 外键) | `sql/init.sql`:xinghuo 库,course / time_slot / classroom 三表,外键关联 |
| ② 用 JDBC 重写添加/查看全部/按名称查找 | `src/CourseDao.java` 的 add / findAll / findByName(PreparedStatement 参数化防注入) |
| ③ 两条统计 SQL 在程序中展示 | `countByWeekday()`(GROUP BY)、`joinQuery()`(三表 JOIN),菜单 4、5 |
| ④ 调课操作在事务里 | `swapTime()`:`setAutoCommit(false)` → 两条 UPDATE → commit / 异常 rollback |
| ⑤ 提交表结构截图与查询结果截图 | 提交页附 9 张运行截图 |

加分项:

- **连接池**:`src/DBUtil.java` 使用 HikariCP 5.1.0(池上限 10、最少空闲 2),`CourseDao` 层零改动,体现分层可维护性
- **索引**:`CREATE INDEX idx_course_name ON course(name)` 加速"按名称查找"(B+ 树,O(log n) vs 全表 O(n));注意 `LIKE '%x%'` 因最左前缀原则无法走索引,等值/前缀查询生效(EXPLAIN 验证:type ALL → ref,rows 5 → 1)

## 目录结构

```
course-table/
├── lib/                    # mysql-connector-java-8.0.29 / HikariCP / slf4j-api / slf4j-simple
├── sql/init.sql            # 建库建表 + 初始数据 + 索引(一键初始化)
└── src/
    ├── Course.java         # 实体类:一行记录 ↔ 一个对象
    ├── DBUtil.java         # 连接工具(HikariCP 连接池)
    ├── CourseDao.java      # 数据访问层:所有 SQL 集中于此
    ├── CourseTable.java    # 界面层:7 项菜单
    └── TestCourse.java     # 实体类冒烟测试
```

## 环境与运行

- JDK 26(Oracle OpenJDK 26.0.2)、IntelliJ IDEA 2026.2(Ultimate)
- MySQL 8.0.29(端口 3306,服务名 MySQL80,utf8mb4)

运行步骤:

1. 初始化数据库:`mysql -uroot -p < course-table/sql/init.sql`
2. 配置连接:把 `src/db.properties.example` 复制为 `src/db.properties`,填入你自己的 MySQL 账号密码(该文件已被 .gitignore 忽略,不会进公开仓库)
3. IDEA 打开项目,模块依赖已配好(4 个 jar 均在 lib/)
4. 运行 `CourseTable.main()`,按菜单操作

连接参数集中在 `src/db.properties`(URL / 账号 / 密码),换机器/换密码只改这一处,不动代码。

## 设计说明

- **拆表 + 外键**:时间段/教室拆为独立表,course 存数字 id 引用,消除冗余、修改一处生效、外键约束防脏数据
- **分层**:界面(CourseTable)→ 数据访问(CourseDao)→ 连接(DBUtil),SQL 集中、可复用、易替换
- **事务**:调课两条 UPDATE 原子执行,程序崩溃时连接关闭自动回滚
