<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
%>

<div class="container py-3 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">User Account Management</h3>
            <p class="text-secondary small mb-0">View registered accounts and manage security suspension controls</p>
        </div>
        <a href="<%= cp %>/admin/dashboard" class="btn btn-outline-secondary btn-sm">
            &larr; Back to Dashboard
        </a>
    </div>

    <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
            <% if (users != null && !users.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-uppercase text-secondary">
                            <tr>
                                <th class="ps-4">User Details</th>
                                <th>Contact Number</th>
                                <th>Account Role</th>
                                <th>Registered Date</th>
                                <th>Account Status</th>
                                <th class="text-end pe-4">Access Control</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (User u : users) {
                                boolean isSelf = (currentUser != null && u.getId() == currentUser.getId());
                                boolean isProtectedAdmin = u.isAdmin();
                            %>
                                <tr>
                                    <td class="ps-4 py-3">
                                        <div class="fw-bold"><%= H.e(u.getName()) %></div>
                                        <span class="small text-muted"><%= H.e(u.getEmail()) %></span>
                                    </td>
                                    <td>
                                        <span><%= H.e(u.getPhone()) %></span>
                                    </td>
                                    <td>
                                        <span class="badge bg-secondary-subtle text-secondary-emphasis">
                                            <%= H.e(u.getRole()) %>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="small text-muted">
                                            <%= H.dt(u.getCreatedAt() != null ? u.getCreatedAt().toLocalDateTime().toLocalDate() : null) %>
                                        </span>
                                    </td>
                                    <td>
                                        <%= H.badge(u.getStatus()) %>
                                    </td>
                                    <td class="text-end pe-4">
                                        <% if (isSelf) { %>
                                            <span class="badge bg-light text-muted border">You (Logged In)</span>
                                        <% } else if (isProtectedAdmin) { %>
                                            <span class="badge bg-light text-muted border">System Admin</span>
                                        <% } else if (u.isActive()) { %>
                                            <form method="post" action="<%= cp %>/admin/users/status" class="d-inline m-0">
                                                <%= H.csrf(session) %>
                                                <input type="hidden" name="userId" value="<%= u.getId() %>">
                                                <input type="hidden" name="newStatus" value="BLOCKED">
                                                <button type="submit" class="btn btn-outline-danger btn-sm"
                                                        data-confirm="Block user account for <%= H.e(u.getName()) %>? They will be immediately logged out and forbidden from accessing the platform.">
                                                    Block User
                                                </button>
                                            </form>
                                        <% } else { %>
                                            <form method="post" action="<%= cp %>/admin/users/status" class="d-inline m-0">
                                                <%= H.csrf(session) %>
                                                <input type="hidden" name="userId" value="<%= u.getId() %>">
                                                <input type="hidden" name="newStatus" value="ACTIVE">
                                                <button type="submit" class="btn btn-success btn-sm"
                                                        data-confirm="Unblock and restore active access for <%= H.e(u.getName()) %>?">
                                                    Unblock User
                                                </button>
                                            </form>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="p-5 text-center text-muted">
                    <p class="mb-0">No users found in the system.</p>
                </div>
            <% } %>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
