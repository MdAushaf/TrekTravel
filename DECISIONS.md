# TrekTravel — Architectural Decisions & Sensible Defaults

## 1. Core Mechanisms Summary (Reference Port Analysis)

1. **Booking Transaction & Concurrency**:
   - Booking runs within a single atomic database transaction (`conn.setAutoCommit(false)`).
   - The departure batch row is locked using `SELECT ... FOR UPDATE` to prevent race conditions and overbooking.
   - Available slots are calculated dynamically in SQL (`total_slots - COALESCE(SUM(num_participants), 0)` for `CONFIRMED` bookings).
   - Prices (`price_per_person`) and verified discounts are fetched directly from the database—never trusted from the browser.
   - If the trekker has an `APPROVED` profile in `trekker_profiles`, the verified discount is applied; otherwise, standard price applies.
   - Price per person and discount percentage are snapshot into the `bookings` record at booking time for audit and history.

2. **SecurityFilter & CSRF Protection**:
   - Intercepts all incoming requests to enforce UTF-8 encoding and security headers (`X-Content-Type-Options`, `X-Frame-Options`, `Cache-Control`).
   - Maintains a unique cryptographic CSRF token in `HttpSession`; every state-altering POST request must supply a matching `_csrf` token.
   - Enforces strict Role-Based Access Control (RBAC) on path prefixes: `/trekker/*` (TREKKER), `/organizer/*` (ORGANIZER), `/admin/*` (ADMIN).
   - On every protected request, re-verifies the user's status directly in MySQL; if marked `BLOCKED`, the session is terminated immediately.

3. **File Uploads & Verification**:
   - Handled via Servlet 6.0 `@MultipartConfig` with a strict 2 MB file size ceiling.
   - File integrity is validated via magic bytes (validating real PDF `%PDF`, JPEG `FF D8 FF`, or PNG `89 50 4E 47` signatures).
   - Files are stored in a dedicated folder outside the public web root using unique UUID filenames to prevent path traversal.
   - Government ID documents are served strictly through the `/admin/viewId` endpoint with authentication checks.

---

## 2. Unspecified Defaults & Architectural Decisions

| Decision Area | Chosen Default | Rationale |
|---|---|---|
| **End Date Computation** | `start_date + duration_days - 1` | Per specification §4.4, end date is computed dynamically in Java model/DAO, eliminating data redundancy. |
| **Profile Requirement for Booking** | Trekker must have a profile row in `trekker_profiles` (even if PENDING) | Ensures emergency contact name and phone number are on file before booking any high-altitude trek. |
| **Cancellation Window** | Allowed up to 48 hours (2 days) prior to `start_date` | Per specification §5; allows organizer sufficient time to release freed slots to other travellers. |
| **Trek Editing Workflow** | Updating any trek details resets status to `PENDING` | Per specification §4.3 & reference; prevents organizers from changing approved trek details without re-approval. |
| **Delete Safeguards** | Treks with bookings or batches with confirmed bookings cannot be deleted | Maintains referential integrity and financial records; organizers are shown an informative alert. |
| **Batch Slot Range** | 1 to 50 participants per batch | Per specification §4.4 constraints (`total_slots INT (1–50)`). |
| **Participant Booking Limits** | 1 to 10 participants per individual booking | Per specification §4.5 constraints (`num_participants INT (1–10)`). |
| **Trekker Age Range** | 12 to 70 years old | Per specification §4.2; validated server-side on profile submission. |
| **Upload Directory Location** | Configurable via `upload.dir` in `db.properties`; defaults to `System.getProperty("user.home") + "/trektravel_uploads"` | Ensures uploads persist across redeployments and are never exposed directly via static URL. |
| **Default Database Credentials** | `db.url=jdbc:mysql://localhost:3306/trektravel?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`, user=`root`, password=`CHANGE_ME` | Standard safe placeholder per specification §7. |
| **Color Palette** | Deep forest green (`#1e4620`), slate (`#334155`), warm orange (`#ea580c`) | Mountain/outdoor aesthetic matching specification §5. |
