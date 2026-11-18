<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    String emailVal = (String) request.getAttribute("email");
%>

<div class="container py-4 mb-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card shadow-sm border-0 rounded-4">
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <div class="display-6 mb-2">🏔️</div>
                        <h3 class="fw-bold text-dark">Welcome Back</h3>
                        <p class="text-secondary small">Log in to manage trek departures, bookings, or profiles</p>
                    </div>

                    <form method="post" action="<%= cp %>/login" novalidate>
                        <%= H.csrf(session) %>

                        <div class="mb-3">
                            <label for="email" class="form-label fw-bold small text-muted">EMAIL ADDRESS</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   value="<%= H.e(emailVal) %>" placeholder="you@domain.com" required autofocus>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label fw-bold small text-muted">PASSWORD</label>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="••••••••" required>
                        </div>

                        <button type="submit" class="btn btn-tt-primary w-100 py-2 fw-bold mb-3">
                            Sign In &rarr;
                        </button>

                        <div class="text-center small text-secondary">
                            Don't have an account? <a href="<%= cp %>/register" class="fw-bold text-decoration-none">Register here</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Demo Login Assistant Quick Cards -->
            <div class="card mt-4 border-0 bg-white shadow-sm rounded-4">
                <div class="card-body p-3">
                    <h6 class="fw-bold small text-muted mb-2 d-flex align-items-center gap-1">
                        <span>🔑</span> DEMO CREDENTIALS (CLICK TO AUTOFILL)
                    </h6>
                    <div class="d-flex flex-wrap gap-2">
                        <button type="button" class="btn btn-light btn-sm border small"
                                onclick="fillLogin('admin@trektravel.com', 'Admin@123')">
                            <strong>Admin</strong> (admin@trektravel.com)
                        </button>
                        <button type="button" class="btn btn-light btn-sm border small"
                                onclick="fillLogin('organizer@trektravel.com', 'Organizer@123')">
                            <strong>Organizer</strong> (organizer@trektravel.com)
                        </button>
                        <button type="button" class="btn btn-light btn-sm border small"
                                onclick="fillLogin('trekker@trektravel.com', 'Trekker@123')">
                            <strong>Verified Trekker</strong> (trekker@trektravel.com)
                        </button>
                        <button type="button" class="btn btn-light btn-sm border small"
                                onclick="fillLogin('newbie@trektravel.com', 'Trekker@123')">
                            <strong>Pending Trekker</strong> (newbie@trektravel.com)
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function fillLogin(email, password) {
    document.getElementById("email").value = email;
    document.getElementById("password").value = password;
}
</script>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
