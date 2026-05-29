package com.promo.otp.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TelegramService {
    private static final Logger logger = LogManager.getLogger(TelegramService.class);
    // ЗАМЕНИТЕ НА СВОИ ДАННЫЕ:
    private final String botToken = "8053930512:AAE3VOVQ7byJZSlwyeOycJAax7ZDF5fnVWU";
    private final String chatId = "815326385";
    private final String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

    public void sendCode(String messageText) {
        String text = URLEncoder.encode(messageText, StandardCharsets.UTF_8);
        String url = telegramApiUrl + "?chat_id=" + chatId + "&text=" + text;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Telegram message sent");
            } else {
                logger.error("Telegram API error: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Telegram send error", e);
            Thread.currentThread().interrupt();
        }
    }
}