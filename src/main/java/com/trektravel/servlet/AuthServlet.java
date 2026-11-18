package com.trektravel.servlet;

import com.trektravel.dao.TrekkerProfileDAO;
import com.trektravel.dao.UserDAO;
import com.trektravel.model.TrekkerProfile;
import com.trektravel.model.User;
import com.trektravel.util.PasswordUtil;
import com.trektravel.util.Validate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication servlet handling login, registration, and logout workflows.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final TrekkerProfileDAO profileDAO = new TrekkerProfileDAO();

    @Override
    protected void handle(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/login".equals(path)) {
            if (isPost) {
                processLogin(req, resp);
            } else {
                showLoginForm(req, resp);
            }
        } else if ("/register".equals(path)) {
            if (isPost) {
                processRegister(req, resp);
            } else {
                showRegisterForm(req, resp);
            }
        } else if ("/logout".equals(path)) {
            if (!methodOk(req, resp, "POST")) {
                return;
            }
            processLogout(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showLoginForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            redirectByRole(user, req, resp);
            return;
        }
        show(req, resp, "login");
    }

    private void processLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = p(req, "email");
        String password = p(req, "password");

        if (email == null || password == null) {
            req.setAttribute("errorMessage", "Email and password are required.");
            req.setAttribute("email", email);
            show(req, resp, "login");
            return;
        }

        User user = userDAO.findByEmail(email);
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash(), user.getSalt())) {
            req.setAttribute("errorMessage", "Invalid email or password. Please try again.");
            req.setAttribute("email", email);
            show(req, resp, "login");
            return;
        }

        if (user.isBlocked()) {
            req.setAttribute("errorMessage", "Your account has been suspended. Please contact support.");
            show(req, resp, "login");
            return;
        }

        // Establish clean, fresh session on login
        req.getSession().invalidate();
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("user", user);

        // Preload trekker profile into session if applicable
        if (user.isTrekker()) {
            TrekkerProfile profile = profileDAO.findByUserId(user.getId());
            session.setAttribute("currentProfile", profile);
        }

        flashSuccess(req, "Welcome back, " + user.getName() + "!");
        redirectByRole(user, req, resp);
    }

    private void showRegisterForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            redirectByRole(user, req, resp);
            return;
        }
        show(req, resp, "register");
    }

    private void processRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = p(req, "name");
        String email = p(req, "email");
        String phone = p(req, "phone");
        String role = p(req, "role");
        String password = p(req, "password");
        String confirmPassword = p(req, "confirmPassword");

        req.setAttribute("name", name);
        req.setAttribute("email", email);
        req.setAttribute("phone", phone);
        req.setAttribute("role", role);

        if (!Validate.nonEmpty(name) || !Validate.nonEmpty(email) || !Validate.nonEmpty(phone)
                || !Validate.nonEmpty(role) || !Validate.nonEmpty(password)) {
            req.setAttribute("errorMessage", "All registration fields are required.");
            show(req, resp, "register");
            return;
        }

        if (!Validate.email(email)) {
            req.setAttribute("errorMessage", "Please provide a valid email address.");
            show(req, resp, "register");
            return;
        }

        if (!Validate.phone(phone)) {
            req.setAttribute("errorMessage", "Please provide a valid 10-15 digit phone number.");
            show(req, resp, "register");
            return;
        }

        // Role restriction: TREKKER or ORGANIZER only (no public ADMIN registration)
        if (!"TREKKER".equalsIgnoreCase(role) && !"ORGANIZER".equalsIgnoreCase(role)) {
            req.setAttribute("errorMessage", "Invalid account role selected.");
            show(req, resp, "register");
            return;
        }

        if (!Validate.password(password)) {
            req.setAttribute("errorMessage", "Password must be at least 8 characters long and contain both letters and numbers.");
            show(req, resp, "register");
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Passwords do not match.");
            show(req, resp, "register");
            return;
        }

        if (userDAO.emailExists(email)) {
            req.setAttribute("errorMessage", "An account with this email already exists. Please log in.");
            show(req, resp, "register");
            return;
        }

        String hashedPassword = PasswordUtil.hash(password);

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setRole(role.toUpperCase());
        newUser.setPasswordHash(hashedPassword);
        newUser.setSalt("");
        newUser.setStatus("ACTIVE");

        int userId = userDAO.createUser(newUser);
        if (userId > 0) {
            flashSuccess(req, "Account registered successfully! Please log in.");
            redirect(req, resp, "/login");
        } else {
            req.setAttribute("errorMessage", "Unable to create account. Please try again.");
            show(req, resp, "register");
        }
    }

    private void processLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        HttpSession freshSession = req.getSession(true);
        freshSession.setAttribute("successMessage", "You have been logged out successfully.");
        redirect(req, resp, "/login");
    }

    private void redirectByRole(User user, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (user.isAdmin()) {
            redirect(req, resp, "/admin/dashboard");
        } else if (user.isOrganizer()) {
            redirect(req, resp, "/organizer/treks");
        } else {
            redirect(req, resp, "/trekker/profile");
        }
    }
}
