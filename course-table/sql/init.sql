-- ============================================================
-- 星火实验室任务#30:课程表入库 数据库初始化脚本
-- 使用方法:mysql -uroot -p < init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS xinghuo DEFAULT CHARSET utf8mb4;
USE xinghuo;

-- 时间段表:星期 + 节次
CREATE TABLE time_slot (
    id      INT          NOT NULL AUTO_INCREMENT,
    weekday VARCHAR(10)  NOT NULL,
    period  VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 教室表:name 唯一,防止重复教室
CREATE TABLE classroom (
    id   INT         NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 课程表:外键关联时间段表和教室表(拆表消除冗余)
CREATE TABLE course (
    id      INT         NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50) NOT NULL,
    time_id INT         NOT NULL,
    room_id INT         NOT NULL,
    PRIMARY KEY (id),
    KEY fk_course_time (time_id),
    KEY fk_course_room (room_id),
    KEY idx_course_name (name),
    CONSTRAINT fk_course_room FOREIGN KEY (room_id) REFERENCES classroom (id),
    CONSTRAINT fk_course_time FOREIGN KEY (time_id) REFERENCES time_slot (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 初始数据
INSERT INTO time_slot (weekday, period) VALUES ('Monday', '1-2'), ('Tuesday', '3-4');
INSERT INTO classroom (name) VALUES ('A101'), ('B202');
INSERT INTO course (name, time_id, room_id)
VALUES ('高等数学', 1, 1),
       ('大学英语', 2, 2),
       ('线性代数', 1, 2);

-- 索引:加速程序的"按名称查找"高频查询(B+ 树,O(log n))
-- 注意:LIKE '%x%' 因最左前缀原则无法走该索引
CREATE INDEX idx_course_name ON course (name);

-- 验证(可选):
-- EXPLAIN SELECT * FROM course WHERE name = '高等数学';  -- type=ref, key=idx_course_name
-- EXPLAIN SELECT * FROM course WHERE name LIKE '%数学%'; -- type=ALL,索引失效
