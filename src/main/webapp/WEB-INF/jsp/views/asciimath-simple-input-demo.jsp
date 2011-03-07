<%--

$Id: asciimath-input-demo.jsp 1147 2011-03-04 17:17:29Z dmckain $

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1147 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="ASCIIMathML simple input demo" />
<c:set var="pageId" value="asciiMathSimpleInputDemo" />
<c:set var="headStuff">
  <link rel="stylesheet" type="text/css" href="<c:url value='includes/upconversion-ajax-control.css'/>">
  <script type="text/javascript" src="<c:url value='includes/jquery/jquery-1.5.1.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/ASCIIMathParser.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/ASCIIMathParserBrowserUtilities.js'/>"></script>
  <%@ include file="/WEB-INF/jsp/includes/mathjax.jspf" %>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[
    jQuery(document).ready(function() {
        var asciiMathParser = new ASCIIMathParser(ASCIIMathParserBrowserUtilities.createXMLDocument());
        var renderingQuery = jQuery("#mathJaxRendering");
        var inputQuery = jQuery("#asciiMathInputControl");
        var updatePreview = function() {
            var asciiMathInput = inputQuery.get(0).value;

            /* Call up parser */
            var math = asciiMathParser.parseASCIIMathInput(asciiMathInput.replace(/`/g, "\\`"));
            math.setAttribute("display", "block");

            /* Update display */
            renderingQuery.empty();
            if (document.adoptNode) {
                /* Gecko, Webkit, Opera: adopt MathML Element into this document */
                document.adoptNode(math);
                renderingQuery.append(math);
            }
            else if (math.xml) {
                /* Internet Explorer */
                renderingQuery.append(math.xml);
            }
            else {
                throw new Error("Don't know how to append MathML element into the DOM in this browser");
            }
            MathJax.Hub.Queue(["Typeset", MathJax.Hub, renderingQuery.get(0)]);
        };
        inputQuery.bind("change keyup keydown", updatePreview);
        updatePreview();
    });
//]]></script>

<h1>ASCIIMath simple input demo</h1>

<p>
  This very simple demo shows ASCIIMath input being converted to MathML while you type, using MathJax to
  render the MathML in any modern browser.
</p>
<p>
  See the <a href="asciimath-student-input-demo">ASCIIMath student input demo</a> for a more advanced version of
  this demo that additionally tries to make sense of the input.
</p>

<h2>Try it</h2>
<p>
  Enter some ASCIIMath in the input box below. It will be converted to MathML as you type and shown to the
  right of the input box.
</p>
<div class="inputBox inputWidget">
  <div class="inputPanel">
    <form action="asciimath-input-demo" method="post">
      <input id="asciiMathInputControl" name="asciiMathInput" type="text" value="${asciiMathInput}" size="30">
      <input type="submit" value="Submit">
    </form>
  </div>
  <div class="previewPanel">
    <div id="mathJaxRendering" class="rawRendering"></div>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
