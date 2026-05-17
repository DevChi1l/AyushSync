package org.example.controller;

// TestController.java


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> result = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            result.put("status", "SUCCESS");
            result.put("database_url", metaData.getURL());
            result.put("database_name", connection.getCatalog());
            result.put("username", metaData.getUserName());
            result.put("driver_name", metaData.getDriverName());
            result.put("driver_version", metaData.getDriverVersion());
            result.put("connection_valid", connection.isValid(5));

        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("error_class", e.getClass().getSimpleName());
        }

        return result;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Application is running");
        return status;
    }
}