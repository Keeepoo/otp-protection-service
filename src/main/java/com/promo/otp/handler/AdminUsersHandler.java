package com.promo.otp.handler;

import com.promo.otp.dao.UserDao;
import com.promo.otp.model.User;
import com.promo.otp.util.AuthFilter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class AdminUsersHandler implements HttpHandler {
    private final UserDao userDao = new UserDao();

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

        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            try {
                List<User> users = userDao.findAllNonAdmin();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < users.size(); i++) {
                    User u = users.get(i);
                    json.append("{\"id\":").append(u.getId()).append(",\"login\":\"").append(u.getLogin()).append("\"}");
                    if (i < users.size() - 1) json.append(",");
                }
                json.append("]");
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.length());
                OutputStream os = exchange.getResponseBody();
                os.write(json.toString().getBytes());
                os.close();
            } catch (Exception e) {
                sendResponse(exchange, 500, "Error fetching users");
            }
        } else if ("DELETE".equals(method)) {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("id=")) {
                sendResponse(exchange, 400, "Missing user id");
                return;
            }
            try {
                int userId = Integer.parseInt(query.substring(3));
                userDao.deleteById(userId);
                sendResponse(exchange, 200, "User deleted");
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Invalid user id");
            } catch (Exception e) {
                sendResponse(exchange, 500, "Delete failed");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
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
