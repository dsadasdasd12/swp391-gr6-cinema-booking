package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBContext {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=RapVietDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    private static final DBContext instance = new DBContext();

    public static DBContext getInstance() {
        return instance;
    }

    public DBContext() {
    }

    public Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}