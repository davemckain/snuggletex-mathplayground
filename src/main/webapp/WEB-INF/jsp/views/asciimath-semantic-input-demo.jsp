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
  <link rel="stylesheet" type="text/css" href="<c:url value='/includes/upconversion-ajax-control.css'/>">
  <link rel="stylesheet" type="text/css" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/themes/redmond/jquery-ui.css">
  <script type="text/javascript" src="<c:url value='/lib/AsciiMathParser.js'/>"></script>
  <script type="text/javascript" src="<c:url value='/lib/AsciiMathParserBrowserUtilities.js'/>"></script>
  <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
  <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
  <script type="text/javascript" src="<c:url value='/includes/UpConversionAjaxController.js'/>"></script>
  <script type="text/javascript" src="<c:url value='/includes/AsciiMathInputController.js'/>"></script>
  <%@ include file="/WEB-INF/jsp/includes/mathjax.jspf" %>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[
  jQuery(document).ready(function() {
    MathJax.Hub.Queue(function() {
      UpConversionAjaxController.setUpConversionServiceUrl('<c:url value="/asciimath-upconversion-service"/>');
      UpConversionAjaxController.setDelay(500);

      var upConversionAJAXControl = UpConversionAjaxController.createUpConversionAjaxControl('previewMessages', 'previewRendering');
      upConversionAJAXControl.setPMathSemanticSourceContainerId('pmathSemanticSource');
      upConversionAJAXControl.setPMathBracketedSourceContainerId('pmathBracketedSource');
      upConversionAJAXControl.setCMathSourceContainerId('cmathSource');
      upConversionAJAXControl.setMaximaSourceContainerId('maximaSource');

      var widget = AsciiMathInputController.bindInputWidget('asciiMathInputControl', upConversionAJAXControl);
      widget.setRawSourceContainerId('rawSource');
      widget.setRawRenderingContainerId('rawRendering');
      widget.setHelpButtonId('helpToggle');
      widget.init();
      widget.syncWithInput();
      //widget.setASCIIMathInput('2y-1');
      //widget.show('2x-1', {
      //  cmath: '<math xmlns="http://www.w3.org/1998/Math/MathML"><ci>x</ci></math>',
      //  pmathBracketed: '<math xmlns="http://www.w3.org/1998/Math/MathML"><mi>x</mi></math>'
      //});
    });
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
  <form action="asciimath-semantic-input-demo" method="post" style="text-align: center">
    <div class="inputPanel">
      <a href="<c:url value='/includes/hints.html'/>" target="_blank" id="helpToggle"></a>
      <input id="asciiMathInputControl" name="asciiMathInput" type="text" value="${asciiMathInput}">
      <input type="submit" value="Submit">
    </div>
  </form>
  <div class="previewPanel">
    <div id="previewMessages"></div>
    <div id="previewRendering">
      <math xmlns="http://www.w3.org/1998/Math/MathML"></math>
    </div>
  </div>
</div>

<h2>Technical implementation details</h2>

<p>
  The ASCIIMath input syntax is converted to ASCIIMath's version of Presentation MathML
  via client-side JavaScript on each keystroke in the input box. This live output is shown here:
</p>
<div id="rawRendering" class="rawRendering">
  <math xmlns="http://www.w3.org/1998/Math/MathML"></math>
</div>
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
<pre class="result" id="rawSource"></pre>

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
