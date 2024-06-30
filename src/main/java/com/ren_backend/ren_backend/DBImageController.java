package com.ren_backend.ren_backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBImageController {
    Connection conn;
    public DBImageController() throws SQLException {
        String jdbcUrl = "jdbc:" + System.getenv("IMAGE_DB_URL");
        String username = System.getenv("IMAGE_DB_USERNAME");
        String password = System.getenv("IMAGE_DB_PASSWORD");

        conn = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public void save(byte[] image) {
    }

    protected void finalize() throws SQLException {
        conn.close();
    }
}
