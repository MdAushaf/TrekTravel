package com.trektravel.servlet;

import com.trektravel.dao.BatchDAO;
import com.trektravel.dao.BookingDAO;
import com.trektravel.dao.TrekDAO;
import com.trektravel.model.Batch;
import com.trektravel.model.Booking;
import com.trektravel.model.Trek;
import com.trektravel.model.User;
import com.trektravel.util.Validate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Organizer management servlet handling trek listings, departure batches,
 * participant rosters, and organizer-side booking cancellations.
 */
@WebServlet(name = "OrganizerServlet", urlPatterns = {
        "/organizer/treks",
        "/organizer/trek/form",
        "/organizer/trek/save",
        "/organizer/trek/delete",
        "/organizer/batches",
        "/organizer/batch/save",
        "/organizer/batch/delete",
        "/organizer/bookings",
        "/organizer/booking/cancel"
})
public class OrganizerServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final TrekDAO trekDAO = new TrekDAO();
    private final BatchDAO batchDAO = new BatchDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void handle(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User organizer = currentUser(req);

        // Enforce method guards per specification §5: action URLs ending in /save, /delete, /cancel are POST-only
        boolean isAction = path.endsWith("/save") || path.endsWith("/delete") || path.endsWith("/cancel");
        if (isAction && !isPost) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        if (!isAction && isPost) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        switch (path) {
            case "/organizer/treks":
                showTreks(req, resp, organizer);
                break;
            case "/organizer/trek/form":
                showTrekForm(req, resp, organizer);
                break;
            case "/organizer/trek/save":
                saveTrek(req, resp, organizer);
                break;
            case "/organizer/trek/delete":
                deleteTrek(req, resp, organizer);
                break;
            case "/organizer/batches":
                showBatches(req, resp, organizer);
                break;
            case "/organizer/batch/save":
                saveBatch(req, resp, organizer);
                break;
            case "/organizer/batch/delete":
                deleteBatch(req, resp, organizer);
                break;
            case "/organizer/bookings":
                showBookings(req, resp, organizer);
                break;
            case "/organizer/booking/cancel":
                cancelBooking(req, resp, organizer);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void showTreks(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws ServletException, IOException {
        List<Trek> treks = trekDAO.getTreksByOrganizer(organizer.getId());
        req.setAttribute("treks", treks);
        show(req, resp, "organizer/treks");
    }

    private void showTrekForm(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws ServletException, IOException {
        int trekId = intP(req, "id", -1);
        Trek trek = null;
        if (trekId > 0) {
            trek = trekDAO.findById(trekId);
            if (trek == null || trek.getOrganizerId() != organizer.getId()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trek not found or unauthorized.");
                return;
            }
        }
        req.setAttribute("trek", trek);
        show(req, resp, "organizer/trek_form");
    }

    private void saveTrek(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws IOException {
        int trekId = intP(req, "trekId", -1);
        String name = p(req, "name");
        String state = p(req, "state");
        String region = p(req, "region");
        String baseVillage = p(req, "baseVillage");
        String difficulty = p(req, "difficulty");
        int durationDays = intP(req, "durationDays", 0);
        int maxAltitudeM = intP(req, "maxAltitudeM", -1);
        BigDecimal trekDistanceKm = decP(req, "trekDistanceKm", null);
        String bestSeason = p(req, "bestSeason");
        String highlights = p(req, "highlights");
        String inclusions = p(req, "inclusions");
        String description = p(req, "description");

        if (!Validate.nonEmpty(name) || !Validate.nonEmpty(state) || !Validate.nonEmpty(region)
                || !Validate.nonEmpty(baseVillage) || !Validate.nonEmpty(difficulty)
                || !Validate.nonEmpty(bestSeason) || !Validate.nonEmpty(highlights)
                || !Validate.nonEmpty(inclusions) || !Validate.nonEmpty(description)) {
            flashError(req, "All trek information fields are required.");
            redirect(req, resp, trekId > 0 ? "/organizer/trek/form?id=" + trekId : "/organizer/trek/form");
            return;
        }

        if (!Validate.intBetween(durationDays, 1, 30)) {
            flashError(req, "Trek duration must be between 1 and 30 days.");
            redirect(req, resp, trekId > 0 ? "/organizer/trek/form?id=" + trekId : "/organizer/trek/form");
            return;
        }

        if (maxAltitudeM < 0 || trekDistanceKm == null || trekDistanceKm.compareTo(BigDecimal.ZERO) < 0) {
            flashError(req, "Altitude and trek distance must be valid positive numbers.");
            redirect(req, resp, trekId > 0 ? "/organizer/trek/form?id=" + trekId : "/organizer/trek/form");
            return;
        }

        Trek trek = new Trek();
        trek.setOrganizerId(organizer.getId());
        trek.setName(name);
        trek.setState(state);
        trek.setRegion(region);
        trek.setBaseVillage(baseVillage);
        trek.setDifficulty(difficulty.toUpperCase());
        trek.setDurationDays(durationDays);
        trek.setMaxAltitudeM(maxAltitudeM);
        trek.setTrekDistanceKm(trekDistanceKm);
        trek.setBestSeason(bestSeason);
        trek.setHighlights(highlights);
        trek.setInclusions(inclusions);
        trek.setDescription(description);

        if (trekId > 0) {
            // Edit existing trek: verify ownership
            Trek existing = trekDAO.findById(trekId);
            if (existing == null || existing.getOrganizerId() != organizer.getId()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trek not found or unauthorized.");
                return;
            }
            trek.setTrekId(trekId);
            boolean updated = trekDAO.updateTrek(trek);
            if (updated) {
                flashSuccess(req, "Trek updated successfully! Status reset to PENDING awaiting Admin re-approval.");
            } else {
                flashError(req, "Failed to update trek.");
            }
        } else {
            // Create new trek
            int createdId = trekDAO.createTrek(trek);
            if (createdId > 0) {
                flashSuccess(req, "Trek listing created successfully! Status is PENDING awaiting Admin approval.");
            } else {
                flashError(req, "Failed to create trek listing.");
            }
        }
        redirect(req, resp, "/organizer/treks");
    }

    private void deleteTrek(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws IOException {
        int trekId = intP(req, "trekId", -1);
        Trek trek = trekDAO.findById(trekId);
        if (trek == null || trek.getOrganizerId() != organizer.getId()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trek not found or unauthorized.");
            return;
        }

        if (trekDAO.hasBookings(trekId)) {
            flashError(req, "Cannot delete trek: it has existing booking reservations.");
            redirect(req, resp, "/organizer/treks");
            return;
        }

        boolean deleted = trekDAO.deleteTrek(trekId, organizer.getId());
        if (deleted) {
            flashSuccess(req, "Trek listing deleted successfully.");
        } else {
            flashError(req, "Unable to delete trek.");
        }
        redirect(req, resp, "/organizer/treks");
    }

    private void showBatches(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws ServletException, IOException {
        int trekId = intP(req, "trekId", -1);
        Trek trek = trekDAO.findById(trekId);
        if (trek == null || trek.getOrganizerId() != organizer.getId()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trek not found or unauthorized.");
            return;
        }

        List<Batch> batches = batchDAO.getBatchesByTrekId(trekId);
        req.setAttribute("trek", trek);
        req.setAttribute("batches", batches);
        show(req, resp, "organizer/batches");
    }

    private void saveBatch(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws IOException {
        int trekId = intP(req, "trekId", -1);
        Trek trek = trekDAO.findById(trekId);
        if (trek == null || trek.getOrganizerId() != organizer.getId()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Trek not found or unauthorized.");
            return;
        }

        LocalDate startDate = Validate.date(p(req, "startDate"));
        BigDecimal pricePerPerson = decP(req, "pricePerPerson", null);
        int discountPercent = intP(req, "verifiedDiscountPercent", 0);
        int totalSlots = intP(req, "totalSlots", 15);

        if (startDate == null || startDate.isBefore(LocalDate.now())) {
            flashError(req, "Batch departure date must be in the future.");
            redirect(req, resp, "/organizer/batches?trekId=" + trekId);
            return;
        }

        if (pricePerPerson == null || pricePerPerson.compareTo(BigDecimal.ZERO) <= 0) {
            flashError(req, "Price per person must be a positive amount.");
            redirect(req, resp, "/organizer/batches?trekId=" + trekId);
            return;
        }

        if (!Validate.intBetween(discountPercent, 0, 50)) {
            flashError(req, "Verified trekker discount must be between 0% and 50%.");
            redirect(req, resp, "/organizer/batches?trekId=" + trekId);
            return;
        }

        if (!Validate.intBetween(totalSlots, 1, 50)) {
            flashError(req, "Total slots must be between 1 and 50.");
            redirect(req, resp, "/organizer/batches?trekId=" + trekId);
            return;
        }

        Batch batch = new Batch();
        batch.setTrekId(trekId);
        batch.setStartDate(startDate);
        batch.setPricePerPerson(pricePerPerson);
        batch.setVerifiedDiscountPercent(discountPercent);
        batch.setTotalSlots(totalSlots);
        batch.setStatus("OPEN");

        int batchId = batchDAO.createBatch(batch);
        if (batchId > 0) {
            flashSuccess(req, "Departure batch created successfully.");
        } else {
            flashError(req, "Failed to create departure batch.");
        }
        redirect(req, resp, "/organizer/batches?trekId=" + trekId);
    }

    private void deleteBatch(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws IOException {
        int batchId = intP(req, "batchId", -1);
        int trekId = intP(req, "trekId", -1);

        if (!batchDAO.verifyBatchOwnership(batchId, organizer.getId())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Batch not found or unauthorized.");
            return;
        }

        if (batchDAO.hasConfirmedBookings(batchId)) {
            flashError(req, "Cannot delete departure batch: It contains confirmed trekker bookings.");
            redirect(req, resp, "/organizer/batches?trekId=" + trekId);
            return;
        }

        boolean deleted = batchDAO.deleteBatch(batchId);
        if (deleted) {
            flashSuccess(req, "Departure batch removed.");
        } else {
            flashError(req, "Failed to remove batch.");
        }
        redirect(req, resp, "/organizer/batches?trekId=" + trekId);
    }

    private void showBookings(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws ServletException, IOException {
        List<Booking> bookings = bookingDAO.getBookingsByOrganizerId(organizer.getId());
        req.setAttribute("bookings", bookings);
        show(req, resp, "organizer/bookings");
    }

    private void cancelBooking(HttpServletRequest req, HttpServletResponse resp, User organizer)
            throws IOException {
        int bookingId = intP(req, "bookingId", -1);
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null || booking.getOrganizerId() != organizer.getId()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found or unauthorized.");
            return;
        }

        boolean cancelled = bookingDAO.cancelBooking(bookingId, organizer.getId(), true);
        if (cancelled) {
            flashSuccess(req, "Booking reservation marked as CANCELLED. Slots have been reopened.");
        } else {
            flashError(req, "Unable to cancel booking reservation.");
        }
        redirect(req, resp, "/organizer/bookings");
    }
}
