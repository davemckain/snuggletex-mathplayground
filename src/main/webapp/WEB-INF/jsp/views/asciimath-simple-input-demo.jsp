<%--

$Id: asciimath-input-demo.jsp 1147 2011-03-04 17:17:29Z dmckain $

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1147 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Simple input with ASCIIMath and MathJax" />
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

<h1>Simple input with ASCIIMath and MathJax</h1>

<p>
  This demo lets you enter simple mathematical expressions using
  <a href="http://www1.chapman.edu/~jipsen/mathml/asciimath.xml">ASCIIMath</a>
  syntax. Your input is converted to MathML and displayed using
  <a href="http://www.mathjax.org/">MathJax</a>
  as you type.
<p>
<p>
  (Also see the <a href="asciimath-semantic-input-demo">ASCIIMath student input demo</a>
  for a more advanced version of this demo that additionally tries to make sense of the input.)
</p>

<h2>Try it</h2>
<p>
  Enter some ASCIIMath in the input box below. It will be converted to MathML as you type and shown to the
  right of the input box.
</p>
<div class="inputBox inputWidget">
  <div class="inputPanel">
    <form id="asciiMathInputForm" onsubmit="return false">
      <input id="asciiMathInputControl" name="asciiMathInput" type="text" value="${asciiMathInput}" size="30">
    </form>
  </div>
  <div class="previewPanel">
    <div id="mathJaxRendering" class="rawRendering"></div>
  </div>
</div>

<h2>Technical notes</h2>

<p>
  The interesting aspect of this demo is the marriage of ASCIIMath for input and
  MathJax for output. The use of MathJax is nice here as it makes this idea
  work in any modern browser, so has applications for rich text editors and things
  like that.
</p>
<p>
  This demo is inspired by the
  <a href="http://www1.chapman.edu/~jipsen/mathml/asciimatheditor.xml">ASCIIMath Editor</a>
  on the ASCIIMath website. Note that my demo only handles ASCIIMath math expressions
  (i.e. the bits you would put between `...` in the ASCIIMath Editor demo). It wouldn't be
  hard to extend this to do that too.
</p>
<p>
  View the page source to see the underlying JavaScript that glues things
  together.  It uses my cut-down <a
  href="asciimath-parser">ASCIIMathParser.js</a> to do the actual parsing, but
  you can do the same thing quite easily with the stock ASCIIMathML.js script
  as well.  I also used <a href="http://jquery.com/">jQuery</a> for
  expressiveness and convenience, but this particular demo wouldn't be hard to
  do without jQuery.
</p>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
