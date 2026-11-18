<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    String regName = (String) request.getAttribute("name");
    String regEmail = (String) request.getAttribute("email");
    String regPhone = (String) request.getAttribute("phone");
    String regRole = (String) request.getAttribute("role");
    if (regRole == null) regRole = "TREKKER";
%>

<div class="container py-4 mb-5">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-6">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <div class="display-6 mb-2">🏔️</div>
                        <h3 class="fw-bold text-dark">Join TrekTravel</h3>
                        <p class="text-secondary small">Create an account to book treks or organize departures</p>
                    </div>

                    <form method="post" action="<%= cp %>/register" novalidate>
                        <%= H.csrf(session) %>

                        <div class="mb-3">
                            <label for="name" class="form-label fw-bold small text-muted">FULL NAME</label>
                            <input type="text" class="form-control" id="name" name="name"
                                   value="<%= H.e(regName) %>" placeholder="e.g. Rahul Sharma" required autofocus>
                        </div>

                        <div class="row g-2 mb-3">
                            <div class="col-md-6">
                                <label for="email" class="form-label fw-bold small text-muted">EMAIL ADDRESS</label>
                                <input type="email" class="form-control" id="email" name="email"
                                       value="<%= H.e(regEmail) %>" placeholder="you@domain.com" required>
                            </div>
                            <div class="col-md-6">
                                <label for="phone" class="form-label fw-bold small text-muted">PHONE NUMBER</label>
                                <input type="tel" class="form-control" id="phone" name="phone"
                                       value="<%= H.e(regPhone) %>" placeholder="10-digit mobile" required>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold small text-muted d-block">SELECT ACCOUNT TYPE</label>
                            <div class="row g-2">
                                <div class="col-6">
                                    <input type="radio" class="btn-check" name="role" id="roleTrekker" value="TREKKER"
                                           <%= "TREKKER".equalsIgnoreCase(regRole) ? "checked" : "" %>>
                                    <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center" for="roleTrekker">
                                        <span class="fs-4">🥾</span>
                                        <strong>Trekker</strong>
                                        <span class="small text-muted" style="font-size: 0.75rem;">Discover &amp; Book</span>
                                    </label>
                                </div>
                                <div class="col-6">
                                    <input type="radio" class="btn-check" name="role" id="roleOrganizer" value="ORGANIZER"
                                           <%= "ORGANIZER".equalsIgnoreCase(regRole) ? "checked" : "" %>>
                                    <label class="btn btn-outline-primary w-100 py-2 d-flex flex-column align-items-center" for="roleOrganizer">
                                        <span class="fs-4">⛺</span>
                                        <strong>Trek Operator</strong>
                                        <span class="small text-muted" style="font-size: 0.75rem;">List Departures</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div class="row g-2 mb-4">
                            <div class="col-md-6">
                                <label for="regPassword" class="form-label fw-bold small text-muted">PASSWORD</label>
                                <input type="password" class="form-control" id="regPassword" name="password"
                                       placeholder="Min 8 chars (letters+digits)" required>
                            </div>
                            <div class="col-md-6">
                                <label for="regConfirmPassword" class="form-label fw-bold small text-muted">CONFIRM PASSWORD</label>
                                <input type="password" class="form-control" id="regConfirmPassword" name="confirmPassword"
                                       placeholder="Repeat password" required>
                            </div>
                            <div id="passwordMismatchWarning" class="col-12 text-danger small d-none mt-1">
                                ⚠️ Passwords do not match!
                            </div>
                        </div>

                        <button type="submit" class="btn btn-tt-primary w-100 py-2 fw-bold mb-3">
                            Create Account &rarr;
                        </button>

                        <div class="text-center small text-secondary">
                            Already registered? <a href="<%= cp %>/login" class="fw-bold text-decoration-none">Sign in here</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
