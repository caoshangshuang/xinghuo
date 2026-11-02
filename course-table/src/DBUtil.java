import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类(HikariCP 连接池版)。
 * 连接信息(URL/账号/密码)不写死在代码里,而是从 src/db.properties 读取:
 * - 密码不进公开仓库(db.properties 已被 .gitignore 忽略)
 * - 换机器/换密码只改配置文件,不动代码
 * 第一次使用时,请把 db.properties.example 复制为 db.properties 并填入自己的配置。
 */
public class DBUtil {

    /** 连接池:程序运行期间只创建一次,全局共享 */
    private static final HikariDataSource DS;

    static {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(10);      // 池里最多 10 条连接
        config.setMinimumIdle(2);           // 空闲时最少保留 2 条备用
        config.setConnectionTimeout(3000);  // 借连接最多等 3 秒(毫秒)
        DS = new HikariDataSource(config);  // 创建数据源 = 启动连接池
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "找不到 db.properties:请把 src/db.properties.example 复制为 src/db.properties 并填入你的数据库配置");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("读取 db.properties 失败", e);
        }
        return props;
    }

    // 工具类不需要创建对象,把构造方法私有化,禁止 new DBUtil()
    private DBUtil() {}

    /**
     * 从连接池借一条连接。
     * 方法签名和改造前完全一样,所以 CourseDao / CourseTable 一行都不用改。
     */
    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }
}
