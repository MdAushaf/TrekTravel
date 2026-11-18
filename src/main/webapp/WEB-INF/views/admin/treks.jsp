<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Trek> treks = (List<Trek>) request.getAttribute("treks");
    String currentFilter = (String) request.getAttribute("currentFilter");
    if (currentFilter == null) currentFilter = "ALL";
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">Trek Expedition Approvals</h3>
            <p class="text-secondary small mb-0">Review mountain itineraries, route safety, and authorize public departures</p>
        </div>
        <a href="<%= cp %>/admin/dashboard" class="btn btn-outline-secondary btn-sm">
            &larr; Back to Dashboard
        </a>
    </div>

    <!-- Filter Tabs -->
    <ul class="nav nav-pills mb-4">
        <li class="nav-item">
            <a class="nav-link <%= "ALL".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/treks?status=ALL">All Treks</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= "PENDING".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/treks?status=PENDING">Pending Review</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= "APPROVED".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/treks?status=APPROVED">Approved</a>
        </li>
        <li class="nav-item">
            <a class="nav-link <%= "REJECTED".equalsIgnoreCase(currentFilter) ? "active" : "" %>"
               href="<%= cp %>/admin/treks?status=REJECTED">Rejected</a>
        </li>
    </ul>

    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (treks != null && !treks.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">Trek &amp; Base</th>
                                <th>Operator</th>
                                <th>Location &amp; Terrain</th>
                                <th>Difficulty &amp; Altitude</th>
                                <th>Status</th>
                                <th class="text-end pe-4">Review Decision</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Trek t : treks) { %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.e(t.getName()) %></div>
                                        <span class="small text-muted">
                                            Base: <%= H.e(t.getBaseVillage()) %> &bull; Season: <%= H.e(t.getBestSeason()) %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="fw-bold"><%= H.e(t.getOrganizerName()) %></div>
                                        <span class="small text-muted">ID #<%= t.getOrganizerId() %></span>
                                    </td>
                                    <td>
                                        <div><%= H.e(t.getState()) %></div>
                                        <span class="small text-muted"><%= H.e(t.getRegion()) %></span>
                                    </td>
                                    <td>
                                        <%= H.difficultyBadge(t.getDifficulty()) %>
                                        <div class="small text-muted mt-1">
                                            <%= t.getDurationDays() %> Days &bull; <%= t.getMaxAltitudeM() %> m &bull; <%= t.getTrekDistanceKm() %> km
                                        </div>
                                    </td>
                                    <td>
                                        <%= H.badge(t.getStatus()) %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <div class="d-flex justify-content-end gap-1">
                                            <button type="button" class="btn btn-outline-info btn-sm"
                                                    data-bs-toggle="modal" data-bs-target="#descModal<%= t.getTrekId() %>">
                                                Inspect
                                            </button>

                                            <% if (!t.isApproved()) { %>
                                                <form method="post" action="<%= cp %>/admin/treks/review" class="d-inline m-0">
                                                    <%= H.csrf(session) %>
                                                    <input type="hidden" name="trekId" value="<%= t.getTrekId() %>">
                                                    <input type="hidden" name="decision" value="APPROVED">
                                                    <button type="submit" class="btn btn-success btn-sm"
                                                            data-confirm="Approve <%= H.e(t.getName()) %> for public booking?">
                                                        Approve
                                                    </button>
                                                </form>
                                            <% } %>

                                            <% if (!t.isRejected()) { %>
                                                <form method="post" action="<%= cp %>/admin/treks/review" class="d-inline m-0">
                                                    <%= H.csrf(session) %>
                                                    <input type="hidden" name="trekId" value="<%= t.getTrekId() %>">
                                                    <input type="hidden" name="decision" value="REJECTED">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm"
                                                            data-confirm="Reject or suspend <%= H.e(t.getName()) %>?">
                                                        Reject
                                                    </button>
                                                </form>
                                            <% } %>
                                        </div>

                                        <!-- Detail Inspection Modal -->
                                        <div class="modal fade" id="descModal<%= t.getTrekId() %>" tabindex="-1" aria-hidden="true">
                                            <div class="modal-dialog modal-lg modal-dialog-centered text-start">
                                                <div class="modal-content">
                                                    <div class="modal-header bg-light">
                                                        <h5 class="modal-title fw-bold">Trek Specs: <%= H.e(t.getName()) %></h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body p-4">
                                                        <h6 class="fw-bold text-dark">Description</h6>
                                                        <p class="text-secondary small leading-relaxed mb-3"><%= H.e(t.getDescription()) %></p>

                                                        <h6 class="fw-bold text-dark">Highlights</h6>
                                                        <p class="text-secondary small mb-3"><%= H.e(t.getHighlights()) %></p>

                                                        <h6 class="fw-bold text-dark">Inclusions &amp; Safety Equipment</h6>
                                                        <p class="text-secondary small mb-0"><%= H.e(t.getInclusions()) %></p>
                                                    </div>
                                                    <div class="modal-footer bg-light">
                                                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Close</button>
                                                    </div>
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
                    <div class="display-3 text-muted mb-3">🏔️</div>
                    <h5 class="fw-bold">No Treks Found</h5>
                    <p class="small text-muted mb-0">No treks match the selected "<%= H.e(currentFilter) %>" filter.</p>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
