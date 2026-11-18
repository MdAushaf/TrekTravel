package com.trektravel.filter;

import com.trektravel.dao.UserDAO;
import com.trektravel.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Filter enforcing:
 * 1. UTF-8 request and response encoding
 * 2. Standard security headers (no-cache, nosniff, frame protection)
 * 3. CSRF token validation on every state-altering POST request
 * 4. Role-Based Access Control (RBAC) on /trekker/*, /organizer/*, and /admin/*
 * 5. Real-time DB status check to instantly invalidate BLOCKED users
 */
@WebFilter(filterName = "SecurityFilter", urlPatterns = {"/*"})
public class SecurityFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 1. UTF-8 Character Encoding
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 2. Security Headers
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        resp.setHeader("X-XSS-Protection", "1; mode=block");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        // Skip static resources (CSS, JS, images, favicon)
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // Ensure session exists and has a CSRF token
        HttpSession session = req.getSession(true);
        String sessionCsrf = (String) session.getAttribute("_csrf");
        if (sessionCsrf == null) {
            sessionCsrf = (String) session.getAttribute("csrf");
        }
        if (sessionCsrf == null) {
            sessionCsrf = UUID.randomUUID().toString();
            session.setAttribute("_csrf", sessionCsrf);
            session.setAttribute("csrf", sessionCsrf);
        }

        // 3. CSRF Validation on POST requests
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String requestCsrf = req.getParameter("_csrf");
            if (requestCsrf == null || requestCsrf.isEmpty()) {
                requestCsrf = req.getParameter("csrf");
            }
            if (requestCsrf == null || requestCsrf.isEmpty()) {
                requestCsrf = req.getHeader("X-CSRF-Token");
            }

            boolean csrfValid = false;
            if (requestCsrf != null && sessionCsrf != null) {
                csrfValid = MessageDigest.isEqual(
                        requestCsrf.trim().getBytes(StandardCharsets.UTF_8),
                        sessionCsrf.trim().getBytes(StandardCharsets.UTF_8)
                );
            }

            if (!csrfValid) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                return;
            }
        }

        // 4. Role-Based Access Control (RBAC) & DB Status Verification
        boolean isTrekkerPath = path.startsWith("/trekker/");
        boolean isOrganizerPath = path.startsWith("/organizer/");
        boolean isAdminPath = path.startsWith("/admin/");

        if (isTrekkerPath || isOrganizerPath || isAdminPath) {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser == null) {
                currentUser = (User) session.getAttribute("user");
            }
            if (currentUser == null) {
                session.setAttribute("errorMessage", "Session expired or authentication required. Please log in.");
                session.setAttribute("flash_error", "Session expired or authentication required. Please log in.");
                resp.sendRedirect(contextPath + "/login");
                return;
            }

            // Real-time re-check against DB to ensure user is still ACTIVE
            User freshUser = userDAO.findById(currentUser.getId());
            if (freshUser == null || freshUser.isBlocked()) {
                session.invalidate();
                HttpSession freshSession = req.getSession(true);
                freshSession.setAttribute("errorMessage", "Your account has been suspended. Please contact support.");
                freshSession.setAttribute("flash_error", "Your account has been suspended. Please contact support.");
                resp.sendRedirect(contextPath + "/login");
                return;
            }
            // Update session user in case details changed
            session.setAttribute("currentUser", freshUser);
            session.setAttribute("user", freshUser);

            String role = freshUser.getRole();
            if (isTrekkerPath && !"TREKKER".equalsIgnoreCase(role)) {
                sendForbidden(req, resp, contextPath, role);
                return;
            }
            if (isOrganizerPath && !"ORGANIZER".equalsIgnoreCase(role)) {
                sendForbidden(req, resp, contextPath, role);
                return;
            }
            if (isAdminPath && !"ADMIN".equalsIgnoreCase(role)) {
                sendForbidden(req, resp, contextPath, role);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletRequest req, HttpServletResponse resp, String contextPath, String role)
            throws IOException {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not have permissions for this section.");
    }

    @Override
    public void destroy() {}
}
