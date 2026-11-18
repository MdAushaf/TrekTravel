<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<Trek> treks = (List<Trek>) request.getAttribute("treks");
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">My Trek Listings</h3>
            <p class="text-secondary small mb-0">Manage expedition routes, itineraries, and departure batches</p>
        </div>
        <a href="<%= cp %>/organizer/trek/form" class="btn btn-tt-accent btn-sm">
            ➕ List New Trek
        </a>
    </div>

    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (treks != null && !treks.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">Trek Name &amp; Base</th>
                                <th>Location</th>
                                <th>Difficulty</th>
                                <th>Duration &amp; Distance</th>
                                <th>Batches</th>
                                <th>Status</th>
                                <th class="text-end pe-4">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Trek t : treks) { %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.e(t.getName()) %></div>
                                        <span class="small text-muted">Base: <%= H.e(t.getBaseVillage()) %> &bull; Season: <%= H.e(t.getBestSeason()) %></span>
                                    </td>
                                    <td>
                                        <div><%= H.e(t.getState()) %></div>
                                        <span class="small text-muted"><%= H.e(t.getRegion()) %></span>
                                    </td>
                                    <td>
                                        <%= H.difficultyBadge(t.getDifficulty()) %>
                                    </td>
                                    <td>
                                        <div><%= t.getDurationDays() %> Days &bull; <%= t.getTrekDistanceKm() %> km</div>
                                        <span class="small text-muted">Max Alt: <%= t.getMaxAltitudeM() %> m</span>
                                    </td>
                                    <td>
                                        <a href="<%= cp %>/organizer/batches?trekId=<%= t.getTrekId() %>" class="btn btn-sm btn-outline-secondary">
                                            📅 Batches (<%= t.getOpenBatchesCount() %>)
                                        </a>
                                    </td>
                                    <td>
                                        <%= H.badge(t.getStatus()) %>
                                        <% if (t.isPending()) { %>
                                            <span class="small text-muted d-block" style="font-size: 0.75rem;">Under Admin Review</span>
                                        <% } %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <div class="d-flex justify-content-end gap-1">
                                            <a href="<%= cp %>/organizer/trek/form?id=<%= t.getTrekId() %>" class="btn btn-outline-primary btn-sm">
                                                Edit
                                            </a>
                                            <form method="post" action="<%= cp %>/organizer/trek/delete" class="d-inline m-0">
                                                <%= H.csrf(session) %>
                                                <input type="hidden" name="trekId" value="<%= t.getTrekId() %>">
                                                <button type="submit" class="btn btn-outline-danger btn-sm"
                                                        data-confirm="Are you sure you want to delete this trek? This action cannot be undone.">
                                                    Delete
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="p-5 text-center text-muted">
                    <div class="display-1 text-muted mb-3">⛺</div>
                    <h5 class="fw-bold">No Treks Listed Yet</h5>
                    <p class="small text-muted mb-4">Start publishing your high-altitude itineraries to welcome trekkers across India!</p>
                    <a href="<%= cp %>/organizer/trek/form" class="btn btn-tt-primary">List Your First Trek</a>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
