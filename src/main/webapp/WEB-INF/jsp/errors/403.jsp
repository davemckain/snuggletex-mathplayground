<%--

$Id$

Generic 403 Page

This may be called with a custom authentication failure message set in the
request scope by certain filters and servlets

See FrontEndCosignAuthenticationFilter

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

--%>
<%@ include file="/WEB-INF/jsp/errors/header.jspf" %>

<h1>403 Access Forbidden</h1>

<p class="error">
  ${requestScope['uk.ac.ed.ph.cst.web.errorMessage']}
</p>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
