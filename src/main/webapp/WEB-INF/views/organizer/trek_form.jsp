<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    Trek trek = (Trek) request.getAttribute("trek");
    boolean isEdit = (trek != null && trek.getTrekId() > 0);
%>

<div class="container py-3 mb-5">
    <div class="row justify-content-center">
        <div class="col-lg-9">
            <!-- Header -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h3 class="fw-bold mb-1"><%= isEdit ? "Edit Trek Listing" : "List New Trek Expedition" %></h3>
                    <p class="text-secondary small mb-0">Provide complete route specifications, terrain difficulty, and logistics</p>
                </div>
                <a href="<%= cp %>/organizer/treks" class="btn btn-outline-secondary btn-sm">
                    &larr; Back to Treks
                </a>
            </div>

            <% if (isEdit && trek.isApproved()) { %>
                <div class="alert alert-warning d-flex align-items-center mb-4" role="alert">
                    <span class="fs-4 me-2">⚠️</span>
                    <div>
                        <strong>Notice:</strong> Modifying details of this currently APPROVED trek will reset its status to <strong>PENDING</strong> for administrator re-verification before changes go live publicly.
                    </div>
                </div>
            <% } %>

            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-body p-4 p-md-5">
                    <form method="post" action="<%= cp %>/organizer/trek/save" novalidate>
                        <%= H.csrf(session) %>
                        <% if (isEdit) { %>
                            <input type="hidden" name="trekId" value="<%= trek.getTrekId() %>">
                        <% } %>

                        <h5 class="fw-bold text-dark mb-3 border-bottom pb-2">1. Trek Identity &amp; Location</h5>
                        <div class="row g-3 mb-4">
                            <div class="col-md-12">
                                <label for="name" class="form-label fw-bold small text-muted">TREK NAME</label>
                                <input type="text" class="form-control" id="name" name="name"
                                       value="<%= isEdit ? H.e(trek.getName()) : "" %>" placeholder="e.g. Kedarkantha Winter Trek" required>
                            </div>
                            <div class="col-md-4">
                                <label for="state" class="form-label fw-bold small text-muted">STATE</label>
                                <input type="text" class="form-control" id="state" name="state"
                                       value="<%= isEdit ? H.e(trek.getState()) : "" %>" placeholder="e.g. Uttarakhand, Himachal Pradesh" required>
                            </div>
                            <div class="col-md-4">
                                <label for="region" class="form-label fw-bold small text-muted">REGION / RANGE</label>
                                <input type="text" class="form-control" id="region" name="region"
                                       value="<%= isEdit ? H.e(trek.getRegion()) : "" %>" placeholder="e.g. Garhwal Himalaya, Pir Panjal" required>
                            </div>
                            <div class="col-md-4">
                                <label for="baseVillage" class="form-label fw-bold small text-muted">BASE VILLAGE (START POINT)</label>
                                <input type="text" class="form-control" id="baseVillage" name="baseVillage"
                                       value="<%= isEdit ? H.e(trek.getBaseVillage()) : "" %>" placeholder="e.g. Sankri, Jobra, Barshaini" required>
                            </div>
                        </div>

                        <h5 class="fw-bold text-dark mb-3 border-bottom pb-2">2. Route Specs &amp; Difficulty</h5>
                        <div class="row g-3 mb-4">
                            <div class="col-md-3">
                                <label for="difficulty" class="form-label fw-bold small text-muted">TERRAIN DIFFICULTY</label>
                                <select class="form-select" id="difficulty" name="difficulty" required>
                                    <option value="EASY" <%= (isEdit && "EASY".equalsIgnoreCase(trek.getDifficulty())) ? "selected" : "" %>>Easy (Beginner)</option>
                                    <option value="MODERATE" <%= (isEdit && "MODERATE".equalsIgnoreCase(trek.getDifficulty())) ? "selected" : "" %>>Moderate (Active)</option>
                                    <option value="DIFFICULT" <%= (isEdit && "DIFFICULT".equalsIgnoreCase(trek.getDifficulty())) ? "selected" : "" %>>Difficult (Technical)</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label for="durationDays" class="form-label fw-bold small text-muted">DURATION (DAYS, 1-30)</label>
                                <input type="number" class="form-control" id="durationDays" name="durationDays" min="1" max="30"
                                       value="<%= isEdit ? trek.getDurationDays() : "6" %>" required>
                            </div>
                            <div class="col-md-3">
                                <label for="maxAltitudeM" class="form-label fw-bold small text-muted">MAX ALTITUDE (METERS)</label>
                                <input type="number" class="form-control" id="maxAltitudeM" name="maxAltitudeM" min="0" step="10"
                                       value="<%= isEdit ? trek.getMaxAltitudeM() : "3810" %>" required>
                            </div>
                            <div class="col-md-3">
                                <label for="trekDistanceKm" class="form-label fw-bold small text-muted">DISTANCE (KM)</label>
                                <input type="number" class="form-control" id="trekDistanceKm" name="trekDistanceKm" min="0" step="0.5"
                                       value="<%= isEdit ? trek.getTrekDistanceKm() : "20.0" %>" required>
                            </div>
                        </div>

                        <h5 class="fw-bold text-dark mb-3 border-bottom pb-2">3. Itinerary, Inclusions &amp; Description</h5>
                        <div class="row g-3 mb-4">
                            <div class="col-md-12">
                                <label for="bestSeason" class="form-label fw-bold small text-muted">BEST SEASON</label>
                                <input type="text" class="form-control" id="bestSeason" name="bestSeason"
                                       value="<%= isEdit ? H.e(trek.getBestSeason()) : "" %>" placeholder="e.g. December to April, June to October" required>
                            </div>
                            <div class="col-md-12">
                                <label for="highlights" class="form-label fw-bold small text-muted">TRAIL HIGHLIGHTS (COMMA-SEPARATED)</label>
                                <input type="text" class="form-control" id="highlights" name="highlights"
                                       value="<%= isEdit ? H.e(trek.getHighlights()) : "" %>" placeholder="e.g. Juda Ka Talab, Summit Sunrise, Dense Pine Forests" required>
                            </div>
                            <div class="col-md-12">
                                <label for="inclusions" class="form-label fw-bold small text-muted">INCLUSIONS &amp; LOGISTICS</label>
                                <input type="text" class="form-control" id="inclusions" name="inclusions"
                                       value="<%= isEdit ? H.e(trek.getInclusions()) : "" %>" placeholder="e.g. Certified Trek Leader, Meals, Tent Stay, Forest Permits" required>
                            </div>
                            <div class="col-md-12">
                                <label for="description" class="form-label fw-bold small text-muted">FULL EXPEDITION DESCRIPTION</label>
                                <textarea class="form-control" id="description" name="description" rows="5"
                                          placeholder="Detailed overview of the trail, terrain, base camp logistics, and summit day..." required><%= isEdit ? H.e(trek.getDescription()) : "" %></textarea>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                            <a href="<%= cp %>/organizer/treks" class="btn btn-outline-secondary">Cancel</a>
                            <button type="submit" class="btn btn-tt-primary px-4 py-2 fw-bold">
                                <%= isEdit ? "Save Changes &amp; Submit for Review" : "Publish Trek for Review" %>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
