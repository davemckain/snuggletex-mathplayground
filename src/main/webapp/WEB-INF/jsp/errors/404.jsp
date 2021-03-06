<%--

$Id$

404 Page

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

--%>
<%@ include file="/WEB-INF/jsp/errors/header.jspf" %>

<h1>404 Not Found</h1>
<p class="error">
  The requested resource at <code>${pageContext.errorData.requestURI}</code> could not be found.
</p>

<h1>Help!?</h1>
<ul>
  <li>
    Due to the experimental nature of the Math Playground, the demo or resource
    you were looking for may have moved or replaced. Try the navigation menu to
    see if you can find what you are looking for.
  </li>
  <li>
    Please contact
    <a href="https://www.ph.ed.ac.uk/people/david-mckain">David McKain</a>
    if you need any further information.
  </li>
</ul>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
