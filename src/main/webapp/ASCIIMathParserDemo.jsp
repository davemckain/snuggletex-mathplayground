<%--

$Id$

Copyright (c) 2010, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1053 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="ASCIIMathML Parser Demo" />
<c:set var="pageId" value="asciiMathParserDemo" />
<c:set var="headStuff">
  <script type="text/javascript" src="includes/ASCIIMathParser.js"></script>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[

function createXMLDocument() {
  var doc;
  if (document.implementation && document.implementation.createDocument) {
    /* Gecko, Webkit, Opera */
    doc = document.implementation.createDocument("", "", null);
  }
  else {
    try {
      /* Internet Explorer */
      doc = new ActiveXObject("Microsoft.XMLDOM");
    }
    catch (e) {
      alert("I don't know how to create a DOM Document in this browser");
    }
  }
  return doc;
}

function serializeXMLNode(node) {
  var xml;
  try {
    /* Gecko, Webkit, Opera */
    var serializer = new XMLSerializer();
    xml = serializer.serializeToString(node);
  }
  catch (e) {
    try {
      /* Internet Explorer */
      xml = node.xml;
    }
    catch (e) {
      alert("I don't know how to serialize XML in this browser");
    }
  }
  return xml;
}

function runDemo() {
  var asciiMathInput = document.forms[0].asciiMathInput.value;

  var domDocument = createXMLDocument();
  var asciiMathParser = new ASCIIMathParser(domDocument);
  var asciiMathParser2 = new ASCIIMathParser(domDocument);
  var mathElement = asciiMathParser.parseASCIIMathInput(asciiMathInput);
  var mathMLString = serializeXMLNode(mathElement);

  alert("MathML from ASCIIMath:\n" + mathMLString);
  return false;
}

//]]></script>

<h2>ASCIIMath Parser Demo</h2>

<form action="ASCIIMathParserDemo.jsp" class="input">
  <div class="inputBox">
    <input id="asciiMathInput" name="asciiMathInput" type="text" value="sinx/cosy">
    <input type="button" value="Parse" onclick="runDemo()">
  </div>
</form>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
