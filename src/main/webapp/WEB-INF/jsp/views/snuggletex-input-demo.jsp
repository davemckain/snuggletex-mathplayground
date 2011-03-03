<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1053 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="SnuggleTeX Verified Input Demo" />
<c:set var="pageId" value="snuggleTeXInputDemo" />
<c:set var="headStuff">
  <link rel="stylesheet" type="text/css" href="<c:url value='includes/upconversion-ajax-control.css'/>">
  <script type="text/javascript" src="<c:url value='includes/jquery/jquery-1.5.1.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/UpConversionAJAXController.js'/>"></script>
  <script type="text/javascript" src="<c:url value='includes/SnuggleTeXInputController.js'/>"></script>
  <%@ include file="/WEB-INF/jsp/includes/mathjax.jspf" %>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<!-- TEST -->
<script type="text/javascript">//<![CDATA[
  jQuery(document).ready(function() {
    UpConversionAJAXController.setUpConversionServiceUrl('SnuggleTeXUpConversionService');
    UpConversionAJAXController.setDelay(500);
    UpConversionAJAXController.setUsingMathJax(true);

    var upConversionAJAXControl = UpConversionAJAXController.createUpConversionAJAXControl();
    upConversionAJAXControl.setBracketedRenderingContainerId('verifiedRendering');
    upConversionAJAXControl.setCMathSourceContainerId('cmathSource');
    upConversionAJAXControl.setMaximaSourceContainerId('maximaSource');

    var widget = SnuggleTeXInputController.createInputWidget('latexInputControl', upConversionAJAXControl);
    widget.init();
  });
//]]></script>
<h2>SnuggleTeX Verified Input Demo</h2>
<h3>SnuggleTeX Input</h3>
<p>
  Enter some LaTeX in the box below:
</p>
<form action="SnuggleTeXInputDemo" method="post" class="input">
  <div class="inputBox">
    <input id="latexInputControl" name="latexMathInput" type="text" value="${latexMathInput}">
    <input type="submit" value="Submit">
  </div>
</form>

<h3>Verified MathML</h3>
<p>This shows the results of "verifying" the input to see if it "makes sense".</p>
<div id="verifiedRendering"></div>

<h3>Resulting Content MathML source</h3>
<p>This is the resulting Content MathML, created by the verification service.</p>
<pre class="result" id="cmathSource"></pre>

<h3>Resulting Maxima code</h3>
<p>This is the resulting Maxima input syntax, created by the verification service.</p>
<pre class="result" id="maximaSource"></pre>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
