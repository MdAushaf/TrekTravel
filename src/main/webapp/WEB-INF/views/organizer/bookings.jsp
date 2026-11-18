<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">Participant Roster &amp; Bookings</h3>
            <p class="text-secondary small mb-0">Manage participant manifest and emergency contacts for your expeditions</p>
        </div>
        <a href="<%= cp %>/organizer/treks" class="btn btn-outline-secondary btn-sm">
            &larr; Back to Treks
        </a>
    </div>

    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (bookings != null && !bookings.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">Trek &amp; Dates</th>
                                <th>Trekker Info</th>
                                <th>Emergency Contact</th>
                                <th>Slots</th>
                                <th>Total Revenue</th>
                                <th>Status</th>
                                <th class="text-end pe-4">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Booking b : bookings) { %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.e(b.getTrekName()) %></div>
                                        <span class="small text-muted"><%= H.dt(b.getStartDate()) %> &ndash; <%= H.dt(b.getEndDate()) %></span>
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
                                            <span class="text-muted small">Not provided</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <span class="badge bg-secondary-subtle text-secondary-emphasis">
                                            👥 <%= b.getNumParticipants() %> Slot<%= b.getNumParticipants() > 1 ? "s" : "" %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="fw-bold fs-6 text-dark"><%= H.money(b.getTotalAmount()) %></div>
                                        <% if (b.getDiscountPercent() > 0) { %>
                                            <span class="verified-badge d-inline-block" style="font-size: 0.7rem;">
                                                <%= b.getDiscountPercent() %>% Verified
                                            </span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <%= H.badge(b.getStatus()) %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <% if (b.isConfirmed()) { %>
                                            <form method="post" action="<%= cp %>/organizer/booking/cancel" class="d-inline m-0">
                                                <%= H.csrf(session) %>
                                                <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                                                <button type="submit" class="btn btn-outline-danger btn-sm"
                                                        data-confirm="Cancel this reservation for <%= H.e(b.getUserName()) %>? Slots will be reopened.">
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
                    <div class="display-3 text-muted mb-3">📋</div>
                    <h5 class="fw-bold">No Participant Bookings Yet</h5>
                    <p class="small text-muted mb-0">When travellers book departure slots on your treks, participant records and emergency contacts will appear here.</p>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
