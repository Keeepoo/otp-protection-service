package com.promo.otp.dao;

import com.promo.otp.model.OtpConfig;
import com.promo.otp.util.DatabasePool;
import java.sql.*;

public class OtpConfigDao {
    public OtpConfig getConfig() throws SQLException {
        String sql = "SELECT code_length, ttl_seconds FROM otp_config WHERE id = 1";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                OtpConfig cfg = new OtpConfig();
                cfg.setId(1);
                cfg.setCodeLength(rs.getInt("code_length"));
                cfg.setTtlSeconds(rs.getInt("ttl_seconds"));
                return cfg;
            } else {
                OtpConfig def = new OtpConfig();
                def.setId(1);
                def.setCodeLength(6);
                def.setTtlSeconds(300);
                updateConfig(def);
                return def;
            }
        }
    }

    public void updateConfig(OtpConfig config) throws SQLException {
        String sql = "UPDATE otp_config SET code_length = ?, ttl_seconds = ?, updated_at = CURRENT_TIMESTAMP WHERE id = 1";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, config.getCodeLength());
            stmt.setInt(2, config.getTtlSeconds());
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                String insert = "INSERT INTO otp_config (id, code_length, ttl_seconds) VALUES (1, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setInt(1, config.getCodeLength());
                    ins.setInt(2, config.getTtlSeconds());
                    ins.executeUpdate();
                }
            }
        }
    }
}
