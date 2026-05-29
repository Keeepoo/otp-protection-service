package com.promo.otp.service;

import com.promo.otp.dao.OtpCodeDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiredCodesScheduler {
    private static final Logger logger = LogManager.getLogger(ExpiredCodesScheduler.class);
    private final OtpCodeDao otpCodeDao = new OtpCodeDao();

    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int updated = otpCodeDao.markExpiredCodes();
                if (updated > 0) {
                    logger.info("Marked {} codes as EXPIRED", updated);
                }
            } catch (SQLException e) {
                logger.error("Error expiring codes", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
}
