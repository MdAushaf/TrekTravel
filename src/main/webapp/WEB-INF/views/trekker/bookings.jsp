<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">My Trek Reservations</h3>
            <p class="text-secondary small mb-0">View upcoming departure slots, tickets, and booking status</p>
        </div>
        <a href="<%= cp %>/search" class="btn btn-tt-accent btn-sm">
            ➕ Book Another Trek
        </a>
    </div>

    <!-- Cancellation Policy Note -->
    <div class="alert alert-light border small text-muted mb-4 d-flex align-items-center">
        <span class="fs-5 me-2">ℹ️</span>
        <div>
            <strong>Cancellation Rule:</strong> Reservations may be cancelled at no penalty strictly up to <strong>48 hours (2 days)</strong> before the scheduled departure date. Freed slots are instantly released back to the departure inventory.
        </div>
    </div>

    <!-- Bookings Table -->
    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (bookings != null && !bookings.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">Trek &amp; Region</th>
                                <th>Dates</th>
                                <th>Participants</th>
                                <th>Pricing Snapshot</th>
                                <th>Total Paid</th>
                                <th>Status</th>
                                <th class="text-end pe-4">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Booking b : bookings) { %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.e(b.getTrekName()) %></div>
                                        <span class="small text-muted">
                                            <%= H.e(b.getState()) %> &bull; <%= H.difficultyBadge(b.getDifficulty()) %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="fw-bold"><%= H.dt(b.getStartDate()) %> &ndash; <%= H.dt(b.getEndDate()) %></div>
                                        <span class="small text-muted"><%= b.getDurationDays() %> Days Expedition</span>
                                    </td>
                                    <td>
                                        <span class="badge bg-secondary-subtle text-secondary-emphasis">
                                            👥 <%= b.getNumParticipants() %> Slot<%= b.getNumParticipants() > 1 ? "s" : "" %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="small text-muted">Rate: <%= H.money(b.getPricePerPerson()) %>/person</div>
                                        <% if (b.getDiscountPercent() > 0) { %>
                                            <span class="verified-badge mt-1 d-inline-block">
                                                <%= b.getDiscountPercent() %>% Verified Discount
                                            </span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <div class="fw-bold fs-6 text-dark"><%= H.money(b.getTotalAmount()) %></div>
                                        <span class="small text-success">Confirmed</span>
                                    </td>
                                    <td>
                                        <%= H.badge(b.getStatus()) %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <% if (b.isConfirmed()) { %>
                                            <% if (b.canBeCancelled()) { %>
                                                <form method="post" action="<%= cp %>/trekker/cancel" class="d-inline m-0">
                                                    <%= H.csrf(session) %>
                                                    <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm"
                                                            data-confirm="Are you sure you want to cancel this booking for <%= H.e(b.getTrekName()) %>? Your reserved slots will be released immediately.">
                                                        Cancel Booking
                                                    </button>
                                                </form>
                                            <% } else { %>
                                                <span class="badge bg-light text-muted border" title="Cannot cancel within 48 hours of departure">
                                                    Non-refundable
                                                </span>
                                            <% } %>
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
                    <div class="display-1 text-muted mb-3">🎒</div>
                    <h5 class="fw-bold">No Trek Bookings Yet</h5>
                    <p class="small text-muted mb-4">You haven't reserved any departure slots. Browse open treks to start your adventure!</p>
                    <a href="<%= cp %>/search" class="btn btn-tt-primary">Explore Upcoming Departures</a>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
