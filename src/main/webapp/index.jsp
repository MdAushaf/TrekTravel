<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Forward directly to PublicServlet root mapping
    request.getRequestDispatcher("/").forward(request, response);
%>
