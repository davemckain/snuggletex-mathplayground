<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1053 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="title" value="SnuggleTeX Verified Input Demo" />
<c:set var="pageId" value="snuggleTeXInputDemo" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<h3>SnuggleTeX Input</h3>
<pre class="result">${fn:escapeXml(latexMathInput)}</pre>

<c:choose>
  <c:when test="${empty errors}">
    <h3>Resulting Parallel MathML</h3>
    <pre class="result">${fn:escapeXml(pmathParallel)}</pre>

    <h3>Resulting Presentation MathML</h3>
    <pre class="result">${fn:escapeXml(pmath)}</pre>

    <h3>Resulting Content MathML</h3>
    <pre class="result">${fn:escapeXml(cmath)}</pre>

    <h3>Resulting Maxima</h3>
    <pre class="result">${fn:escapeXml(maxima)}</pre>
  </c:when>
  <c:otherwise>
    <h3>Resulting Parse Errors</h3>
    <ul>
      <c:forEach var="error" items="${errors}">
        <li>${error}</li>
      </c:forEach>
    </ul>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
