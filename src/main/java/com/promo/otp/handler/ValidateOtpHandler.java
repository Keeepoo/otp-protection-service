package com.promo.otp.handler;

import com.promo.otp.model.User;
import com.promo.otp.service.OtpService;
import com.promo.otp.service.UserService;
import com.promo.otp.util.AuthFilter;
import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ValidateOtpHandler implements HttpHandler {
    private final OtpService otpService;
    private final UserService userService;

    public ValidateOtpHandler(OtpService otpService, UserService userService) {
        this.otpService = otpService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        Claims claims;
        try {
            claims = AuthFilter.validateToken(authHeader);
        } catch (Exception e) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }
        String login = claims.getSubject();
        String body = new String(exchange.getRequestBody().readAllBytes());
        try {
            Map<String, String> data = JsonUtil.fromJson(body, Map.class);
            String operationId = data.get("operationId");
            String code = data.get("code");

            User user = userService.findByLogin(login);
            if (user == null) {
                sendResponse(exchange, 404, "User not found");
                return;
            }
            boolean valid = otpService.validateCode(user.getId(), operationId, code);
            String response = valid ? "Code valid" : "Code invalid or expired";
            sendResponse(exchange, valid ? 200 : 400, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Validation error: " + e.getMessage());
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