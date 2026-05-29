package com.promo.otp;

import com.promo.otp.handler.*;
import com.promo.otp.service.*;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        UserService userService = new UserService();
        OtpService otpService = new OtpService();

        server.createContext("/api/register", new RegisterHandler(userService));
        server.createContext("/api/login", new LoginHandler(userService));
        server.createContext("/api/user/generate", new GenerateOtpHandler(otpService, userService));
        server.createContext("/api/user/validate", new ValidateOtpHandler(otpService, userService));
        server.createContext("/api/admin/config", new AdminConfigHandler());
        server.createContext("/api/admin/users", new AdminUsersHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        logger.info("Server started on port 8080");

        ExpiredCodesScheduler scheduler = new ExpiredCodesScheduler();
        scheduler.start();
    }
}
