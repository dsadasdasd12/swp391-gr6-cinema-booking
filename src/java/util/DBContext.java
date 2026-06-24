package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LONG
 */
public class DBContext {

        // Thông tin kết nối: đọc từ biến môi trường nếu có, ngược lại dùng giá trị mặc định.
        private static final String URL;
        private static final String USER;
        private static final String PASSWORD;

        static {
        String envUrl = System.getenv("DB_URL");
        URL = (envUrl != null && !envUrl.isBlank()) ? envUrl
            : "jdbc:sqlserver://localhost:1433;databaseName=RapVietDB;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true";
        String envUser = System.getenv("DB_USER");
        USER = (envUser != null) ? envUser : "sa";
        String envPass = System.getenv("DB_PASSWORD");
        PASSWORD = (envPass != null) ? envPass : "123"; // default to 123 for local environment
        }

    private static final DBContext instance = new DBContext();
    
    // Dynamic connection pool to reuse active physical connections
    private static final List<Connection> pool = new ArrayList<>();

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DBContext getInstance() {
        return instance;
    }

    public DBContext() {
    }

    public Connection getConnection() {
        connect();
        return connection;
    }

    private DBContext() {
        connect();
    }

    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (Exception e) {
            connection = null;
            System.err.println("[DBContext] Kết nối thất bại: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Đang thử kết nối: " + URL);
        try {
            Connection physicalConn = DriverManager.getConnection(URL, USER, PASSWORD);
            return createConnectionProxy(physicalConn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Dynamic proxy to intercept close() calls and return connections back to the pool
    private Connection createConnectionProxy(final Connection physicalConn) {
        return (Connection) Proxy.newProxyInstance(
            DBContext.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    synchronized (pool) {
                        // Recycle the connection instead of physically closing it
                        if (!physicalConn.isClosed() && pool.size() < 15) {
                            pool.add(physicalConn);
                            return null;
                        }
                    }
                }
                return method.invoke(physicalConn, args);
            }
        );
    }
}
