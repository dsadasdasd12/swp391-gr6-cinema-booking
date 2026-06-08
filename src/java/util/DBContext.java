package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class DBContext {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=RapVietDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

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
        synchronized (pool) {
            while (!pool.isEmpty()) {
                Connection conn = pool.remove(pool.size() - 1);
                try {
                    // Quick check to see if the connection is still alive
                    if (conn != null && !conn.isClosed() && conn.isValid(1)) {
                        return createConnectionProxy(conn);
                    }
                } catch (Exception e) {
                    try {
                        if (conn != null) conn.close();
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
        }
        
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