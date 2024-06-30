package com.ren_backend.ren_backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBImageController {
    Connection conn;
    String tableName;

    public DBImageController() throws SQLException {
        String jdbcUrl = "jdbc:" + System.getenv("IMAGE_DB_URL");
        String username = System.getenv("IMAGE_DB_USERNAME");
        String password = System.getenv("IMAGE_DB_PASSWORD");

        tableName = System.getenv("IMAGE_DB_TABLENAME");

        conn = DriverManager.getConnection(jdbcUrl, username, password);
        System.out.println("Successfully connected to database " + conn);
    }

    public void save(byte[] image) throws SQLException {
        System.out.println("Started saving into " + conn);
        String insertQuery = "INSERT INTO " + tableName + " (image) VALUES (?)";
        System.out.println("Query is: " + insertQuery);
        PreparedStatement stmt = conn.prepareStatement(insertQuery);

        stmt.setBytes(1, image);
        System.out.println("Current statement is: " + stmt);
        stmt.executeUpdate();
        stmt.close();
    }

    protected void finalize() throws SQLException {
        conn.close();
    }
}
