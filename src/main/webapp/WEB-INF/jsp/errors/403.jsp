<%--

$Id: 403.jsp 1080 2010-12-13 16:59:40Z dmckain $

Generic 403 Page

This may be called with a custom authentication failure message set in the
request scope by certain filters and servlets

See FrontEndCosignAuthenticationFilter

Copyright (c) 2010, The University of Edinburgh.
All Rights Reserved

--%>
<%@ include file="/WEB-INF/jsp/errors/header.jspf" %>

<h1>403 Access Forbidden</h1>

<p class="error">
  ${requestScope['uk.ac.ed.ph.cst.web.errorMessage']}
</p>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
