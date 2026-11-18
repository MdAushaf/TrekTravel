package com.trektravel.servlet;

import com.trektravel.dao.BookingDAO;
import com.trektravel.dao.TrekDAO;
import com.trektravel.dao.TrekkerProfileDAO;
import com.trektravel.dao.UserDAO;
import com.trektravel.model.Booking;
import com.trektravel.model.Trek;
import com.trektravel.model.TrekkerProfile;
import com.trektravel.model.User;
import com.trektravel.util.DBUtil;
import com.trektravel.util.UploadUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * System Administration servlet managing metrics dashboard, trekker ID verifications,
 * trek approvals, user status (block/unblock), and system-wide booking reservations.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
        "/admin/dashboard",
        "/admin/profiles",
        "/admin/profiles/review",
        "/admin/viewId",
        "/admin/treks",
        "/admin/treks/review",
        "/admin/users",
        "/admin/users/status",
        "/admin/bookings",
        "/admin/bookings/cancel"
})
public class AdminServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final TrekkerProfileDAO profileDAO = new TrekkerProfileDAO();
    private final TrekDAO trekDAO = new TrekDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void handle(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User admin = currentUser(req);

        // Method guard per specification §5
        boolean isAction = path.endsWith("/review") || path.endsWith("/status") || path.endsWith("/cancel");
        if (isAction && !isPost) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        if (!isAction && isPost) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        switch (path) {
            case "/admin/dashboard":
                showDashboard(req, resp);
                break;
            case "/admin/profiles":
                showProfiles(req, resp);
                break;
            case "/admin/profiles/review":
                reviewProfile(req, resp);
                break;
            case "/admin/viewId":
                viewIdProof(req, resp);
                break;
            case "/admin/treks":
                showTreks(req, resp);
                break;
            case "/admin/treks/review":
                reviewTrek(req, resp);
                break;
            case "/admin/users":
                showUsers(req, resp);
                break;
            case "/admin/users/status":
                changeUserStatus(req, resp, admin);
                break;
            case "/admin/bookings":
                showBookings(req, resp);
                break;
            case "/admin/bookings/cancel":
                cancelBooking(req, resp, admin);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int pendingProfiles = profileDAO.countPendingProfiles();
        int pendingTreks = trekDAO.countPendingTreks();
        int totalUsers = userDAO.countUsers();
        int confirmedBookings = bookingDAO.countConfirmedBookings();
        BigDecimal totalRevenue = bookingDAO.getTotalRevenue();

        req.setAttribute("pendingProfiles", pendingProfiles);
        req.setAttribute("pendingTreks", pendingTreks);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("confirmedBookings", confirmedBookings);
        req.setAttribute("totalRevenue", totalRevenue);

        show(req, resp, "admin/dashboard");
    }

    private void showProfiles(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String filter = p(req, "status");
        if (filter == null) filter = "PENDING";

        List<TrekkerProfile> profiles = profileDAO.getAllProfiles(filter);
        req.setAttribute("profiles", profiles);
        req.setAttribute("currentFilter", filter);
        show(req, resp, "admin/profiles");
    }

    private void reviewProfile(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int profileId = intP(req, "profileId", -1);
        String decision = p(req, "decision");
        if (decision == null) decision = p(req, "action");
        if ("APPROVE".equalsIgnoreCase(decision)) decision = "APPROVED";
        if ("REJECT".equalsIgnoreCase(decision)) decision = "REJECTED";
        String remarks = p(req, "remarks");

        if (profileId <= 0 || decision == null || (!"APPROVED".equalsIgnoreCase(decision) && !"REJECTED".equalsIgnoreCase(decision))) {
            flashError(req, "Invalid profile review action.");
            redirect(req, resp, "/admin/profiles");
            return;
        }

        boolean updated = profileDAO.updateVerificationStatus(profileId, decision.toUpperCase(), remarks);
        if (updated) {
            flashSuccess(req, "Trekker profile #" + profileId + " marked as " + decision.toUpperCase() + ".");
        } else {
            flashError(req, "Failed to update profile verification status.");
        }
        redirect(req, resp, "/admin/profiles");
    }

    /**
     * Streams the uploaded government ID proof directly and securely to authenticated admins.
     */
    private void viewIdProof(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int profileId = intP(req, "profileId", -1);
        TrekkerProfile profile = profileDAO.findById(profileId);
        if (profile == null || profile.getIdProofFile() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile or ID proof document not found.");
            return;
        }

        File file = new File(DBUtil.getUploadDir(), profile.getIdProofFile());
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Uploaded ID proof file is not available on disk.");
            return;
        }

        String mimeType = UploadUtil.getMimeType(profile.getIdProofFile());
        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());
        resp.setHeader("Content-Disposition", "inline; filename=\"" + profile.getIdProofFile() + "\"");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }

    private void showTreks(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String filter = p(req, "status");
        if (filter == null) filter = "ALL";

        List<Trek> treks = trekDAO.getAllTreksForAdmin(filter);
        req.setAttribute("treks", treks);
        req.setAttribute("currentFilter", filter);
        show(req, resp, "admin/treks");
    }

    private void reviewTrek(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int trekId = intP(req, "trekId", -1);
        String decision = p(req, "decision");
        if (decision == null) decision = p(req, "action");
        if ("APPROVE".equalsIgnoreCase(decision)) decision = "APPROVED";
        if ("REJECT".equalsIgnoreCase(decision)) decision = "REJECTED";

        if (trekId <= 0 || decision == null || (!"APPROVED".equalsIgnoreCase(decision) && !"REJECTED".equalsIgnoreCase(decision))) {
            flashError(req, "Invalid trek review decision.");
            redirect(req, resp, "/admin/treks");
            return;
        }

        boolean updated = trekDAO.updateTrekStatus(trekId, decision.toUpperCase());
        if (updated) {
            flashSuccess(req, "Trek listing #" + trekId + " marked as " + decision.toUpperCase() + ".");
        } else {
            flashError(req, "Failed to update trek status.");
        }
        redirect(req, resp, "/admin/treks");
    }

    private void showUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<User> users = userDAO.getAllUsers();
        req.setAttribute("users", users);
        show(req, resp, "admin/users");
    }

    private void changeUserStatus(HttpServletRequest req, HttpServletResponse resp, User admin)
            throws IOException {
        int userId = intP(req, "userId", -1);
        String newStatus = p(req, "newStatus");

        if (userId <= 0 || newStatus == null || (!"ACTIVE".equalsIgnoreCase(newStatus) && !"BLOCKED".equalsIgnoreCase(newStatus))) {
            flashError(req, "Invalid status update request.");
            redirect(req, resp, "/admin/users");
            return;
        }

        User targetUser = userDAO.findById(userId);
        if (targetUser == null) {
            flashError(req, "Target user not found.");
            redirect(req, resp, "/admin/users");
            return;
        }

        // Cannot block self or another admin per specification §5
        if (targetUser.getId() == admin.getId() || targetUser.isAdmin()) {
            flashError(req, "Action denied: System Administrators cannot be blocked.");
            redirect(req, resp, "/admin/users");
            return;
        }

        boolean updated = userDAO.updateStatus(userId, newStatus.toUpperCase());
        if (updated) {
            flashSuccess(req, "User " + targetUser.getName() + " has been " + (newStatus.equalsIgnoreCase("BLOCKED") ? "BLOCKED" : "ACTIVATED") + ".");
        } else {
            flashError(req, "Failed to update user status.");
        }
        redirect(req, resp, "/admin/users");
    }

    private void showBookings(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Booking> bookings = bookingDAO.getAllBookingsForAdmin();
        req.setAttribute("bookings", bookings);
        show(req, resp, "admin/bookings");
    }

    private void cancelBooking(HttpServletRequest req, HttpServletResponse resp, User admin)
            throws IOException {
        int bookingId = intP(req, "bookingId", -1);
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            flashError(req, "Booking reservation not found.");
            redirect(req, resp, "/admin/bookings");
            return;
        }

        boolean cancelled = bookingDAO.cancelBooking(bookingId, admin.getId(), true);
        if (cancelled) {
            flashSuccess(req, "Booking #" + bookingId + " marked as CANCELLED. Associated slots reopened.");
        } else {
            flashError(req, "Failed to cancel booking reservation.");
        }
        redirect(req, resp, "/admin/bookings");
    }
}
