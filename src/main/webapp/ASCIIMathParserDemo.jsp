<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1053 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="ASCIIMathML Parser Demo" />
<c:set var="pageId" value="asciiMathParserDemo" />
<c:set var="headStuff">
  <script type="text/javascript" src="includes/ASCIIMathParser.js"></script>
  <script type="text/javascript" src="includes/ASCIIMathParserBrowserUtilities.js"></script>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[

function runDemo() {
    var asciiMathInput = document.forms[0].asciiMathInput.value;

    var domDocument = ASCIIMathParserBrowserUtilities.createXMLDocument();
    var asciiMathParser = new ASCIIMathParser(domDocument);
    var mathElement = asciiMathParser.parseASCIIMathInput(asciiMathInput);
    var mathMLString = ASCIIMathParserBrowserUtilities.serializeXMLNode(mathElement);

    alert("MathML from ASCIIMath:\n" + ASCIIMathParserBrowserUtilities.indentMathMLString(mathMLString));
    return false;
}

//]]></script>

<h2>ASCIIMath Parser Demo</h2>

<form action="ASCIIMathParserDemo.jsp" class="input" onsubmit="return runDemo()">
  <div class="inputBox">
    <input id="asciiMathInput" name="asciiMathInput" type="text" value="sinx/cosy">
    <input type="button" value="Parse" onclick="runDemo()">
  </div>
</form>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
