<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision$

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="AsciiMathParser.js" />
<c:set var="pageId" value="asciiMathParser" />
<c:set var="headStuff">
  <script type="text/javascript" src="<c:url value='/includes/AsciiMathParser.js'/>"></script>
  <script type="text/javascript" src="<c:url value='/includes/AsciiMathParserBrowserUtilities.js'/>"></script>
</c:set>

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<script type="text/javascript">//<![CDATA[

function runDemo() {
    var asciiMathInput = document.forms[0].asciiMathInput.value;

    var domDocument = AsciiMathParserBrowserUtilities.createXMLDocument();
    var asciiMathParser = new AsciiMathParser(domDocument);
    var mathElement = asciiMathParser.parseAsciiMathInput(asciiMathInput);
    var mathMLString = AsciiMathParserBrowserUtilities.serializeXMLNode(mathElement);
    var indented = AsciiMathParserBrowserUtilities.indentMathMLString(mathMLString);

    alert("MathML from AsciiMathParser is:\n" + indented);
    return false;
}

//]]></script>

<h1>AsciiMathParser.js</h1>

<h2>About</h2>

<p>
  AsciiMathParser.js is a very simple reduced version of
  <a href="http://www1.chapman.edu/~jipsen/mathml/asciimath.html">ASCIIMathML.js</a> that
  provides only the core parsing functionality for converting ASCIIMath input
  syntax into MathML.
</p>
<p>
  This is aimed at developers, who may find this useful in a number of
  situations, including:
</p>
<ul>
  <li>
    Integrating ASCIIMath input syntax with a different browser display engine,
    such as <a href="http://www.mathjax.org/">MathJax</a>.
  </li>
  <li>
    Using ASCIIMath outside the browser.
  </li>
  <li>
    Unit testing the ASCIIMath parser.
  </li>
</ul>

<h2>Demo</h2>

<p>
  Type some ASCIIMath input into the box below and hit "Parse" to see the
  MathML that is produced.
</p>

<form action="AsciiMathParserDemo.jsp" class="input" onsubmit="return runDemo()">
  <div class="inputBox">
    <input id="asciiMathInput" name="asciiMathInput" type="text" value="sinx/cosy">
    <input type="button" value="Parse" onclick="runDemo()">
  </div>
</form>

<h2>Download</h2>

<ul>
  <li><a href="<c:url value='/includes/AsciiMathParser.js'/>">AsciiMathParser.js</a> (the core parsing code)</li>
  <li><a href="<c:url value='/includes/AsciiMathParserBrowserUtilities.js'/>">AsciiMathParserBrowserUtilities.js</a> (optional extras that may be useful for browser-based work)</li>
</ul>

<p>
  (This code is released under the same LGPL license as ASCIIMathML.js itself.)
</p>

<h2>Usage Requirements</h2>

<p>
  You should be able to use this code in any JavaScript engine that has access to an
  XML DOM (Level 2) <code>Document</code> Object. The code handles Microsoft's
  (slightly lacking) DOM implementation as well, though I haven't tested across many
  versions of MSXML as yet.
</p>
<p>
  Examples where this code has been run successfully include:
</p>

<ul>
  <li>"Modern" browsers (i.e. recent versions of Firefox, Internet Explorer, Chrome, Safari, Opera)</li>
  <li>The Rhino JavaScript engine running in Java</li>
</ul>

<h2>Usage</h2>

<p>
  You should create an instance of <code>AsciiMathParser</code>, passing
  an XML DOM <code>Document</code> Object that will be used to create the resulting MathML.
</p>
<p>
  You then call the <code>parseAsciiMathInput()</code> method, passing the ASCIIMath input
  string you want to parse. A call to this method returns a DOM <code>Element</code> Object
  corresponding to the resulting <code>&lt;math&gt;</code> element.
  (Note that the resulting <code>Element</code> will belong to the <code>Document</code>
  you passed earlier, but will not be added to its Node hierarchy. You can do this
  yourself if required.)
</p>
<p>
  The <code>parseAsciiMathInput()</code> method takes an optional second argument that
  lets you pass additional options. This argument should be an object containing some
  or all of the following properties:
</p>
<ul>
  <li>
    <b>displayMode</b> (true|false): If true, a <code>display="block"</code> attribute
    will be added to the resulting <code>&lt;math&gt;</code> element.</li>
  <li>
    <b>addSourceAnnotation</b> (true|false): If true, a MathML
    <code>&lt;annotation encoding="ASCIIMathInput"&gt;</code>
    element will be added to the resulting MathML that contains the original ASCIIMath
    input.
  </li>
</ul>
<p>
  <strong>Hint:</strong> If you are using this code within a browser, there
  are some methods in the optional <code>AsciiMathParserBrowserUtilities.js</code>
  file that you can use to create a suitable <code>Document</code> and serialize
  the resulting MathML <code>Element</code> to an XML String. See the example below.
</p>

<h2>Example</h2>

<p>
  Here is a simple example of the code being used in a browser:

<pre class="result">
&lt;script type="text/javascript" src="AsciiMathParser.js">&lt;/script>
&lt;script type="text/javascript" src="AsciiMathParserBrowserUtilities.js">&lt;/script>
&lt;script type="text/javascript">

var domDocument = AsciiMathParserBrowserUtilities.createXMLDocument();
var asciiMathParser = new AsciiMathParser(domDocument);
var mathElement = asciiMathParser.parseAsciiMathInput(asciiMathInput);
var mathMLString = AsciiMathParserBrowserUtilities.serializeXMLNode(mathElement);
var indented = AsciiMathParserBrowserUtilities.indentMathMLString(mathMLString);

alert("MathML from AsciiMathParser is:\n" + indented);

&lt;/script>
</pre>

<h2>Limitations</h2>
<ul>
  <li>
    This code only parses ASCIIMath input syntax. There is currently no support for its LaTeX
    input syntax, or its SVG/graphics features. (It would probably not be too
    hard to add these in future, though...)
  </li>
  <li>
    The MathML generated doesn't include any of the extra attributes and styling that ASCIIMathML
    adds. You can add these yourself if needed by manipulating the resulting MathML Element.
  </li>
</ul>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
