<%--

$Id: changeIdentity.jsp 1053 2010-11-16 12:06:55Z dmckain $

Copyright (c) 2010, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1053 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="title" value="ASCIIMathML Verified Input Demo" />
<c:set var="pageId" value="asciiMathInputDemo" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<h3>ASCIIMath Input</h3>
<pre class="result">${asciiMathInput}</pre>

<h3>Resulting Parallel MathML</h3>
<pre class="result">${fn:escapeXml(parallelMathML)}</pre>

<h3>Resulting Presentation MathML</h3>
<pre class="result">${fn:escapeXml(pMathML)}</pre>

<h3>Resulting Content MathML</h3>
<pre class="result">${fn:escapeXml(cMathML)}</pre>

<h3>Resulting Maxima</h3>
<pre class="result">${fn:escapeXml(maxima)}</pre>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
