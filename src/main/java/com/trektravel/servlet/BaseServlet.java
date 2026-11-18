package com.trektravel.servlet;

import com.trektravel.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Base abstract servlet providing unified request dispatching, flash messaging,
 * view forwarding, redirects, method guards, and parameter parsers.
 */
public abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, true);
    }

    /**
     * Unified entry point for processing HTTP requests.
     *
     * @param req    the HttpServletRequest
     * @param resp   the HttpServletResponse
     * @param isPost true if POST request, false if GET
     */
    protected abstract void handle(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException;

    /**
     * Stores a flash message in the session to be displayed once on the next page view.
     */
    protected void flash(HttpServletRequest req, String key, String message) {
        req.getSession(true).setAttribute(key, message);
    }

    protected void flashSuccess(HttpServletRequest req, String message) {
        flash(req, "successMessage", message);
        flash(req, "flash_success", message);
    }

    protected void flashError(HttpServletRequest req, String message) {
        flash(req, "errorMessage", message);
        flash(req, "flash_error", message);
    }

    /**
     * Forwards request internally to a JSP view under /WEB-INF/views/<viewName>.jsp.
     */
    protected void show(HttpServletRequest req, HttpServletResponse resp, String viewName)
            throws ServletException, IOException {
        String path = "/WEB-INF/views/" + viewName + ".jsp";
        req.getRequestDispatcher(path).forward(req, resp);
    }

    /**
     * Issues an HTTP 302 redirect relative to the application's context path.
     */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /**
     * Enforces the required HTTP method (e.g. GET or POST).
     * Sends HTTP 405 Method Not Allowed if mismatched.
     */
    protected boolean methodOk(HttpServletRequest req, HttpServletResponse resp, String allowedMethod)
            throws IOException {
        if (!req.getMethod().equalsIgnoreCase(allowedMethod)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP " + req.getMethod() + " is not permitted for this URL.");
            return false;
        }
        return true;
    }

    /**
     * Safe parameter reader: trims whitespace and returns null if empty.
     */
    protected String p(HttpServletRequest req, String paramName) {
        String val = req.getParameter(paramName);
        if (val == null) return null;
        val = val.trim();
        return val.isEmpty() ? null : val;
    }

    /**
     * Parses integer parameter with a fallback default.
     */
    protected int intP(HttpServletRequest req, String paramName, int defaultValue) {
        String val = p(req, paramName);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses BigDecimal parameter with a fallback default.
     */
    protected BigDecimal decP(HttpServletRequest req, String paramName, BigDecimal defaultValue) {
        String val = p(req, paramName);
        if (val == null) return defaultValue;
        try {
            return new BigDecimal(val);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the currently authenticated User from session, or null if anonymous.
     */
    protected User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        User u = (User) session.getAttribute("currentUser");
        if (u == null) {
            u = (User) session.getAttribute("user");
        }
        return u;
    }

    protected User user(HttpServletRequest req) {
        return currentUser(req);
    }
}
