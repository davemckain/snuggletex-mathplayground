<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision$

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="title" value="SnuggleTeX semantic input widget demo" />
<c:set var="pageId" value="snuggleTeXInputDemo" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<h1>SnuggleTeX semantic input widget demo</h1>

<p>
  Here are the results of processing your SnuggleTeX input at the server.
  There's nothing here that you didn't see before you hit "Submit"!
</p>
<p>
  (Most of the fun in this demo is in the live AJAX feedback, so this page is
  rather boring. But it does indicate what information could be used on the server
  in order to perform further processing, such as passing the input to a Computer
  Algebra System, or something similar...)
</p>
<p>
  <a href="snuggletex-input-demo">Reset demo</a>.
</p>

<h2>SnuggleTeX input</h2>
<pre class="result">${fn:escapeXml(latexMathInput)}</pre>

<c:choose>
  <c:when test="${empty errors}">
    <h2>Resulting parallel MathML</h2>
    <pre class="result">${fn:escapeXml(pmathParallel)}</pre>

    <h3>Semantically enriched Presentation MathML</h3>
    <pre class="result">${fn:escapeXml(pmath)}</pre>

    <h3>Content MathML</h3>
    <pre class="result">${fn:escapeXml(cmath)}</pre>

    <h3>Maxima input syntax</h3>
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
