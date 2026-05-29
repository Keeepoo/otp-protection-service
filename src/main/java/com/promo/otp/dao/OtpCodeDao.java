package com.promo.otp.dao;

import com.promo.otp.model.OtpCode;
import com.promo.otp.util.DatabasePool;
import java.sql.*;

public class OtpCodeDao {
    public void save(OtpCode code) throws SQLException {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code, status, expires_at, channel_sent) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, code.getUserId());
            stmt.setString(2, code.getOperationId());
            stmt.setString(3, code.getCode());
            stmt.setString(4, code.getStatus());
            stmt.setTimestamp(5, code.getExpiresAt());
            stmt.setString(6, code.getChannelSent());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) code.setId(rs.getInt(1));
        }
    }

    public OtpCode findActiveByUserAndOperation(int userId, String operationId) throws SQLException {
        String sql = "SELECT id, user_id, operation_id, code, status, expires_at FROM otp_codes WHERE user_id = ? AND operation_id = ? AND status = 'ACTIVE' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, operationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                OtpCode code = new OtpCode();
                code.setId(rs.getInt("id"));
                code.setUserId(rs.getInt("user_id"));
                code.setOperationId(rs.getString("operation_id"));
                code.setCode(rs.getString("code"));
                code.setStatus(rs.getString("status"));
                code.setExpiresAt(rs.getTimestamp("expires_at"));
                return code;
            }
            return null;
        }
    }

    public void updateStatus(int codeId, String status) throws SQLException {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, codeId);
            stmt.executeUpdate();
        }
    }

    public int markExpiredCodes() throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < NOW()";
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        }
    }
}
