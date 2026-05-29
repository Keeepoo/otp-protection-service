package com.promo.otp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

public class AuthFilter {
    public static Claims validateToken(String authHeader) throws JwtException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return JwtUtil.parseToken(token);
    }
}
