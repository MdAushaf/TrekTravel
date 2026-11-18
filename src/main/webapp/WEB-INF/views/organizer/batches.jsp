<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    Trek trek = (Trek) request.getAttribute("trek");
    List<Batch> batches = (List<Batch>) request.getAttribute("batches");
%>

<div class="container py-3 mb-5">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">Departure Batches &mdash; <%= H.e(trek.getName()) %></h3>
            <p class="text-secondary small mb-0">
                <%= H.e(trek.getState()) %> &bull; <%= trek.getDurationDays() %> Days Expedition &bull;
                Base: <%= H.e(trek.getBaseVillage()) %>
            </p>
        </div>
        <a href="<%= cp %>/organizer/treks" class="btn btn-outline-secondary btn-sm">
            &larr; Back to Treks
        </a>
    </div>

    <div class="row g-4">
        <!-- Add Departure Batch Card -->
        <div class="col-lg-4">
            <div class="card shadow-sm border-0 rounded-4 sticky-top" style="top: 85px;">
                <div class="card-header bg-light py-3">
                    <h5 class="fw-bold mb-0">➕ Schedule Departure Batch</h5>
                </div>
                <div class="card-body p-4">
                    <form method="post" action="<%= cp %>/organizer/batch/save">
                        <%= H.csrf(session) %>
                        <input type="hidden" name="trekId" value="<%= trek.getTrekId() %>">

                        <div class="mb-3">
                            <label for="startDate" class="form-label fw-bold small text-muted">START / DEPARTURE DATE</label>
                            <input type="date" class="form-control future-date-only" id="startDate" name="startDate" required>
                            <div class="form-text">Cannot schedule departures in the past.</div>
                        </div>

                        <div class="mb-3">
                            <label for="pricePerPerson" class="form-label fw-bold small text-muted">STANDARD PRICE (₹ PER PERSON)</label>
                            <input type="number" class="form-control" id="pricePerPerson" name="pricePerPerson" min="100" step="50" placeholder="e.g. 8500" required>
                        </div>

                        <div class="mb-3">
                            <label for="verifiedDiscountPercent" class="form-label fw-bold small text-muted">VERIFIED TREKKER DISCOUNT (%)</label>
                            <input type="number" class="form-control" id="verifiedDiscountPercent" name="verifiedDiscountPercent" min="0" max="50" value="15" required>
                            <div class="form-text">Discount granted to Gov-ID verified trekkers (0 to 50%).</div>
                        </div>

                        <div class="mb-4">
                            <label for="totalSlots" class="form-label fw-bold small text-muted">TOTAL SLOTS CAPACITY (1-50)</label>
                            <input type="number" class="form-control" id="totalSlots" name="totalSlots" min="1" max="50" value="20" required>
                            <div class="form-text">Max capacity per mountain batch.</div>
                        </div>

                        <button type="submit" class="btn btn-tt-primary w-100 py-2 fw-bold">
                            Create Departure Batch &rarr;
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <!-- Existing Batches List -->
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="fw-bold mb-0">Scheduled Departures (<%= batches != null ? batches.size() : 0 %>)</h5>
                    <span class="small text-muted">End dates dynamically calculated (+<%= trek.getDurationDays() - 1 %> days)</span>
                </div>
                <div class="card-body p-0">
                    <% if (batches != null && !batches.isEmpty()) { %>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light small text-uppercase text-secondary">
                                    <tr>
                                        <th class="ps-4">Dates</th>
                                        <th>Price &amp; Discount</th>
                                        <th>Slots &amp; Availability</th>
                                        <th>Status</th>
                                        <th class="text-end pe-4">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Batch b : batches) {
                                        int booked = b.getBookedSlots();
                                        int total = b.getTotalSlots();
                                        int left = b.getSlotsLeft();
                                        boolean hasConfirmedBookings = booked > 0;
                                    %>
                                        <tr>
                                            <td class="ps-4 py-3">
                                                <div class="fw-bold"><%= H.dt(b.getStartDate()) %> &ndash; <%= H.dt(b.getEndDate()) %></div>
                                                <span class="small text-muted"><%= trek.getDurationDays() %> Days</span>
                                            </td>
                                            <td>
                                                <div class="fw-bold"><%= H.money(b.getPricePerPerson()) %></div>
                                                <% if (b.getVerifiedDiscountPercent() > 0) { %>
                                                    <span class="verified-badge d-inline-block mt-1">
                                                        <%= b.getVerifiedDiscountPercent() %>% Verified (<%= H.money(b.getDiscountedPrice()) %>)
                                                    </span>
                                                <% } else { %>
                                                    <span class="small text-muted">No discount</span>
                                                <% } %>
                                            </td>
                                            <td>
                                                <div>
                                                    <span class="fw-bold text-dark"><%= left %></span> / <%= total %> slots left
                                                </div>
                                                <div class="progress mt-1" style="height: 6px; width: 110px;">
                                                    <div class="progress-bar bg-success" role="progressbar"
                                                         style="width: <%= (int)(((double)booked / total) * 100) %>%;"></div>
                                                </div>
                                                <span class="small text-muted" style="font-size: 0.75rem;"><%= booked %> booked</span>
                                            </td>
                                            <td>
                                                <%= H.badge(b.getStatus()) %>
                                            </td>
                                            <td class="text-end pe-4">
                                                <% if (hasConfirmedBookings) { %>
                                                    <span class="badge bg-light text-muted border" title="Cannot delete departure with active bookings">
                                                        Has Bookings
                                                    </span>
                                                <% } else { %>
                                                    <form method="post" action="<%= cp %>/organizer/batch/delete" class="d-inline m-0">
                                                        <%= H.csrf(session) %>
                                                        <input type="hidden" name="batchId" value="<%= b.getBatchId() %>">
                                                        <input type="hidden" name="trekId" value="<%= trek.getTrekId() %>">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm"
                                                                data-confirm="Delete this departure batch? This cannot be undone.">
                                                            Delete
                                                        </button>
                                                    </form>
                                                <% } %>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } else { %>
                        <div class="p-5 text-center text-muted">
                            <div class="display-3 text-muted mb-3">📅</div>
                            <h6 class="fw-bold">No Departure Batches Created Yet</h6>
                            <p class="small text-muted mb-0">Use the form on the left to schedule your first upcoming departure dates for this trek.</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
