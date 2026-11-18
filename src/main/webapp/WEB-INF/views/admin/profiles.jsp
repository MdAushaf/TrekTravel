<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<TrekkerProfile> profiles = (List<TrekkerProfile>) request.getAttribute("profiles");
    String currentFilter = (String) request.getAttribute("currentFilter");
    if (currentFilter == null) currentFilter = "PENDING";
%>

<div class="container py-3 mb-5">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">Trekker ID Verifications</h3>
            <p class="text-secondary small mb-0">Verify government identification proofs and validate emergency contact records</p>
        </div>
        <a href="<%= cp %>/admin/dashboard" class="btn btn-outline-secondary btn-sm">
            &larr; Back to Dashboard
        </a>
    </div>

    <!-- Filter Tabs -->
    <ul class="nav nav-pills mb-4">
        <li class="nav-item">
            <a class="nav-link <%= "PENDING".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/profiles?status=PENDING">Pending Review</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= "APPROVED".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/profiles?status=APPROVED">Approved</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= "REJECTED".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/profiles?status=REJECTED">Rejected</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= "ALL".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/profiles?status=ALL">All Profiles</a>
        </li>
    </ul>

    <!-- Profiles Table -->
    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (profiles != null && !profiles.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">Trekker Details</th>
                                <th>Age &amp; City</th>
                                <th>Fitness &amp; Medical</th>
                                <th>Emergency Contact</th>
                                <th>ID Proof Document</th>
                                <th>Status</th>
                                <th class="text-end pe-4">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (TrekkerProfile p : profiles) { %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.e(p.getUserName()) %></div>
                                        <span class="small text-muted d-block">📧 <%= H.e(p.getUserEmail()) %></span>
                                        <span class="small text-muted d-block">📞 <%= H.e(p.getUserPhone()) %></span>
                                    </td>
                                    <td>
                                        <div>DOB: <%= H.dt(p.getDateOfBirth()) %></div>
                                        <span class="small text-muted">City: <%= H.e(p.getCity()) %></span>
                                    </td>
                                    <td>
                                        <div>Level: <strong><%= H.e(p.getFitnessLevel()) %></strong></div>
                                        <% if (p.getMedicalNotes() != null && !p.getMedicalNotes().trim().isEmpty()) { %>
                                            <span class="small text-danger d-block mt-1">⚠️ <%= H.e(p.getMedicalNotes()) %></span>
                                        <% } else { %>
                                            <span class="small text-muted">No medical conditions</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <div class="fw-bold"><%= H.e(p.getEmergencyContactName()) %></div>
                                        <span class="small text-danger">🚨 <%= H.e(p.getEmergencyContactPhone()) %></span>
                                    </td>
                                    <td>
                                        <% if (p.getIdProofFile() != null) { %>
                                            <a href="<%= cp %>/admin/viewId?profileId=<%= p.getProfileId() %>" target="_blank"
                                               class="btn btn-outline-primary btn-sm d-inline-flex align-items-center gap-1">
                                                <span>📄</span> View ID Proof
                                            </a>
                                        <% } else { %>
                                            <span class="text-muted small">No file</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <%= H.badge(p.getVerificationStatus()) %>
                                        <% if (p.getAdminRemarks() != null && !p.getAdminRemarks().trim().isEmpty()) { %>
                                            <span class="small text-muted d-block mt-1" style="font-size: 0.75rem;">
                                                Note: <%= H.e(p.getAdminRemarks()) %>
                                            </span>
                                        <% } %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <button type="button" class="btn btn-outline-secondary btn-sm"
                                                data-bs-toggle="modal" data-bs-target="#reviewModal<%= p.getProfileId() %>">
                                            Review
                                        </button>

                                        <!-- Review Modal Dialog -->
                                        <div class="modal fade" id="reviewModal<%= p.getProfileId() %>" tabindex="-1" aria-hidden="true">
                                            <div class="modal-dialog modal-dialog-centered">
                                                <div class="modal-content text-start">
                                                    <form method="post" action="<%= cp %>/admin/profiles/review">
                                                        <%= H.csrf(session) %>
                                                        <input type="hidden" name="profileId" value="<%= p.getProfileId() %>">

                                                        <div class="modal-header bg-light">
                                                            <h5 class="modal-title fw-bold">Review ID: <%= H.e(p.getUserName()) %></h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <div class="modal-body p-4">
                                                            <div class="mb-3">
                                                                <label class="form-label fw-bold">Verification Decision</label>
                                                                <div class="d-flex gap-3">
                                                                    <div class="form-check">
                                                                        <input class="form-check-input" type="radio" name="decision" id="app<%= p.getProfileId() %>" value="APPROVED" checked>
                                                                        <label class="form-check-label fw-bold text-success" for="app<%= p.getProfileId() %>">
                                                                            ✅ Approve Profile
                                                                        </label>
                                                                    </div>
                                                                    <div class="form-check">
                                                                        <input class="form-check-input" type="radio" name="decision" id="rej<%= p.getProfileId() %>" value="REJECTED">
                                                                        <label class="form-check-label fw-bold text-danger" for="rej<%= p.getProfileId() %>">
                                                                            ❌ Reject Profile
                                                                        </label>
                                                                    </div>
                                                                </div>
                                                            </div>

                                                            <div class="mb-3">
                                                                <label for="remarks<%= p.getProfileId() %>" class="form-label fw-bold small text-muted">ADMIN REMARKS / REASON</label>
                                                                <textarea class="form-control" id="remarks<%= p.getProfileId() %>" name="remarks" rows="3"
                                                                          placeholder="Enter reason if rejecting (e.g. Document blurry, mismatched name)..."><%= H.e(p.getAdminRemarks()) %></textarea>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer bg-light">
                                                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                                                            <button type="submit" class="btn btn-tt-primary px-4">Submit Decision</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="p-5 text-center text-muted">
                    <div class="display-3 text-muted mb-3">🪪</div>
                    <h5 class="fw-bold">No Profiles Found</h5>
                    <p class="small text-muted mb-0">No trekker profiles currently match the selected "<%= H.e(currentFilter) %>" filter.</p>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
