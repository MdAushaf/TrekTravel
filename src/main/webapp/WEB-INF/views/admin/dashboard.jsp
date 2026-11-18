<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    Integer pendingProfiles = (Integer) request.getAttribute("pendingProfiles");
    Integer pendingTreks = (Integer) request.getAttribute("pendingTreks");
    Integer totalUsers = (Integer) request.getAttribute("totalUsers");
    Integer confirmedBookings = (Integer) request.getAttribute("confirmedBookings");
    BigDecimal totalRevenue = (BigDecimal) request.getAttribute("totalRevenue");

    if (pendingProfiles == null) pendingProfiles = 0;
    if (pendingTreks == null) pendingTreks = 0;
    if (totalUsers == null) totalUsers = 0;
    if (confirmedBookings == null) confirmedBookings = 0;
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">System Administration Dashboard</h3>
            <p class="text-secondary small mb-0">Overview of verification queues, operator listings, platform metrics, and revenue</p>
        </div>
        <span class="badge bg-dark px-3 py-2">
            Administrator Portal
        </span>
    </div>

    <!-- Alert for Pending Items -->
    <% if (pendingProfiles > 0 || pendingTreks > 0) { %>
        <div class="alert alert-warning d-flex align-items-center justify-content-between mb-4 rounded-3" role="alert">
            <div class="d-flex align-items-center">
                <span class="fs-3 me-3">🔔</span>
                <div>
                    <strong>Action Required:</strong>
                    You have
                    <% if (pendingProfiles > 0) { %>
                        <strong><%= pendingProfiles %></strong> pending trekker ID verification(s)
                    <% } %>
                    <% if (pendingProfiles > 0 && pendingTreks > 0) { %> and <% } %>
                    <% if (pendingTreks > 0) { %>
                        <strong><%= pendingTreks %></strong> pending trek listing(s)
                    <% } %>
                    awaiting approval.
                </div>
            </div>
            <div class="d-flex gap-2">
                <% if (pendingProfiles > 0) { %>
                    <a href="<%= cp %>/admin/profiles" class="btn btn-warning btn-sm fw-bold">Review IDs</a>
                <% } %>
                <% if (pendingTreks > 0) { %>
                    <a href="<%= cp %>/admin/treks" class="btn btn-warning btn-sm fw-bold">Review Treks</a>
                <% } %>
            </div>
        </div>
    <% } %>

    <!-- 5 Key Metrics -->
    <div class="row g-4 mb-4">
        <div class="col-md-4 col-lg">
            <div class="metric-box h-100">
                <div class="metric-number text-warning"><%= pendingProfiles %></div>
                <div class="metric-label">Pending Profiles</div>
                <a href="<%= cp %>/admin/profiles" class="small text-decoration-none mt-2 d-inline-block">Open Review Queue &rarr;</a>
            </div>
        </div>
        <div class="col-md-4 col-lg">
            <div class="metric-box h-100">
                <div class="metric-number text-primary"><%= pendingTreks %></div>
                <div class="metric-label">Pending Treks</div>
                <a href="<%= cp %>/admin/treks" class="small text-decoration-none mt-2 d-inline-block">Open Trek Approvals &rarr;</a>
            </div>
        </div>
        <div class="col-md-4 col-lg">
            <div class="metric-box h-100">
                <div class="metric-number text-dark"><%= totalUsers %></div>
                <div class="metric-label">Registered Users</div>
                <a href="<%= cp %>/admin/users" class="small text-decoration-none mt-2 d-inline-block">Manage Users &rarr;</a>
            </div>
        </div>
        <div class="col-md-6 col-lg">
            <div class="metric-box h-100">
                <div class="metric-number text-info"><%= confirmedBookings %></div>
                <div class="metric-label">Confirmed Bookings</div>
                <a href="<%= cp %>/admin/bookings" class="small text-decoration-none mt-2 d-inline-block">View Ledger &rarr;</a>
            </div>
        </div>
        <div class="col-md-6 col-lg">
            <div class="metric-box h-100">
                <div class="metric-number text-success"><%= H.money(totalRevenue) %></div>
                <div class="metric-label">Total Platform Revenue</div>
                <span class="small text-muted mt-2 d-inline-block">Confirmed reservations</span>
            </div>
        </div>
    </div>

    <!-- Quick Navigation Hub -->
    <div class="row g-4">
        <div class="col-md-6">
            <div class="card shadow-sm border-0 rounded-4 h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="fw-bold mb-0">🛡️ Verification &amp; Safety Controls</h5>
                </div>
                <div class="card-body p-4">
                    <p class="text-secondary small mb-3">
                        Review uploaded Government ID documents from trekkers. Approved trekkers gain verified status and receive automatic discounts on departure batches.
                    </p>
                    <div class="d-flex gap-2">
                        <a href="<%= cp %>/admin/profiles?status=PENDING" class="btn btn-tt-primary btn-sm">
                            Pending IDs (<%= pendingProfiles %>)
                        </a>
                        <a href="<%= cp %>/admin/profiles?status=ALL" class="btn btn-outline-secondary btn-sm">
                            All Profiles
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-6">
            <div class="card shadow-sm border-0 rounded-4 h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="fw-bold mb-0">🏔️ Expedition Listings &amp; Inventory</h5>
                </div>
                <div class="card-body p-4">
                    <p class="text-secondary small mb-3">
                        Inspect new trek itineraries submitted by organizers. Treks must be verified for safety and trail specifications before opening to public booking.
                    </p>
                    <div class="d-flex gap-2">
                        <a href="<%= cp %>/admin/treks?status=PENDING" class="btn btn-tt-primary btn-sm">
                            Pending Treks (<%= pendingTreks %>)
                        </a>
                        <a href="<%= cp %>/admin/treks?status=ALL" class="btn btn-outline-secondary btn-sm">
                            All Treks
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
