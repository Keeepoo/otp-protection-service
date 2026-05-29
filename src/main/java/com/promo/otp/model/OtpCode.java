package com.promo.otp.model;

import java.sql.Timestamp;

public class OtpCode {
    private int id;
    private int userId;
    private String operationId;
    private String code;
    private String status;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private String channelSent;

    public OtpCode() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public String getChannelSent() { return channelSent; }
    public void setChannelSent(String channelSent) { this.channelSent = channelSent; }
}