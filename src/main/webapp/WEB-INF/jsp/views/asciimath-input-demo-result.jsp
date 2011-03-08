<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision$

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="title" value="ASCIIMathML semantic input widget demo" />
<c:set var="pageId" value="asciiMathInputDemo" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<h1>ASCIIMath semantic input widget demo</h1>

<p>
  Here are the results of sending the input to server. There's nothing here
  that you didn't see before you hit "Submit"!
</p>
<p>
  (Most of the fun in this demo is in the live AJAX feedback, so this page is
  rather boring. But it does indicate what information could be used on the server
  in order to perform further processing, such as passing the input to a Computer
  Algebra System, or something similar...)
</p>
<p>
  <a href="asciimath-input-demo">Reset demo</a>.
</p>

<h2>ASCIIMath Input</h2>
<pre class="result">${fn:escapeXml(asciiMathInput)}</pre>

<h2>Resulting Parallel MathML</h2>
<pre class="result">${fn:escapeXml(pmathParallel)}</pre>

<h2>Resulting Semantic Presentation MathML</h2>
<pre class="result">${fn:escapeXml(pmathSemantic)}</pre>

<h2>Resulting Content MathML</h2>
<pre class="result">${fn:escapeXml(cmath)}</pre>

<h2>Resulting Maxima input syntax</h2>
<pre class="result">${fn:escapeXml(maxima)}</pre>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
