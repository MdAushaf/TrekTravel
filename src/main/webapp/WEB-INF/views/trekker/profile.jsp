<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    TrekkerProfile profile = (TrekkerProfile) request.getAttribute("profile");
    boolean hasExisting = (profile != null);
%>

<div class="container py-3 mb-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <!-- Header -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h3 class="fw-bold mb-1">Trekker Profile &amp; Verification</h3>
                    <p class="text-secondary small mb-0">Emergency contact registration &amp; Government ID proof verification</p>
                </div>
                <% if (hasExisting) { %>
                    <div><%= H.badge(profile.getVerificationStatus()) %></div>
                <% } %>
            </div>

            <!-- Verification Status Alert -->
            <% if (hasExisting) { %>
                <% if (profile.isApproved()) { %>
                    <div class="alert alert-success d-flex align-items-center mb-4" role="alert">
                        <span class="fs-3 me-3">🎖️</span>
                        <div>
                            <h6 class="fw-bold mb-1">Verified Trekker Status Approved</h6>
                            <p class="small mb-0">
                                Verified on <%= H.dt(profile.getVerifiedAt() != null ? profile.getVerifiedAt().toLocalDateTime().toLocalDate() : null) %>.
                                You are receiving automatic verified discounts on all departure batches!
                            </p>
                        </div>
                    </div>
                <% } else if (profile.isPending()) { %>
                    <div class="alert alert-warning d-flex align-items-center mb-4" role="alert">
                        <span class="fs-3 me-3">⏳</span>
                        <div>
                            <h6 class="fw-bold mb-1">Profile Verification Pending Admin Review</h6>
                            <p class="small mb-0">
                                Your ID proof document was submitted on <%= H.dt(profile.getSubmittedAt() != null ? profile.getSubmittedAt().toLocalDateTime().toLocalDate() : null) %>.
                                Platform admins are reviewing your documents. You can still book treks; verified discounts will activate once approved.
                            </p>
                        </div>
                    </div>
                <% } else if (profile.isRejected()) { %>
                    <div class="alert alert-danger d-flex align-items-center mb-4" role="alert">
                        <span class="fs-3 me-3">❌</span>
                        <div>
                            <h6 class="fw-bold mb-1">Profile Verification Rejected</h6>
                            <p class="small mb-1">
                                <strong>Admin Remarks:</strong> <%= H.e(profile.getAdminRemarks() != null ? profile.getAdminRemarks() : "Uploaded document is unclear or invalid.") %>
                            </p>
                            <p class="small mb-0">Please review your information, attach a clear Government ID copy, and re-submit.</p>
                        </div>
                    </div>
                <% } %>
            <% } else { %>
                <div class="alert alert-info d-flex align-items-center mb-4" role="alert">
                    <span class="fs-3 me-3">📝</span>
                    <div>
                        <h6 class="fw-bold mb-1">Complete Profile Before Booking</h6>
                        <p class="small mb-0">
                            High-altitude expeditions require an emergency contact on file for safety. Upload your Government ID to unlock up to 25% verified trekker discounts.
                        </p>
                    </div>
                </div>
            <% } %>

            <!-- Profile Form -->
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-body p-4 p-md-5">
                    <form method="post" action="<%= cp %>/trekker/profile?_csrf=<%= H.token(session) %>" enctype="multipart/form-data">
                        <%= H.csrf(session) %>

                        <h5 class="fw-bold text-dark mb-3 border-bottom pb-2">1. Personal &amp; Medical Info</h5>
                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="dateOfBirth" class="form-label fw-bold small text-muted">DATE OF BIRTH (AGE 12-70)</label>
                                <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth"
                                       value="<%= (hasExisting && profile.getDateOfBirth() != null) ? profile.getDateOfBirth().toString() : "" %>" required>
                                <div class="form-text">Must be between 12 and 70 years of age.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="city" class="form-label fw-bold small text-muted">CURRENT CITY</label>
                                <input type="text" class="form-control" id="city" name="city"
                                       value="<%= hasExisting ? H.e(profile.getCity()) : "" %>" placeholder="e.g. Pune, Dehradun, Delhi" required>
                            </div>
                            <div class="col-md-6">
                                <label for="fitnessLevel" class="form-label fw-bold small text-muted">FITNESS LEVEL</label>
                                <select class="form-select" id="fitnessLevel" name="fitnessLevel" required>
                                    <option value="BEGINNER" <%= (hasExisting && "BEGINNER".equalsIgnoreCase(profile.getFitnessLevel())) ? "selected" : "" %>>Beginner (Occasional walks, first trek)</option>
                                    <option value="INTERMEDIATE" <%= (hasExisting && "INTERMEDIATE".equalsIgnoreCase(profile.getFitnessLevel())) ? "selected" : "" %>>Intermediate (Active runner / regular hiker)</option>
                                    <option value="EXPERIENCED" <%= (hasExisting && "EXPERIENCED".equalsIgnoreCase(profile.getFitnessLevel())) ? "selected" : "" %>>Experienced (Multiple high-altitude treks completed)</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="medicalNotes" class="form-label fw-bold small text-muted">MEDICAL NOTES / ALLERGIES (OPTIONAL)</label>
                                <input type="text" class="form-control" id="medicalNotes" name="medicalNotes"
                                       value="<%= hasExisting ? H.e(profile.getMedicalNotes()) : "" %>" placeholder="e.g. Asthma, allergies, prior knee surgery">
                            </div>
                        </div>

                        <h5 class="fw-bold text-dark mb-3 border-bottom pb-2">2. Emergency Contact (Mandatory)</h5>
                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="emergencyContactName" class="form-label fw-bold small text-muted">EMERGENCY CONTACT NAME &amp; RELATION</label>
                                <input type="text" class="form-control" id="emergencyContactName" name="emergencyContactName"
                                       value="<%= hasExisting ? H.e(profile.getEmergencyContactName()) : "" %>" placeholder="e.g. Sunita Sharma (Mother)" required>
                            </div>
                            <div class="col-md-6">
                                <label for="emergencyContactPhone" class="form-label fw-bold small text-muted">EMERGENCY CONTACT PHONE</label>
                                <input type="tel" class="form-control" id="emergencyContactPhone" name="emergencyContactPhone"
                                       value="<%= hasExisting ? H.e(profile.getEmergencyContactPhone()) : "" %>" placeholder="10-digit mobile number" required>
                            </div>
                        </div>

                        <h5 class="fw-bold text-dark mb-3 border-bottom pb-2">3. Government ID Proof Document</h5>
                        <div class="mb-4">
                            <label for="idProof" class="form-label fw-bold small text-muted">
                                UPLOAD GOVT-ID PROOF (AADHAAR / PASSPORT / VOTER ID)
                            </label>
                            <input type="file" class="form-control" id="idProof" name="idProof"
                                   accept=".pdf,.jpg,.jpeg,.png" <%= hasExisting ? "" : "required" %>>
                            <div class="form-text">
                                Allowed formats: PDF, JPG, PNG &bull; Max file size: 2 MB.
                                <% if (hasExisting && profile.getIdProofFile() != null) { %>
                                    <span class="text-success fw-bold d-block mt-1">
                                        📎 Current file on record: <%= H.e(profile.getIdProofFile()) %> (leave blank to keep current)
                                    </span>
                                <% } %>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                            <a href="<%= cp %>/trekker/bookings" class="btn btn-outline-secondary">Back to My Bookings</a>
                            <button type="submit" class="btn btn-tt-primary px-4 py-2 fw-bold">
                                <%= hasExisting ? "Update &amp; Re-submit Profile" : "Submit Profile for Verification" %>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
