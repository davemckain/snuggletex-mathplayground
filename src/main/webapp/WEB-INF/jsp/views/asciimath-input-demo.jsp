<%--

$Id: changeIdentity.jsp 1053 2010-11-16 12:06:55Z dmckain $

Copyright (c) 2010, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision: 1053 $

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="ASCIIMathML Verified Input Demo" />
<c:set var="pageId" value="asciiMathInputDemo" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[
  jQuery(document).ready(function() {
    ASCIIMathInputController.setValidatorServiceUrl('ASCIIMathUpConversionService');
    ASCIIMathInputController.setDelay(500);
    var widget = ASCIIMathInputController.createInputWidget('asciiMathInputControl', 'asciiMathOutputControl');
    widget.setMathJaxRenderingContainerId('mathJaxRendering');
    widget.validatedRenderingContainerId = 'validatedRendering';
    widget.pmathSourceContainerId = 'pmathSource';
    widget.cmathSourceContainerId = 'cmathSource';
    widget.maximaSourceContainerId = 'maximaSource';
    widget.init();
  });
//]]></script>
<h3>ASCIIMath Input</h3>
<form action="ASCIIMathInputDemo" method="post">
  <input id="asciiMathInputControl" name="asciiMathInput" type="text" value="${asciiMathInput}">
  <input id="asciiMathOutputControl" name="asciiMathOutput" type="hidden">
  <input type="submit" value="Go!">
</form>
<h3>ASCIIMathML (real time MathJax rendering)</h3>
<div id="mathJaxRendering"></div>

<h3>Verified MathML</h3>
<div id="validatedRendering"></div>

<h3>Resulting ASCIIMathML source</h3>
<pre class="result" id="pmathSource"></pre>

<h3>Resulting Content MathML source</h3>
<pre class="result" id="cmathSource"></pre>

<h3>Resulting Maxima code</h3>
<pre class="result" id="maximaSource"></pre>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
