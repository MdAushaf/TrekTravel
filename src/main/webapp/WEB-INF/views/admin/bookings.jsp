<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">System-wide Bookings Ledger</h3>
            <p class="text-secondary small mb-0">Master audit ledger of all trek departure slot reservations across the platform</p>
        </div>
        <a href="<%= cp %>/admin/dashboard" class="btn btn-outline-secondary btn-sm">
            &larr; Back to Dashboard
        </a>
    </div>

    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (bookings != null && !bookings.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4"># &amp; Booking Date</th>
                                <th>Trekker / Account</th>
                                <th>Emergency Contact</th>
                                <th>Expedition &amp; Departures</th>
                                <th>Slots</th>
                                <th>Revenue Snapshot</th>
                                <th>Status</th>
                                <th class="text-end pe-4">Admin Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Booking b : bookings) { %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold">#<%= b.getBookingId() %></div>
                                        <span class="small text-muted">
                                            <%= H.dt(b.getCreatedAt() != null ? b.getCreatedAt().toLocalDateTime().toLocalDate() : null) %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="fw-bold"><%= H.e(b.getUserName()) %></div>
                                        <span class="small text-muted d-block">📧 <%= H.e(b.getUserEmail()) %></span>
                                        <span class="small text-muted d-block">📞 <%= H.e(b.getUserPhone()) %></span>
                                    </td>
                                    <td>
                                        <% if (b.getEmergencyContactName() != null) { %>
                                            <div class="fw-bold text-dark"><%= H.e(b.getEmergencyContactName()) %></div>
                                            <span class="small text-danger">🚨 <%= H.e(b.getEmergencyContactPhone()) %></span>
                                        <% } else { %>
                                            <span class="text-muted small">&mdash;</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <div class="fw-bold"><%= H.e(b.getTrekName()) %></div>
                                        <span class="small text-muted"><%= H.dt(b.getStartDate()) %> &ndash; <%= H.dt(b.getEndDate()) %></span>
                                    </td>
                                    <td>
                                        <span class="badge bg-secondary-subtle text-secondary-emphasis">
                                            👥 <%= b.getNumParticipants() %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="fw-bold fs-6 text-dark"><%= H.money(b.getTotalAmount()) %></div>
                                        <span class="small text-muted">Rate: <%= H.money(b.getPricePerPerson()) %></span>
                                        <% if (b.getDiscountPercent() > 0) { %>
                                            <span class="verified-badge d-block mt-1" style="font-size: 0.7rem;">
                                                <%= b.getDiscountPercent() %>% Verified Disc.
                                            </span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <%= H.badge(b.getStatus()) %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <% if (b.isConfirmed()) { %>
                                            <form method="post" action="<%= cp %>/admin/bookings/cancel" class="d-inline m-0">
                                                <%= H.csrf(session) %>
                                                <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                                                <button type="submit" class="btn btn-outline-danger btn-sm"
                                                        data-confirm="Cancel booking #<%= b.getBookingId() %> for <%= H.e(b.getUserName()) %>? Slots will be released back to the departure inventory.">
                                                    Cancel
                                                </button>
                                            </form>
                                        <% } else { %>
                                            <span class="text-muted small">&mdash;</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="p-5 text-center text-muted">
                    <div class="display-3 text-muted mb-3">📑</div>
                    <h5 class="fw-bold">No Bookings Recorded Yet</h5>
                    <p class="small text-muted mb-0">System-wide reservations will be listed here as travellers book departure slots.</p>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
