package com.promo.otp.handler;

import com.promo.otp.dao.OtpConfigDao;
import com.promo.otp.model.OtpConfig;
import com.promo.otp.util.AuthFilter;
import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class AdminConfigHandler implements HttpHandler {
    private final OtpConfigDao configDao = new OtpConfigDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        Claims claims;
        try {
            claims = AuthFilter.validateToken(authHeader);
            String role = claims.get("role", String.class);
            if (!"ADMIN".equals(role)) {
                sendResponse(exchange, 403, "Forbidden: admin only");
                return;
            }
        } catch (Exception e) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        try {
            Map<String, Integer> data = JsonUtil.fromJson(body, Map.class);
            int codeLength = data.get("codeLength");
            int ttlSeconds = data.get("ttlSeconds");
            OtpConfig cfg = new OtpConfig();
            cfg.setCodeLength(codeLength);
            cfg.setTtlSeconds(ttlSeconds);
            configDao.updateConfig(cfg);
            sendResponse(exchange, 200, "Config updated");
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(status, message.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }
}
