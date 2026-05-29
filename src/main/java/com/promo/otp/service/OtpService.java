package com.promo.otp.service;

import com.promo.otp.dao.OtpCodeDao;
import com.promo.otp.dao.OtpConfigDao;
import com.promo.otp.model.OtpCode;
import com.promo.otp.model.OtpConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class OtpService {
    private static final Logger logger = LogManager.getLogger(OtpService.class);
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();
    private final OtpConfigDao otpConfigDao = new OtpConfigDao();
    private final SecureRandom random = new SecureRandom();

    private final SmsService smsService = new SmsService();
    private final EmailService emailService = new EmailService();
    private final TelegramService telegramService = new TelegramService();
    private final FileService fileService = new FileService();

    public String generateAndSend(int userId, String operationId, String phone, String email) throws SQLException {
        OtpConfig config = otpConfigDao.getConfig();
        int length = config.getCodeLength();
        int ttl = config.getTtlSeconds();
        String code = generateNumericCode(length);
        Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(ttl));

        OtpCode otpCode = new OtpCode();
        otpCode.setUserId(userId);
        otpCode.setOperationId(operationId);
        otpCode.setCode(code);
        otpCode.setStatus("ACTIVE");
        otpCode.setExpiresAt(expiresAt);
        otpCode.setChannelSent("ALL");
        otpCodeDao.save(otpCode);

        // Отправки (все ошибки логгируются, но не бросаются)
        smsService.sendCode(phone, code);
        emailService.sendCode(email, code);
        telegramService.sendCode("User " + userId + " operation " + operationId + " code: " + code);
        fileService.saveCode(operationId, code);

        logger.info("OTP generated for user {} operation {}", userId, operationId);
        return code;
    }

    public boolean validateCode(int userId, String operationId, String enteredCode) throws SQLException {
        OtpCode stored = otpCodeDao.findActiveByUserAndOperation(userId, operationId);
        if (stored == null) {
            logger.warn("No active code for user {} operation {}", userId, operationId);
            return false;
        }
        if (stored.getExpiresAt().before(Timestamp.from(Instant.now()))) {
            otpCodeDao.updateStatus(stored.getId(), "EXPIRED");
            return false;
        }
        if (!stored.getCode().equals(enteredCode)) {
            return false;
        }
        otpCodeDao.updateStatus(stored.getId(), "USED");
        logger.info("Code validated for user {} operation {}", userId, operationId);
        return true;
    }

    private String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
