package com.promo.otp.service;

import com.promo.otp.dao.UserDao;
import com.promo.otp.model.User;
import com.promo.otp.util.JwtUtil;
import com.promo.otp.util.PasswordUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDao userDao = new UserDao();

    public void register(String login, String password, String role) throws SQLException {
        if (userDao.findByLogin(login) != null) {
            throw new RuntimeException("Login already exists");
        }
        if ("ADMIN".equalsIgnoreCase(role) && userDao.isAdminExists()) {
            throw new RuntimeException("Admin already exists, cannot register another admin");
        }
        String hash = PasswordUtil.hash(password);
        User user = new User(login, hash, role.toUpperCase());
        userDao.save(user);
        logger.info("Registered user {} with role {}", login, role);
    }

    public String login(String login, String password) throws SQLException {
        User user = userDao.findByLogin(login);
        if (user == null || !PasswordUtil.check(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = JwtUtil.generateToken(user.getLogin(), user.getRole());
        logger.info("User {} logged in", login);
        return token;
    }

    public User findByLogin(String login) throws SQLException {
        return userDao.findByLogin(login);
    }

    public List<User> getAllNonAdmin() throws SQLException {
        return userDao.findAllNonAdmin();
    }

    public void deleteUser(int userId) throws SQLException {
        userDao.deleteById(userId);
        logger.info("Deleted user {}", userId);
    }
}
