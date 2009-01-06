<?xml version="1.0"?>
<!--

$Id$

Stylesheet to soup up the raw output from the SnuggleTeX
process to make a nice web page.

This adds in the rest of the <head/> stuff, standard headers & footers
and the navigation menu.

Relative links are fixed up so they are relative to the supplied
context-path.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="h m s xs">

  <xsl:output method="xhtml"/>

  <!-- Need to pass webapp context path so as to locate images and stuff -->
  <xsl:param name="context-path" as="xs:string" required="yes"/>

  <!-- Extract page ID as first <s:pageId/> element -->
  <xsl:variable name="pageId" select="string(/h:html/h:body/s:pageId[1])" as="xs:string"/>

  <!-- Extract page title as first <h2/> heading -->
  <xsl:variable name="title" select="string(/h:html/h:body/h:h2[1])" as="xs:string"/>

  <xsl:template match="h:html">
    <html xml:lang="en" lang="en">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </html>
  </xsl:template>

  <xsl:template match="h:head">
    <head>
      <title>MathPlayground - <xsl:value-of select="$title"/></title>
      <!-- Copy any existing <meta/> elements -->
      <xsl:apply-templates select="h:meta"/>
      <meta name="author" content="David McKain" />
      <meta name="publisher" content="The University of Edinburgh" />
      <link rel="stylesheet" href="{$context-path}/includes/core.css" />
      <link rel="stylesheet" href="{$context-path}/includes/website.css" />
      <xsl:apply-templates select="." mode="add-javascript"/>
    </head>
  </xsl:template>

  <!-- Importing stylesheets should override as required -->
  <xsl:template match="h:head" mode="add-javascript"/>

  <xsl:template match="h:body">
    <body id="{$pageId}">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" id="header">
        <tr>
          <td width="84" align="left" valign="top">
            <a href="http://www.ed.ac.uk" class="headertext"><img
              src="{$context-path}/includes/uoe_logo.jpg"
              alt="The University of Edinburgh" id="logo" name="logo"
              width="84" height="84" border="0" /></a>
          </td>
          <td align="left">
            <h3>THE UNIVERSITY of EDINBURGH</h3>
            <h1>SCHOOL OF PHYSICS AND ASTRONOMY</h1>
          </td>
        </tr>
      </table>
      <h1 id="location">
        <a href="{$context-path}">MathPlayground (v@uk.ac.ed.ph.mathplayground.version@)</a>
      </h1>
      <div id="content">
        <div id="navigation">
          <div id="navinner">
            <h2>Demos</h2>
            <ul>
              <li><a class="latexinput" href="{$context-path}/latexinput.xhtml">LaTeX Conversion Demo</a></li>
              <li><a class="asciimath" href="{$context-path}/asciimath.xhtml">ASCIIMathML Demo</a></li>
            </ul>
          </div>
        </div>
        <div id="maincontent">
          <div id="maininner">
            <!-- Generate page content -->
            <xsl:apply-templates select="." mode="make-content"/>
          </div>
        </div>
      </div>
      <div id="copyright">
        <p>
          MathPlayground v@uk.ac.ed.ph.mathplayground.version@
          <br />
          Copyright &#xa9; 2009
          <a href="http://www.ph.ed.ac.uk">The School of Physics and Astronomy</a>,
          <a href="http://www.ed.ac.uk">The University of Edinburgh</a>.
          <br />
          For more information, contact
          <a href="http://www.ph.ed.ac.uk/elearning/contacts/#dmckain">David McKain</a>.
        </p>
        <p>
          The University of Edinburgh is a charitable body, registered in Scotland,
          with registration number SC005336.
        </p>
      </div>
    </body>
  </xsl:template>

  <xsl:template match="h:body" mode="make-content">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Copy all other HTML and MathML as-is -->
  <xsl:template match="h:*">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="m:math">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Keep any PI's (e.g. for MathPlayer) -->
  <xsl:template match="processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Leave out SnuggleTeX metadata -->
  <xsl:template match="s:*"/>

</xsl:stylesheet>
