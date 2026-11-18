<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    Trek trek = (Trek) request.getAttribute("trek");
    List<Batch> batches = (List<Batch>) request.getAttribute("batches");
    TrekkerProfile profile = (TrekkerProfile) request.getAttribute("trekkerProfile");
    Boolean hasProfile = (Boolean) request.getAttribute("hasProfile");
    if (hasProfile == null) hasProfile = false;
    Boolean isVerifiedTrekker = (Boolean) request.getAttribute("isVerifiedTrekker");
    if (isVerifiedTrekker == null) isVerifiedTrekker = false;
%>

<div class="container mb-5">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="<%= cp %>/">Home</a></li>
            <li class="breadcrumb-item"><a href="<%= cp %>/search">Treks</a></li>
            <li class="breadcrumb-item active" aria-current="page"><%= H.e(trek.getName()) %></li>
        </ol>
    </nav>

    <!-- Trek Header Hero Card -->
    <div class="card shadow-sm border-0 mb-4 overflow-hidden">
        <div class="p-4 p-md-5 bg-dark text-white" style="background: linear-gradient(135deg, #132e15 0%, #1e4620 100%);">
            <div class="row align-items-center">
                <div class="col-lg-8">
                    <div class="d-flex align-items-center gap-2 mb-2">
                        <span class="badge bg-light text-dark">📍 <%= H.e(trek.getState()) %>, <%= H.e(trek.getRegion()) %></span>
                        <%= H.difficultyBadge(trek.getDifficulty()) %>
                    </div>
                    <h1 class="fw-bold mb-2"><%= H.e(trek.getName()) %></h1>
                    <p class="text-white-50 mb-3">
                        Base Village: <strong><%= H.e(trek.getBaseVillage()) %></strong> &bull;
                        Best Season: <strong><%= H.e(trek.getBestSeason()) %></strong> &bull;
                        Curated by: <strong><%= H.e(trek.getOrganizerName()) %></strong>
                    </p>
                    <div class="d-flex flex-wrap gap-4 text-white small">
                        <div>⏱️ <strong><%= trek.getDurationDays() %></strong> Days Duration</div>
                        <div>⛰️ <strong><%= trek.getMaxAltitudeM() %> m</strong> Max Altitude</div>
                        <div>🥾 <strong><%= trek.getTrekDistanceKm() %> km</strong> Total Distance</div>
                    </div>
                </div>
                <div class="col-lg-4 text-lg-end mt-4 mt-lg-0">
                    <div class="p-3 bg-white bg-opacity-10 rounded-3 d-inline-block text-start border border-white border-opacity-25">
                        <span class="small text-white-50 d-block">Starting from</span>
                        <div class="display-6 fw-bold text-white"><%= H.moneyShort(trek.getMinPrice()) %></div>
                        <span class="small text-white-50">per person (Standard)</span>
                        <% if (trek.getMaxDiscountPercent() > 0) { %>
                            <div class="mt-2 text-warning small fw-bold">
                                🎖️ Up to <%= trek.getMaxDiscountPercent() %>% Verified Discount Available
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>

        <!-- Trek Description & Highlights Tabs -->
        <div class="card-body p-4">
            <div class="row g-4">
                <div class="col-lg-7">
                    <h5 class="fw-bold text-dark border-bottom pb-2">Overview</h5>
                    <p class="text-secondary mb-4 leading-relaxed"><%= H.e(trek.getDescription()) %></p>

                    <h5 class="fw-bold text-dark border-bottom pb-2">Trail Highlights</h5>
                    <p class="text-secondary mb-4"><%= H.e(trek.getHighlights()) %></p>

                    <h5 class="fw-bold text-dark border-bottom pb-2">Inclusions & Logistics</h5>
                    <p class="text-secondary mb-0"><%= H.e(trek.getInclusions()) %></p>
                </div>

                <div class="col-lg-5">
                    <div class="card bg-light border-0 p-3 rounded-3">
                        <h6 class="fw-bold mb-3 d-flex align-items-center gap-2">
                            <span>🛡️</span> Trekker Requirements & Safety
                        </h6>
                        <ul class="list-unstyled small text-secondary mb-3">
                            <li class="mb-2">✔️ Emergency contact information on file is mandatory before booking slots.</li>
                            <li class="mb-2">✔️ Valid Govt ID required at base village check-in point.</li>
                            <li class="mb-2">✔️ Full cancellation permitted up to 48 hours prior to departure.</li>
                            <li class="mb-0">✔️ Certified wilderness guides and medical kits accompany every departure.</li>
                        </ul>
                        <% if (currentUser == null) { %>
                            <div class="alert alert-info py-2 small mb-0">
                                <a href="<%= cp %>/login" class="alert-link fw-bold">Log in as Trekker</a> to book departure slots.
                            </div>
                        <% } else if (currentUser.isTrekker() && !hasProfile) { %>
                            <div class="alert alert-warning py-2 small mb-0">
                                ⚠️ Emergency contact required before booking: <a href="<%= cp %>/trekker/profile" class="alert-link fw-bold">Complete Trekker Profile</a>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Upcoming Batches Schedule Table -->
    <div class="card shadow-sm border-0 mb-5">
        <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
            <h5 class="fw-bold mb-0">Upcoming Departure Batches</h5>
            <span class="badge bg-success-subtle text-success-emphasis border border-success-subtle">
                Real-time Guaranteed Inventory
            </span>
        </div>
        <div class="card-body p-0">
            <% if (batches != null && !batches.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">Dates &amp; Duration</th>
                                <th>Slots Left</th>
                                <th>Standard Price</th>
                                <th>Verified Trekker Price</th>
                                <th class="text-end pe-4">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Batch b : batches) {
                                int slotsLeft = b.getSlotsLeft();
                                boolean hasSlots = slotsLeft > 0;
                            %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.dt(b.getStartDate()) %> &ndash; <%= H.dt(b.getEndDate()) %></div>
                                        <span class="small text-muted"><%= b.getDurationDays() %> Days Expedition</span>
                                    </td>
                                    <td>
                                        <% if (slotsLeft > 5) { %>
                                            <span class="badge bg-success"><%= slotsLeft %> slots left</span>
                                        <% } else if (slotsLeft > 0) { %>
                                            <span class="badge bg-warning text-dark"><%= slotsLeft %> slots left &bull; Fast Filling</span>
                                        <% } else { %>
                                            <span class="badge bg-danger">Sold Out</span>
                                        <% } %>
                                        <span class="small text-muted d-block mt-1">Total capacity: <%= b.getTotalSlots() %></span>
                                    </td>
                                    <td>
                                        <span class="fw-bold"><%= H.money(b.getPricePerPerson()) %></span>
                                        <span class="small text-muted d-block">per person</span>
                                    </td>
                                    <td>
                                        <% if (b.getVerifiedDiscountPercent() > 0) {
                                            BigDecimal discRate = b.getDiscountedPrice();
                                        %>
                                            <span class="fw-bold text-success"><%= H.money(discRate) %></span>
                                            <span class="verified-badge d-block mt-1"><%= b.getVerifiedDiscountPercent() %>% OFF Verified</span>
                                        <% } else { %>
                                            <span class="text-muted small">Standard rate applies</span>
                                        <% } %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <% if (!hasSlots) { %>
                                            <button class="btn btn-secondary btn-sm" disabled>Full</button>
                                        <% } else if (currentUser == null) { %>
                                            <a href="<%= cp %>/login" class="btn btn-outline-primary btn-sm">Login to Book</a>
                                        <% } else if (!currentUser.isTrekker()) { %>
                                            <span class="text-muted small">Trekker role required</span>
                                        <% } else if (!hasProfile) { %>
                                            <a href="<%= cp %>/trekker/profile" class="btn btn-warning btn-sm">Complete Profile First</a>
                                        <% } else { %>
                                            <!-- Trigger Booking Modal -->
                                            <button type="button" class="btn btn-tt-primary btn-sm px-3" data-bs-toggle="modal"
                                                    data-bs-target="#bookModal<%= b.getBatchId() %>">
                                                Book Slots
                                            </button>

                                            <!-- Booking Modal Dialog -->
                                            <div class="modal fade" id="bookModal<%= b.getBatchId() %>" tabindex="-1" aria-labelledby="modalLabel<%= b.getBatchId() %>" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered">
                                                    <div class="modal-content text-start">
                                                        <form method="post" action="<%= cp %>/trekker/book">
                                                            <%= H.csrf(session) %>
                                                            <input type="hidden" name="batchId" value="<%= b.getBatchId() %>">
                                                            <input type="hidden" name="trekId" value="<%= trek.getTrekId() %>">

                                                            <div class="modal-header bg-light">
                                                                <h5 class="modal-title fw-bold" id="modalLabel<%= b.getBatchId() %>">
                                                                    Book: <%= H.e(trek.getName()) %>
                                                                </h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <div class="modal-body p-4">
                                                                <div class="mb-3 p-3 bg-light rounded">
                                                                    <div class="small text-muted">Departure Dates:</div>
                                                                    <div class="fw-bold"><%= H.dt(b.getStartDate()) %> &ndash; <%= H.dt(b.getEndDate()) %></div>
                                                                    <div class="small text-muted mt-1">Available slots: <strong><%= slotsLeft %></strong></div>
                                                                </div>

                                                                <% if (isVerifiedTrekker && b.getVerifiedDiscountPercent() > 0) { %>
                                                                    <div class="alert alert-success py-2 small mb-3">
                                                                        🎖️ <strong>Verified Discount Applied:</strong> <%= b.getVerifiedDiscountPercent() %>% discount active on your booking!
                                                                    </div>
                                                                <% } else if (b.getVerifiedDiscountPercent() > 0) { %>
                                                                    <div class="alert alert-warning py-2 small mb-3">
                                                                        💡 <em>Note: Your profile is not yet approved. You are booking at the standard rate of <%= H.money(b.getPricePerPerson()) %>/person.</em>
                                                                    </div>
                                                                <% } %>

                                                                <div class="mb-3">
                                                                    <label for="numParts<%= b.getBatchId() %>" class="form-label fw-bold">Number of Participants (1 to 10)</label>
                                                                    <input type="number" class="form-control form-control-lg"
                                                                           id="numParts<%= b.getBatchId() %>" name="numParticipants"
                                                                           min="1" max="<%= Math.min(10, slotsLeft) %>" value="1" required
                                                                           oninput="calculateModalTotal(<%= b.getBatchId() %>, <%= b.getPricePerPerson() %>, <%= (isVerifiedTrekker ? b.getVerifiedDiscountPercent() : 0) %>)">
                                                                    <div class="form-text">Max 10 slots per booking, capped by remaining slots.</div>
                                                                </div>

                                                                <div class="p-3 bg-light rounded text-center border">
                                                                    <span class="small text-muted d-block">Estimated Total Amount:</span>
                                                                    <span class="fs-3 fw-bold text-success" id="modalTotal<%= b.getBatchId() %>">
                                                                        <%= H.money(isVerifiedTrekker ? b.getDiscountedPrice() : b.getPricePerPerson()) %>
                                                                    </span>
                                                                </div>
                                                            </div>
                                                            <div class="modal-footer bg-light">
                                                                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                                                                <button type="submit" class="btn btn-tt-primary px-4">Confirm &amp; Reserve</button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="p-5 text-center text-muted">
                    <p class="mb-0">There are currently no open upcoming batches scheduled for this trek. Please check back later.</p>
                </div>
            <% } %>
        </div>
    </div>
</div>

<script>
function calculateModalTotal(batchId, basePrice, discountPercent) {
    const input = document.getElementById("numParts" + batchId);
    const display = document.getElementById("modalTotal" + batchId);
    if (!input || !display) return;
    const count = parseInt(input.value, 10) || 1;
    const rate = basePrice * (1 - (discountPercent / 100));
    const total = rate * count;
    display.textContent = "₹" + total.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
</script>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
