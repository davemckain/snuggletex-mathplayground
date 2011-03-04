<%--

$Id$

Copyright (c) 2011, The University of Edinburgh.
All Rights Reserved

@author  David McKain
@version $Revision$

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="MathPlayground" />
<c:set var="pageId" value="home" />

<%@ include file="/WEB-INF/jsp/includes/header.jspf" %>

<h1>SnuggleTeX Math Playground</h1>

<h2>About</h2>

<p>
  The SnuggleTeX Math Playground is a small and somewhat random showcase
  of demos and code snippets relating to "maths on the web".
</p>
<p>
  It is primarily intended to be an incubation area to try out and share
  new ideas that might be used in other projects, and which might be of
  interest to others working in similar areas.
</p>

<h2>Current Beneficiary Projects</h2>

<p>
  Many of the ideas being explored are intended to benefit an existing
  project. Some current projects and investigative themes are:
</p>

<ul>
  <li>
    <a href="http://www2.ph.ed.ac.uk/snuggletex/">SnuggleTeX</a>:
    Improvements to the "up-conversion" or "semantic enrichment" ideas,
    as well as updating its web output templates to accommodate recent
    significant advances like MathJax.
  </li>
  <li>
    <a href="http://www2.ph.ed.ac.uk/MathAssessEngine">MathAssessEngine</a>:
    Widening browser support and improving the process for inputting and making
    sense of simple mathematical expressions.
  </li>
</ul>

<%@ include file="/WEB-INF/jsp/includes/footer.jspf" %>
