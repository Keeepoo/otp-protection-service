package com.promo.otp.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileService {
    private static final Logger logger = LogManager.getLogger(FileService.class);
    private final Path filePath = Paths.get("codes.log");

    public void saveCode(String operationId, String code) {
        String line = String.format("Operation: %s, Code: %s, Time: %s%n", operationId, code, java.time.LocalDateTime.now());
        try {
            Files.writeString(filePath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("Code saved to file for operation {}", operationId);
        } catch (IOException e) {
            logger.error("Failed to write code to file", e);
        }
    }
}
