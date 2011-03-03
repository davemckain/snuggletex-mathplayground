<%--

$Id: 500.jsp 1080 2010-12-13 16:59:40Z dmckain $

500 Page

(Unfortunately, this has got JSP scriptlets in it.... yuck!)

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

--%>
<%@ page import="java.io.PrintWriter" %>
<%@ include file="/WEB-INF/jsp/errors/header.jspf" %>

<h1>500 Internal Server Error</h1>
<p class="error">
  This tool has encountered a software problem while trying to
  process your request.
</p>
<p>
  This error has been logged and will be inspected by the web administrator
  in due course.
</p>

<h1>Error Information</h1>

<%
  Throwable ex = pageContext.getErrorData().getThrowable();
  PrintWriter writer = new PrintWriter(out);
%><pre><% ex.printStackTrace(writer); %></pre><%
  if (ex instanceof ServletException) {
    ex = ((ServletException) ex).getRootCause();
    while (ex!=null) {
%><h1>Caused by:</h1>
<pre><% ex.printStackTrace(writer); %></pre><%
      ex = ex.getCause();
    }
  }
%>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
