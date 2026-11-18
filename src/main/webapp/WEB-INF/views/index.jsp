<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Trek> featured = (List<Trek>) request.getAttribute("featuredTreks");
%>

<!-- Hero Section -->
<div class="container mb-5">
    <div class="hero-banner p-4 p-md-5">
        <div class="row align-items-center">
            <div class="col-lg-8">
                <span class="badge bg-warning text-dark px-3 py-2 fw-bold text-uppercase mb-3">
                    🏔️ Himalayan Departures 2026
                </span>
                <h1 class="display-4 fw-bold mb-3">Climb Higher. Travel Smarter.</h1>
                <p class="lead mb-4">
                    Discover India's most breathtaking trails — from the snow-covered summit of Kedarkantha to the alpine passes of Hampta. Book verified departure batches directly with certified organizers.
                </p>
                <div class="d-flex flex-wrap gap-2">
                    <a href="<%= cp %>/search" class="btn btn-tt-accent btn-lg px-4">Find Upcoming Treks</a>
                    <a href="<%= cp %>/register" class="btn btn-outline-light btn-lg px-4">Register as Trekker</a>
                </div>
            </div>
            <div class="col-lg-4 d-none d-lg-block text-center">
                <div class="p-4 bg-white bg-opacity-10 rounded-4 border border-white border-opacity-25 backdrop-blur">
                    <div class="display-3 mb-2">⛺</div>
                    <h5 class="text-white fw-bold">Verified Trekker Perks</h5>
                    <p class="small text-white-50 mb-0">
                        Verify your Government ID to unlock exclusive up to 25% verified discounts across all upcoming departure batches.
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Search Form Section -->
<div class="container mb-5">
    <div class="search-card">
        <form method="get" action="<%= cp %>/search" class="row g-3 align-items-end">
            <div class="col-md-4">
                <label for="location" class="form-label fw-bold small text-muted">STATE / REGION / TREK NAME</label>
                <div class="input-group">
                    <span class="input-group-text bg-light">📍</span>
                    <input type="text" class="form-control" id="location" name="location" placeholder="e.g. Uttarakhand, Himachal, Kedarkantha">
                </div>
            </div>
            <div class="col-md-3">
                <label for="difficulty" class="form-label fw-bold small text-muted">DIFFICULTY</label>
                <select class="form-select" id="difficulty" name="difficulty">
                    <option value="ALL">All Difficulties</option>
                    <option value="EASY">Easy (Beginner friendly)</option>
                    <option value="MODERATE">Moderate (Active hikers)</option>
                    <option value="DIFFICULT">Difficult (Experienced)</option>
                </select>
            </div>
            <div class="col-md-2">
                <label for="month" class="form-label fw-bold small text-muted">DEPARTURE MONTH</label>
                <select class="form-select" id="month" name="month">
                    <option value="0">Any Month</option>
                    <option value="1">January</option>
                    <option value="2">February</option>
                    <option value="3">March</option>
                    <option value="4">April</option>
                    <option value="5">May</option>
                    <option value="6">June</option>
                    <option value="7">July</option>
                    <option value="8">August</option>
                    <option value="9">September</option>
                    <option value="10">October</option>
                    <option value="11">November</option>
                    <option value="12">December</option>
                </select>
            </div>
            <div class="col-md-3">
                <button type="submit" class="btn btn-tt-primary w-100 py-2">
                    🔍 Search Departures
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Featured Treks Grid -->
<div class="container mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1">Featured Mountain Treks</h2>
            <p class="text-muted small mb-0">Verified expeditions with open departure dates scheduled</p>
        </div>
        <a href="<%= cp %>/search" class="btn btn-outline-secondary btn-sm">View All Treks &rarr;</a>
    </div>

    <div class="row g-4">
        <% if (featured != null && !featured.isEmpty()) { %>
            <% for (Trek t : featured) { %>
                <div class="col-md-6 col-lg-4">
                    <div class="card trek-card">
                        <div class="trek-header d-flex justify-content-between align-items-start">
                            <div>
                                <span class="trek-location">📍 <%= H.e(t.getState()) %>, <%= H.e(t.getRegion()) %></span>
                                <h5 class="trek-title mt-1 mb-0"><%= H.e(t.getName()) %></h5>
                            </div>
                            <%= H.difficultyBadge(t.getDifficulty()) %>
                        </div>
                        <div class="card-body">
                            <p class="text-secondary small mb-3 flex-grow-1">
                                <%= H.e(t.getDescription().length() > 140 ? t.getDescription().substring(0, 137) + "..." : t.getDescription()) %>
                            </p>

                            <div class="d-flex justify-content-between py-2 border-top border-bottom mb-3 text-muted small">
                                <span class="trek-stat">⏱️ <%= t.getDurationDays() %> Days</span>
                                <span class="trek-stat">⛰️ <%= t.getMaxAltitudeM() %> m</span>
                                <span class="trek-stat">🥾 <%= t.getTrekDistanceKm() %> km</span>
                            </div>

                            <div class="d-flex justify-content-between align-items-center mt-auto">
                                <div>
                                    <span class="text-muted small d-block">Starting from</span>
                                    <span class="price-tag"><%= H.moneyShort(t.getMinPrice()) %></span>
                                    <span class="text-muted small">/ person</span>
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
                <p class="text-muted">No open trek departures listed currently. Check back soon!</p>
            </div>
        <% } %>
    </div>
</div>

<!-- Platform Highlights Section -->
<div class="container mb-5 py-4">
    <div class="row g-4 text-center">
        <div class="col-md-4">
            <div class="metric-box h-100">
                <div class="display-5 mb-2">🛡️</div>
                <h5 class="fw-bold">Zero Overbooking Guarantee</h5>
                <p class="text-muted small mb-0">
                    Proprietary row-level transaction locking ensures slots booked are guaranteed. Live batch inventory strictly maintained.
                </p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="metric-box h-100">
                <div class="display-5 mb-2">🪪</div>
                <h5 class="fw-bold">Verified Trekker Savings</h5>
                <p class="text-muted small mb-0">
                    Trekkers with verified Government ID proofs and emergency contacts qualify for automatic discounts on every departure.
                </p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="metric-box h-100">
                <div class="display-5 mb-2">🏕️</div>
                <h5 class="fw-bold">Certified Mountain Operators</h5>
                <p class="text-muted small mb-0">
                    Every trek itinerary, safety equipment list, and meal plan is reviewed and approved by platform administrators.
                </p>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
