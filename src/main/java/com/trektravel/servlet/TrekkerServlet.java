package com.trektravel.servlet;

import com.trektravel.dao.BookingDAO;
import com.trektravel.dao.TrekkerProfileDAO;
import com.trektravel.model.Booking;
import com.trektravel.model.TrekkerProfile;
import com.trektravel.model.User;
import com.trektravel.util.UploadUtil;
import com.trektravel.util.Validate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Trekker portal servlet handling personal profile verification, slot bookings,
 * booking ledger views, and cancellations.
 */
@WebServlet(name = "TrekkerServlet", urlPatterns = {
        "/trekker/profile",
        "/trekker/bookings",
        "/trekker/book",
        "/trekker/cancel"
})
@MultipartConfig(
        maxFileSize = 2 * 1024 * 1024,      // 2 MB max file size
        maxRequestSize = 4 * 1024 * 1024,   // 4 MB max total request
        fileSizeThreshold = 512 * 1024      // 512 KB in-memory buffer
)
public class TrekkerServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private final TrekkerProfileDAO profileDAO = new TrekkerProfileDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void handle(HttpServletRequest req, HttpServletResponse resp, boolean isPost)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User user = currentUser(req);

        if ("/trekker/profile".equals(path)) {
            if (isPost) {
                saveProfile(req, resp, user);
            } else {
                showProfile(req, resp, user);
            }
        } else if ("/trekker/bookings".equals(path)) {
            if (isPost) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            } else {
                showBookings(req, resp, user);
            }
        } else if ("/trekker/book".equals(path)) {
            if (!methodOk(req, resp, "POST")) return;
            processBooking(req, resp, user);
        } else if ("/trekker/cancel".equals(path)) {
            if (!methodOk(req, resp, "POST")) return;
            processCancellation(req, resp, user);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showProfile(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        TrekkerProfile profile = profileDAO.findByUserId(user.getId());
        req.setAttribute("profile", profile);
        show(req, resp, "trekker/profile");
    }

    private void saveProfile(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        String dobStr = p(req, "dateOfBirth");
        String city = p(req, "city");
        String emergencyName = p(req, "emergencyContactName");
        String emergencyPhone = p(req, "emergencyContactPhone");
        String fitnessLevel = p(req, "fitnessLevel");
        String medicalNotes = p(req, "medicalNotes");

        TrekkerProfile existing = profileDAO.findByUserId(user.getId());

        LocalDate dob = Validate.date(dobStr);
        if (dob == null || !Validate.ageValid(dob)) {
            flashError(req, "Invalid Date of Birth. Trekkers must be between 12 and 70 years old.");
            redirect(req, resp, "/trekker/profile");
            return;
        }

        if (!Validate.nonEmpty(city) || !Validate.nonEmpty(emergencyName) || !Validate.nonEmpty(emergencyPhone)
                || !Validate.nonEmpty(fitnessLevel)) {
            flashError(req, "Please fill in all mandatory profile and emergency contact fields.");
            redirect(req, resp, "/trekker/profile");
            return;
        }

        if (!Validate.phone(emergencyPhone)) {
            flashError(req, "Please provide a valid 10-15 digit emergency contact phone number.");
            redirect(req, resp, "/trekker/profile");
            return;
        }

        // Handle ID proof file upload
        String idProofFileName = null;
        try {
            Part part = req.getPart("idProof");
            if (part != null && part.getSize() > 0) {
                UploadUtil.UploadResult result = UploadUtil.saveIdProof(part);
                if (!result.isSuccess()) {
                    flashError(req, result.getErrorMessage());
                    redirect(req, resp, "/trekker/profile");
                    return;
                }
                idProofFileName = result.getFileName();
            }
        } catch (Exception e) {
            flashError(req, "File upload error: " + e.getMessage());
            redirect(req, resp, "/trekker/profile");
            return;
        }

        if (idProofFileName == null) {
            if (existing != null && existing.getIdProofFile() != null) {
                idProofFileName = existing.getIdProofFile(); // retain previously uploaded file
            } else {
                flashError(req, "Please upload a valid Govt-issued ID proof document (PDF, JPG, PNG under 2 MB).");
                redirect(req, resp, "/trekker/profile");
                return;
            }
        }

        TrekkerProfile profile = new TrekkerProfile();
        profile.setUserId(user.getId());
        profile.setDateOfBirth(dob);
        profile.setCity(city);
        profile.setEmergencyContactName(emergencyName);
        profile.setEmergencyContactPhone(emergencyPhone);
        profile.setFitnessLevel(fitnessLevel.toUpperCase());
        profile.setMedicalNotes(medicalNotes);
        profile.setIdProofFile(idProofFileName);

        boolean saved = profileDAO.saveOrUpdate(profile);
        if (saved) {
            flashSuccess(req, "Trekker profile submitted successfully! Verification is now PENDING review by Admin.");
        } else {
            flashError(req, "Unable to save profile details. Please try again.");
        }
        redirect(req, resp, "/trekker/profile");
    }

    private void showBookings(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        List<Booking> bookings = bookingDAO.getBookingsByUserId(user.getId());
        req.setAttribute("bookings", bookings);
        show(req, resp, "trekker/bookings");
    }

    private void processBooking(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        int batchId = intP(req, "batchId", -1);
        int numParticipants = intP(req, "numParticipants", 1);
        int trekId = intP(req, "trekId", -1);

        // Verification check: Profile must exist before booking
        TrekkerProfile profile = profileDAO.findByUserId(user.getId());
        if (profile == null) {
            flashError(req, "Emergency contact details are mandatory before booking a trek. Please complete your profile first.");
            redirect(req, resp, "/trekker/profile");
            return;
        }

        BookingDAO.BookingResult result = bookingDAO.createBookingInTransaction(user.getId(), batchId, numParticipants);
        if (result.isSuccess()) {
            flashSuccess(req, result.getMessage());
            redirect(req, resp, "/trekker/bookings");
        } else {
            flashError(req, result.getMessage());
            if (trekId > 0) {
                redirect(req, resp, "/trek?id=" + trekId);
            } else {
                redirect(req, resp, "/search");
            }
        }
    }

    private void processCancellation(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        int bookingId = intP(req, "bookingId", -1);
        if (bookingId <= 0) {
            flashError(req, "Invalid booking request.");
            redirect(req, resp, "/trekker/bookings");
            return;
        }

        boolean cancelled = bookingDAO.cancelBooking(bookingId, user.getId(), false);
        if (cancelled) {
            flashSuccess(req, "Booking cancelled successfully. Your slots have been released.");
        } else {
            flashError(req, "Cancellation failed: Cancellations are permitted strictly up to 48 hours prior to the batch start date.");
        }
        redirect(req, resp, "/trekker/bookings");
    }
}
