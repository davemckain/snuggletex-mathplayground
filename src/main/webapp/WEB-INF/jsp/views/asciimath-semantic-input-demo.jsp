<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision$

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="ASCIIMathML semantic input widget demo" />
<c:set var="pageId" value="asciiMathInputDemo" />
<c:set var="headStuff">
  <link rel="stylesheet" type="text/css" href="<c:url value='includes/upconversion-ajax-control.css'/>">
  <script type="text/javascript" src="<c:url value='includes/jquery/jquery-1.5.1.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/ASCIIMathParser.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/ASCIIMathParserBrowserUtilities.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/UpConversionAJAXController.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/ASCIIMathInputController.js'/>"></script>
  <%@ include file="/WEB-INF/jsp/includes/mathjax.jspf" %>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[
  jQuery(document).ready(function() {
    UpConversionAJAXController.setUpConversionServiceUrl('<c:url value="/asciimath-upconversion-service"/>');
    UpConversionAJAXController.setDelay(500);

    var upConversionAJAXControl = UpConversionAJAXController.createUpConversionAJAXControl();
    upConversionAJAXControl.setBracketedRenderingContainerId('previewRendering');
    upConversionAJAXControl.setPMathSemanticSourceContainerId('pmathSemanticSource');
    upConversionAJAXControl.setPMathBracketedSourceContainerId('pmathBracketedSource');
    upConversionAJAXControl.setCMathSourceContainerId('cmathSource');
    upConversionAJAXControl.setMaximaSourceContainerId('maximaSource');

    var widget = ASCIIMathInputController.createInputWidget('asciiMathInputControl', 'asciiMathOutputControl', upConversionAJAXControl);
    widget.setPMathSourceContainerId('pmathSource');
    widget.setMathJaxRenderingContainerId('mathJaxRendering');
    widget.init();
  });
//]]></script>

<h1>ASCIIMath semantic input widget demo</h1>

<p>
  This demo presents a little "widget" for inputting the kind of simple maths
  we might expect from a 16-18 year-old student in the UK. It uses ASCIIMathML
  as an input syntax and attempts to "make sense" of the input and provide
  useful feedback while the student types. This type of approach might be
  useful in assessment systems.
</p>
<p>
  (This is a more specialised version of the
  <a href="asciimath-simple-input-demo">ASCIIMath simple input with MathJax</a>
  demo. That demo focuses only on the input on display of mathematics; this demo
  additionallly tries to apply semantics to it.)
</p>

<h2>Try it</h2>
<p>
  Enter some ASCIIMath in the box below. Feedback will be shown while you type
  to the right of the input box. Some additional technical details will appear
  below that may be interesting to some people...
</p>
<div class="inputBox inputWidget">
  <div class="inputPanel">
    <form action="asciimath-semantic-input-demo" method="post" style="text-align: center">
      <input id="asciiMathInputControl" name="asciiMathInput" type="text" value="${asciiMathInput}">
      <input id="asciiMathOutputControl" name="asciiMathOutput" type="hidden">
      <input type="submit" value="Submit">
    </form>
  </div>
  <div class="previewPanel">
    <div id="previewRendering"></div>
  </div>
</div>

<h2>Quick input guide</h2>

<ul>
  <li><b>Numbers:</b> 0, 53, 62.9, ...</li>
  <li><b>Variables:</b> <kbd>x</kbd>, <kbd>y</kbd>, <kbd>N</kbd>, <kbd>i</kbd>, <kbd>alpha</kbd>, <kbd>pi</kbd>, ...</li>
  <li><b>Subscripted variables:</b> <kbd>x_1</kbd>, <kbd>n_{i_j}</kbd>,  <kbd>y_{n,m}</kbd> <em>or</em> y_<kbd>(n,m)</kbd>, ...</li>
  <li><b>Powers:</b> <kbd>x^2</kbd>, <kbd>e^{i pi}</kbd>, ...</li>
  <li><b>Arithmetic operators:</b> <kbd>+</kbd>, <kbd>-</kbd>, <kbd>*</kbd>, <kbd>times</kbd>, <kbd>/</kbd></li>
  <li><b>Fractions:</b> <kbd>1/2</kbd>, <kbd>(1+x)/(2-y)</kbd>
  <li><b>Implicit multiplcation:</b> <kbd>2x</kbd>, <kbd>5t^2</kbd>, <kbd>asin(bx)</kbd> ...</li>
  <li><b>Precedence:</b> follows usual rules, use brackets if required</li>
  <li><b>Brackets:</b> <kbd>sin(2t+4)</kbd> (often optional, <kbd>{...}</kbd> as well)</li>
  <li><b>Elementary functions</b>: <kbd>exp</kbd>, <kbd>log</kbd>, <kbd>sin</kbd>, <kbd>cos</kbd>, <kbd>tan</kbd>, <kbd>sinh</kbd>, ...</li>
  <li><b>Inverse functions:</b> <kbd>sin^-1x</kbd>, <kbd>coth^-1t</kbd>, ...</li>
  <li><b>Powers of functions:</b> <kbd>sin^2x = (sin x)^2</kbd>, ...</li>
  <li><b>Oddities:</b> <kbd>sin2x = (sin2)x != sin(2x)</kbd> (use brackets here)</li>
  <li><b>Functions:</b> <kbd>f</kbd> and <kbd>g</kbd> are treated as functions</li>
  <li><b>Special symbols:</b> <kbd>e</kbd> is the exponential number, <kbd>i</kbd> is the imaginary number,
    <kbd>&#x3b3;</kbd> is Euler's constant.</li>
</ul>

<h2>Technical implementation details</h2>

<p>
  The ASCIIMath input syntax is converted to ASCIIMath's version of Presentation MathML
  via client-side JavaScript on each keystroke in the input box. This live output is shown here:
</p>
<div id="mathJaxRendering" class="rawRendering"></div>
<p>
  This raw output then sent (after a short delay) to a simple AJAX web service
  that runs the SnuggleTeX semantic enrichment process on it to see if it can
  be converted to Content MathML (and Maxima input syntax). It also converts it
  to a special "bracketed" version of Presentation MathML, which is shown next
  to the input box above, using MathJax for rendering so that it works in most
  modern browsers. This bracketed MathML is useful as it allows the student to
  check that the input is being interpreted as she intended, giving her the
  ability to adjust her input by adding extra brackets, or making other
  changes. The results of this process are sent back to the browser, which
  updates the page as appropriate.
</p>
<p>
  MathML experts may be interested in the live information below.
</p>

<h3>Raw Presentation MathML generated by ASCIIMath</h3>
<p>
  This is the raw MathML generated by ASCIIMath before it gets passed to the web service
  for munging. Note that there can be some oddities in its output, which are fixed up by
  the web service.
</p>
<pre class="result" id="pmathSource"></pre>

<h3>Semantic Presentation MathML generated by SnuggleTeX</h3>
<p>
  This shows the result of the first part of the SnuggleTeX semantic enrichment process,
  which restructures the raw Presentation MathML to emphasise the underlying semantics
  and make it more amenable for further processing.
</p>
<pre class="result" id="pmathSemanticSource"></pre>

<h3>Bracketed Presentation MathML generated SnuggleTeX</h3>
<p>
  This is the source of the MathML that is shown to the student, emphasising where
  inferred groupings and bracketings have been made.
</p>
<pre class="result" id="pmathBracketedSource"></pre>

<h3>Resulting Content MathML source</h3>
<p>
  This is the resulting Content MathML, created by the web service. Being able to generate
  this is criteria we use here for "making sense of the input".
</p>
<pre class="result" id="cmathSource"></pre>

<h3>Resulting Maxima code</h3>
<p>
  This is the resulting Maxima input syntax, created by the web service from the Content
  MathML. This will not always succeed as some supported Content MathML constructs can't
  be turned into Maxima equivalents.
</p>
<pre class="result" id="maximaSource"></pre>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
