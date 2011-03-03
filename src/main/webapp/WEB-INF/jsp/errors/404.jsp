<%--

$Id: 404.jsp 1080 2010-12-13 16:59:40Z dmckain $

404 Page

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

--%>
<%@ include file="/WEB-INF/jsp/errors/header.jspf" %>

<h1>404 Not Found</h1>
<p class="error">
  The requested resource at <tt>${pageContext.errorData.requestURI}</tt> could not be found.
</p>

<h1>Help!?</h1>
<ul>
  <li>
    If you followed a link to this page, please contact
    <a href="http://www.ph.ed.ac.uk/elearning/contacts/#dmckain">David McKain</a>
    to report this broken link.
  </li>
</ul>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
