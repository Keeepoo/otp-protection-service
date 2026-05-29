package com.promo.otp.handler;

import com.promo.otp.service.UserService;
import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class RegisterHandler implements HttpHandler {
    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> data = JsonUtil.fromJson(body, Map.class);
            String login = data.get("login");
            String password = data.get("password");
            String role = data.getOrDefault("role", "USER");
            userService.register(login, password, role);
            sendResponse(exchange, 201, "User registered");
        } catch (Exception e) {
            sendResponse(exchange, 400, e.getMessage());
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
