<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="jakarta.servlet.RequestDispatcher" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (statusCode == null) {
        statusCode = response.getStatus();
    }
    String errorExceptionMessage = null;
    Throwable exceptionObj = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    if (exceptionObj != null) {
        errorExceptionMessage = exceptionObj.getMessage();
    }

    String title = "Unexpected Error";
    String description = "An error occurred while processing your request. Please return to the homepage or try again.";
    String icon = "⚠️";

    if (statusCode != null) {
        switch (statusCode) {
            case 403:
                title = "403 &mdash; Access Denied";
                description = "You do not have permission to access the requested resource or invalid CSRF token.";
                icon = "🚫";
                break;
            case 404:
                title = "404 &mdash; Page Not Found";
                description = "The trek, departure batch, or page you were looking for could not be found.";
                icon = "🏔️";
                break;
            case 405:
                title = "405 &mdash; Method Not Allowed";
                description = "The HTTP method used for this request is not supported for this URL endpoint.";
                icon = "⛔";
                break;
            case 500:
                title = "500 &mdash; Server Error";
                description = "Our mountain expedition server encountered an internal error. Please check system logs.";
                icon = "⛈️";
                break;
        }
    }
%>

<div class="container py-5 text-center my-4">
    <div class="display-1 mb-3"><%= icon %></div>
    <h2 class="fw-bold mb-3"><%= title %></h2>
    <p class="text-secondary lead mx-auto mb-4" style="max-width: 550px;">
        <%= description %>
    </p>

    <% if (errorExceptionMessage != null && !errorExceptionMessage.isEmpty()) { %>
        <div class="alert alert-secondary d-inline-block small text-start mb-4">
            <code><%= H.e(errorExceptionMessage) %></code>
        </div>
    <% } %>

    <div>
        <a href="<%= cp %>/" class="btn btn-tt-primary px-4 py-2">
            Return to Homepage
        </a>
        <a href="<%= cp %>/search" class="btn btn-outline-secondary px-4 py-2 ms-2">
            Explore Treks
        </a>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
