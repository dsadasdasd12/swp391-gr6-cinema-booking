/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.sql.Connection;
import java.sql.DriverManager;

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

    private static DBContext instance = new DBContext();
    Connection connection;

    public static DBContext getInstance() {
        return instance;
    }

    public Connection getConnection() {
        connect();
        return connection;
    }

    public DBContext() {
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
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("==> KET NOI THANH CONG!");
                System.out.println("    DBMS   : " + c.getMetaData().getDatabaseProductName()
                        + " " + c.getMetaData().getDatabaseProductVersion());
                System.out.println("    Catalog: " + c.getCatalog());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("==> THIEU DRIVER SQL Server tren classpath: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("==> KET NOI THAT BAI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}