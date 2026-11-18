package com.trektravel.util;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC connections to MySQL and loads configuration from db.properties on the classpath.
 */
public class DBUtil {

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String uploadDir;

    static {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("WARNING: db.properties not found on classpath, using defaults.");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load db.properties: " + e.getMessage());
        }

        dbUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/trektravel?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        dbUser = props.getProperty("db.user", "root");
        dbPassword = props.getProperty("db.password", "");

        String configuredUpload = props.getProperty("upload.dir", "").trim();
        if (configuredUpload.isEmpty()) {
            uploadDir = System.getProperty("user.home") + File.separator + "trektravel_uploads";
        } else {
            uploadDir = configuredUpload;
        }

        // Ensure upload directory exists
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Could not create upload directory: " + e.getMessage());
        }

        // Load MySQL Connector/J driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: com.mysql.cj.jdbc.Driver not found! Ensure mysql-connector-j-8.x.jar is in WEB-INF/lib");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static String getUploadDir() {
        return uploadDir;
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
