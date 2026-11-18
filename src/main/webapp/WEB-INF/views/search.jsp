<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Trek> treks = (List<Trek>) request.getAttribute("treks");
    String searchLoc = (String) request.getAttribute("location");
    String searchDiff = (String) request.getAttribute("difficulty");
    Integer searchMonth = (Integer) request.getAttribute("month");
    BigDecimal searchMaxPrice = (BigDecimal) request.getAttribute("maxPrice");
    Boolean isVerifiedTrekker = (Boolean) request.getAttribute("isVerifiedTrekker");
    if (isVerifiedTrekker == null) isVerifiedTrekker = false;
%>

<div class="container mb-5">
    <!-- Search Filter Bar -->
    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body p-4 bg-light rounded">
            <h4 class="fw-bold mb-3 d-flex align-items-center gap-2">
                <span>🔍</span> Find Your Next Himalayan Trek
            </h4>
            <form method="get" action="<%= cp %>/search" class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label for="location" class="form-label fw-bold small text-muted">STATE / REGION / KEYWORD</label>
                    <input type="text" class="form-control" id="location" name="location"
                           value="<%= H.e(searchLoc) %>" placeholder="e.g. Uttarakhand, Garhwal">
                </div>
                <div class="col-md-3">
                    <label for="difficulty" class="form-label fw-bold small text-muted">DIFFICULTY</label>
                    <select class="form-select" id="difficulty" name="difficulty">
                        <option value="ALL" <%= ("ALL".equalsIgnoreCase(searchDiff) || searchDiff == null) ? "selected" : "" %>>All Difficulties</option>
                        <option value="EASY" <%= "EASY".equalsIgnoreCase(searchDiff) ? "selected" : "" %>>Easy</option>
                        <option value="MODERATE" <%= "MODERATE".equalsIgnoreCase(searchDiff) ? "selected" : "" %>>Moderate</option>
                        <option value="DIFFICULT" <%= "DIFFICULT".equalsIgnoreCase(searchDiff) ? "selected" : "" %>>Difficult</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label for="month" class="form-label fw-bold small text-muted">DEPARTURE MONTH</label>
                    <select class="form-select" id="month" name="month">
                        <option value="0" <%= (searchMonth == null || searchMonth == 0) ? "selected" : "" %>>Any Month</option>
                        <% for (int m = 1; m <= 12; m++) {
                            String mName = java.time.Month.of(m).name();
                            mName = mName.substring(0, 1).toUpperCase() + mName.substring(1).toLowerCase();
                        %>
                            <option value="<%= m %>" <%= (searchMonth != null && searchMonth == m) ? "selected" : "" %>><%= mName %></option>
                        <% } %>
                    </select>
                </div>
                <div class="col-md-2">
                    <label for="maxPrice" class="form-label fw-bold small text-muted">MAX PRICE (₹)</label>
                    <input type="number" class="form-control" id="maxPrice" name="maxPrice" min="500" step="100"
                           value="<%= searchMaxPrice != null ? searchMaxPrice : "" %>" placeholder="e.g. 10000">
                </div>
                <div class="col-md-2">
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-tt-primary w-100">Filter</button>
                        <a href="<%= cp %>/search" class="btn btn-outline-secondary">Reset</a>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Verified Trekker Discount Banner -->
    <% if (isVerifiedTrekker) { %>
        <div class="alert alert-success d-flex align-items-center mb-4" role="alert">
            <span class="fs-4 me-3">🎖️</span>
            <div>
                <strong>Verified Trekker Status Active!</strong> You are eligible for exclusive verified discounts on all departure batches. Look for the special discounted prices below!
            </div>
        </div>
    <% } else if (currentUser != null && currentUser.isTrekker()) { %>
        <div class="alert alert-warning d-flex align-items-center mb-4" role="alert">
            <span class="fs-4 me-3">💡</span>
            <div>
                <strong>Want lower prices?</strong> <a href="<%= cp %>/trekker/profile" class="alert-link">Verify your Trekker Profile</a> with a Government ID to unlock up to 25% Verified Trekker discounts.
            </div>
        </div>
    <% } %>

    <!-- Results Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="fw-bold mb-0">
            Available Departures
            <span class="badge bg-secondary ms-1"><%= (treks != null) ? treks.size() : 0 %> treks</span>
        </h5>
        <span class="text-muted small">Showing approved treks with open future departures</span>
    </div>

    <!-- Treks Grid -->
    <div class="row g-4">
        <% if (treks != null && !treks.isEmpty()) { %>
            <% for (Trek t : treks) { %>
                <div class="col-md-6 col-lg-4">
                    <div class="card trek-card">
                        <div class="trek-header d-flex justify-content-between align-items-start">
                            <div>
                                <span class="trek-location">📍 <%= H.e(t.getState()) %> &bull; <%= H.e(t.getRegion()) %></span>
                                <h5 class="trek-title mt-1 mb-0"><%= H.e(t.getName()) %></h5>
                                <span class="small text-muted">Operator: <%= H.e(t.getOrganizerName()) %></span>
                            </div>
                            <%= H.difficultyBadge(t.getDifficulty()) %>
                        </div>
                        <div class="card-body">
                            <p class="text-secondary small mb-3 flex-grow-1">
                                <%= H.e(t.getDescription().length() > 130 ? t.getDescription().substring(0, 127) + "..." : t.getDescription()) %>
                            </p>

                            <div class="d-flex justify-content-between py-2 border-top border-bottom mb-3 text-muted small">
                                <span class="trek-stat">⏱️ <%= t.getDurationDays() %> Days</span>
                                <span class="trek-stat">⛰️ <%= t.getMaxAltitudeM() %> m</span>
                                <span class="trek-stat">🥾 <%= t.getTrekDistanceKm() %> km</span>
                            </div>

                            <div class="d-flex justify-content-between align-items-center mt-auto">
                                <div>
                                    <span class="text-muted small d-block">From</span>
                                    <% if (isVerifiedTrekker && t.getMaxDiscountPercent() > 0) {
                                        BigDecimal discounted = PriceUtil.calculateDiscountedPricePerPerson(t.getMinPrice(), t.getMaxDiscountPercent());
                                    %>
                                        <span class="price-strike"><%= H.moneyShort(t.getMinPrice()) %></span>
                                        <span class="price-tag text-success"><%= H.moneyShort(discounted) %></span>
                                        <span class="verified-badge d-block mt-1">Verified: <%= t.getMaxDiscountPercent() %>% OFF</span>
                                    <% } else { %>
                                        <span class="price-tag"><%= H.moneyShort(t.getMinPrice()) %></span>
                                        <% if (t.getMaxDiscountPercent() > 0) { %>
                                            <span class="badge bg-warning-subtle text-warning-emphasis d-block mt-1">Up to <%= t.getMaxDiscountPercent() %>% off for verified</span>
                                        <% } %>
                                    <% } %>
                                </div>
                                <a href="<%= cp %>/trek?id=<%= t.getTrekId() %>" class="btn btn-tt-primary btn-sm px-3">
                                    View Dates &rarr;
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            <% } %>
        <% } else { %>
            <div class="col-12 text-center py-5">
                <div class="display-1 text-muted mb-3">🏔️</div>
                <h4 class="fw-bold">No treks matching your search criteria</h4>
                <p class="text-muted mb-4">Try adjusting your filters, location keyword, or maximum price threshold.</p>
                <a href="<%= cp %>/search" class="btn btn-tt-accent">View All Open Treks</a>
            </div>
        <% } %>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
