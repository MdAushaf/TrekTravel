package com.trektravel.servlet;

import com.trektravel.dao.BatchDAO;
import com.trektravel.dao.TrekDAO;
import com.trektravel.dao.TrekkerProfileDAO;
import com.trektravel.model.Batch;
import com.trektravel.model.Trek;
import com.trektravel.model.TrekkerProfile;
import com.trektravel.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Public discovery servlet handling landing page, search with dynamic filters, and trek details.
 */
@WebServlet(name = "PublicServlet", urlPatterns = {"", "/search", "/trek"})
public class PublicServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final TrekDAO trekDAO = new TrekDAO();
    private final BatchDAO batchDAO = new BatchDAO();
    private final TrekkerProfileDAO profileDAO = new TrekkerProfileDAO();

    @Override
    protected void handle(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException {

        if (isPost) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String path = req.getServletPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            handleHome(req, resp);
        } else if ("/search".equals(path)) {
            handleSearch(req, resp);
        } else if ("/trek".equals(path)) {
            handleTrekDetails(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleHome(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Trek> featuredTreks = trekDAO.getFeaturedTreks(6);
        req.setAttribute("featuredTreks", featuredTreks);
        show(req, resp, "index");
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String location = p(req, "location");
        if (location == null) location = p(req, "q"); // support query param 'q' as well
        String difficulty = p(req, "difficulty");
        int monthInt = intP(req, "month", 0);
        Integer month = (monthInt >= 1 && monthInt <= 12) ? monthInt : null;
        BigDecimal maxPrice = decP(req, "maxPrice", null);

        List<Trek> results = trekDAO.searchTreks(location, difficulty, month, maxPrice);
        req.setAttribute("treks", results);
        req.setAttribute("location", location);
        req.setAttribute("difficulty", difficulty);
        req.setAttribute("month", month);
        req.setAttribute("maxPrice", maxPrice);

        // Check if current user is an approved trekker (for strike-through price display)
        User u = currentUser(req);
        boolean isVerifiedTrekker = false;
        if (u != null && u.isTrekker()) {
            TrekkerProfile profile = profileDAO.findByUserId(u.getId());
            if (profile != null && profile.isApproved()) {
                isVerifiedTrekker = true;
            }
        }
        req.setAttribute("isVerifiedTrekker", isVerifiedTrekker);

        show(req, resp, "search");
    }

    private void handleTrekDetails(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int trekId = intP(req, "id", -1);
        if (trekId <= 0) {
            flashError(req, "Invalid trek identifier specified.");
            redirect(req, resp, "/search");
            return;
        }

        Trek trek = trekDAO.findApprovedById(trekId);
        if (trek == null) {
            flashError(req, "The requested trek was not found or is not approved for public viewing.");
            redirect(req, resp, "/search");
            return;
        }

        List<Batch> upcomingBatches = batchDAO.getFutureOpenBatchesByTrekId(trekId);

        User u = currentUser(req);
        TrekkerProfile profile = null;
        boolean hasProfile = false;
        boolean isVerifiedTrekker = false;

        if (u != null && u.isTrekker()) {
            profile = profileDAO.findByUserId(u.getId());
            if (profile != null) {
                hasProfile = true;
                isVerifiedTrekker = profile.isApproved();
            }
        }

        req.setAttribute("trek", trek);
        req.setAttribute("batches", upcomingBatches);
        req.setAttribute("trekkerProfile", profile);
        req.setAttribute("hasProfile", hasProfile);
        req.setAttribute("isVerifiedTrekker", isVerifiedTrekker);

        show(req, resp, "trek");
    }
}
