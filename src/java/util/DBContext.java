/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Admin
 */
public class DBContext {

    // Thông tin kết nối dùng chung cho cả constructor và main() test.
    // encrypt=true;trustServerCertificate=true: driver mssql-jdbc >=10 mặc định bật mã hóa;
    // SQL Server dev dùng chứng chỉ tự ký nên phải "tin" chứng chỉ, nếu không sẽ lỗi PKIX/SSL.
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=RapVietDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    private static DBContext instance = new DBContext();
    Connection connection;

    public static DBContext getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private DBContext(){
        try {
            if(connection == null || connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
         }catch (Exception e){
             connection = null;
         }
        }

    /**
     * Chạy riêng file này (chuột phải ▸ Run File / Shift+F6) để kiểm tra kết nối DB.
     * Khác với constructor (nuốt lỗi, gán null), main() in ra lỗi CỤ THỂ để biết
     * vì sao kết nối thất bại (sai instance/cổng, sai mật khẩu, thiếu driver, SSL...).
     */
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
