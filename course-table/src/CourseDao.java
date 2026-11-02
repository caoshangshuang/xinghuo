import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程数据访问类:所有和 course 表打交道的 SQL 都集中在这里。
 * 界面层(CourseTable)不写 SQL,只调用这里的方法 —— 这就是"分层"思想。
 */
public class CourseDao {

    /** 添加课程:? 是占位符,配合 setString/setInt 使用,防止 SQL 注入 */
    public void add(String name, int timeId, int roomId) throws SQLException {
        String sql = "INSERT INTO course (name, time_id, room_id) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, timeId);
            ps.setInt(3, roomId);
            ps.executeUpdate();
        }
    }

    /** 查看全部课程 */
    public List<Course> findAll() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Course(rs.getInt("id"), rs.getString("name"),
                        rs.getInt("time_id"), rs.getInt("room_id")));
            }
        }
        return list;
    }

    /** 按名称模糊查找:% 通配符拼在占位符参数里 */
    public List<Course> findByName(String keyword) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE name LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Course(rs.getInt("id"), rs.getString("name"),
                            rs.getInt("time_id"), rs.getInt("room_id")));
                }
            }
        }
        return list;
    }

    /** 统计 SQL ①:GROUP BY 按星期统计课程数 */
    public List<StatRow> countByWeekday() throws SQLException {
        List<StatRow> rows = new ArrayList<>();
        String sql = "SELECT t.weekday, COUNT(*) FROM course c " +
                     "JOIN time_slot t ON c.time_id = t.id " +
                     "GROUP BY t.weekday";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new StatRow(rs.getString(1), rs.getInt(2)));
            }
        }
        return rows;
    }

    /** 统计 SQL ②:三表 JOIN 连表查询 */
    public List<JoinRow> joinQuery() throws SQLException {
        List<JoinRow> rows = new ArrayList<>();
        String sql = "SELECT c.name, t.weekday, t.period, r.name " +
                     "FROM course c " +
                     "JOIN time_slot t ON c.time_id = t.id " +
                     "JOIN classroom r ON c.room_id = r.id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new JoinRow(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)));
            }
        }
        return rows;
    }

    /** 添加课程时展示的可选时间段列表 */
    public List<String> listTimeSlots() throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = "SELECT id, weekday, period FROM time_slot";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lines.add(rs.getInt(1) + ". " + rs.getString(2) + " " + rs.getString(3));
            }
        }
        return lines;
    }

    /** 添加课程时展示的可选教室列表 */
    public List<String> listClassrooms() throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = "SELECT id, name FROM classroom";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lines.add(rs.getInt(1) + ". " + rs.getString(2));
            }
        }
        return lines;
    }

    /**
     * 调课:把两门课的上课时间互换。
     * 两条 UPDATE 必须"要么都成功、要么都失败",所以放在同一个事务里。
     */
    public boolean swapTime(String name1, String name2) throws SQLException {
        if (name1.equals(name2)) {
            return false;
        }
        String sql = "UPDATE course SET time_id = ? WHERE name = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);              // ① 开始事务
            try {
                Integer time1 = findTimeId(conn, name1);
                Integer time2 = findTimeId(conn, name2);
                if (time1 == null || time2 == null) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, time2);
                    ps.setString(2, name1);
                    ps.executeUpdate();
                    ps.setInt(1, time1);
                    ps.setString(2, name2);
                    ps.executeUpdate();
                }
                conn.commit();                      // ② 两条都成功 → 提交
                return true;
            } catch (SQLException e) {
                conn.rollback();                    // ③ 任何一步出错 → 回滚
                throw e;
            }
        }
    }

    /** 查一门课的 time_id;注意用传入的连接,保证和外层事务是同一个连接 */
    private Integer findTimeId(Connection conn, String name) throws SQLException {
        String sql = "SELECT time_id FROM course WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    /** 统计结果行:星期 + 课程数 */
    public static class StatRow {
        private final String weekday;
        private final int count;

        public StatRow(String weekday, int count) {
            this.weekday = weekday;
            this.count = count;
        }

        public String getWeekday() { return weekday; }
        public int getCount() { return count; }
    }

    /** 连表查询结果行:课程名 + 星期 + 节次 + 教室 */
    public static class JoinRow {
        private final String name;
        private final String weekday;
        private final String period;
        private final String roomName;

        public JoinRow(String name, String weekday, String period, String roomName) {
            this.name = name;
            this.weekday = weekday;
            this.period = period;
            this.roomName = roomName;
        }

        public String getName() { return name; }
        public String getWeekday() { return weekday; }
        public String getPeriod() { return period; }
        public String getRoomName() { return roomName; }
    }
}
