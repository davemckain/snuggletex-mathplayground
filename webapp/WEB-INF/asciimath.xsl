<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
demonstrating ASCIIMathML -> various formats.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="h xs">

  <!-- Import basic formatting stylesheet -->
  <xsl:import href="format-output.xsl"/>

  <!-- ASCIIMath input - this will be put into a textarea -->
  <xsl:param name="ascii-input" select="'-1+sinx'" as="xs:string"/>

  <!-- Various text outputs -->
  <xsl:param name="pmathml-raw" select="''" as="xs:string"/>
  <xsl:param name="pmathml-fixed" select="'(Failed)'" as="xs:string"/>
  <xsl:param name="pmathml-enhanced" select="'(Failed)'" as="xs:string"/>
  <xsl:param name="cmathml" as="xs:string" select="'(Failed)'" required="no"/>
  <xsl:param name="maxima-input" as="xs:string" select="'(Failed)'" required="no"/>
  <xsl:param name="maxima-output" as="xs:string" select="'(Failed)'" required="no"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'asciimath'" as="xs:string"/>

  <!-- Override title -->
  <xsl:variable name="title" select="'AsciiMathML Demo'" as="xs:string"/>

  <!-- Add in JavaScript -->
  <xsl:template match="h:head" mode="add-javascript">
    <script type="text/javascript" src="{$context-path}/includes/ASCIIMathML.js"></script>
    <script type="text/javascript" src="{$context-path}/includes/ASCIIMathMLeditor.js"></script>
    <script type="text/javascript" src="{$context-path}/includes/ASCIIMathMLcustomisations.js"></script>
    <script type="text/javascript"><![CDATA[
      function setupMath() {
        AMdisplayQuoted('asciimathinput','preview',true);
      }
      window.onload = setupMath;
    ]]></script>
  </xsl:template>

  <xsl:template match="h:body" mode="make-content">
    <h2><xsl:value-of select="$title"/></h2>

    <!-- Stolen shamelessly from PROMPT! -->
    <h3>Input</h3>
    <p>
      Enter some ASCIIMathML into the box below. You should see a real time preview
      of this while you type. Hit <tt>Go!</tt> to see the resulting outputs, which take
      the MathML produced by ASCIIMathML and do stuff to it.
    </p>
    <form method="post" onsubmit="submitMathML('preview', 'mathml')">
      ASCIIMath Input:
      <input id="asciimathinput" name="asciimathinput" type="text" value="{$ascii-input}"
        onkeyup="AMdisplayQuoted('asciimathinput','preview',true)" />
      <input type="hidden" id="mathml" name="mathml" />
      <input type="submit" value="Go!" />
    </form>
    <div class="result">
      Live Preview: <div id="preview"><xsl:text> </xsl:text></div>
    </div>

    <xsl:if test="$pmathml-raw!=''">
      <h3>Raw Presentation MathML extracted from ASCIIMathML</h3>
      <p>
        (This is the raw MathML produced by ASCIIMathML.)
      </p>
      <pre class="result">
        <xsl:value-of select="$pmathml-raw"/>
      </pre>

      <h3>Fixed Presentation MathML</h3>
      <p>
        (This gets it into a roughly equivalent form as the basic output from
        SnuggleTeX.)
      </p>
      <pre class="result">
        <xsl:value-of select="$pmathml-fixed"/>
      </pre>

      <h3>(Post-processed) Presentation MathML</h3>
      <pre class="result">
        <xsl:value-of select="$pmathml-enhanced"/>
      </pre>

      <h3>Content MathML</h3>
      <pre class="result">
        <xsl:value-of select="$cmathml"/>
      </pre>

      <h3>Maxima Input</h3>
      <pre class="result">
        <xsl:value-of select="$maxima-input"/>
      </pre>

      <h3>(Raw) Maxima Output</h3>
      <pre class="result">
        <xsl:value-of select="$maxima-output"/>
      </pre>

    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
