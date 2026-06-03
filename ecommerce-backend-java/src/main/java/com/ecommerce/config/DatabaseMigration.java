package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try { jdbcTemplate.execute("ALTER TABLE users ADD COLUMN uid VARCHAR(3) UNIQUE"); } catch (Exception e) { /* exists */ }
        try { jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN shipping_company VARCHAR(50) DEFAULT NULL"); } catch (Exception e) { /* exists */ }
    }
}
