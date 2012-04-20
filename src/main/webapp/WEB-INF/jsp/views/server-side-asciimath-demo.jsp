<%--

$Id$

Attributes (after POST):

asciiMathInput
mathmlOutput

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision$

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="title" value="Server-side ASCIIMath Demo" />
<c:set var="pageId" value="serverSideasciiMathDemo" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<h1>Server-side ASCIIMath input demo</h1>

<p>
  This demo shows my
  <a href="asciimath-parser">AsciiMathParser.js</a>
  running outside its traditional browser environment.
</p>

<h2>Try it</h2>
<p>
  Enter an ASCIIMath math expression in the box below and hit "Submit" to send
  it to the server for conversion to MathML:
</p>
<form action="server-side-asciimath-demo" method="post" class="input">
  <div class="inputBox">
    <input name="asciiMathInput" type="text" value="${asciiMathInput}">
    <input type="submit" value="Submit">
  </div>
</form>

<c:if test="${!empty mathmlOutput}">
  <h2>MathML output (from server)</h2>

  <pre class="result">${fn:escapeXml(mathmlOutput)}</pre>
</c:if>

<h2>Technical notes</h2>

<p>
  There is not much to this demo at all. The ASCIIMath input you enter is sent
  to a Java servlet. This runs the
  <a href="asciimath-parser">AsciiMathParser.js</a> on it,
  using the <a href="http://www.mozilla.org/rhino/">Rhino JavaScript</a> engine,
  and the resulting MathML is sent back to be flashed in front of your eyes. There
  is <strong>no</strong> JavaScript running in the browser here at all.
</p>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
