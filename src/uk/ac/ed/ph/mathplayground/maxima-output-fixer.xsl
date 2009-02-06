<!--

$Id$

This stylesheets attempts to fix up the raw MathML produced by Maxima
using the standard mathml.lisp module.

TODO: mathml.lisp outputs entities, which we need to substitute early on or pull in DTD...
TODO: Sets are output in MathML as set(1,2)...!

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <!-- ************************************************************ -->

  <xsl:strip-space elements="m:*"/>

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

  <!-- ************************************************************ -->

  <xsl:template match="*|text()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Maxima adds <mspace/> elements to enhance viewability. We don't want these -->
  <xsl:template match="mspace"/>

  <!-- Fix <mfenced/>. Maxima gets the content model for this completely wrong and also
       adds in redundant separators. -->
  <xsl:template match="mfenced">
    <mfenced open="{if (@open) then @open else '('}" close="{if (@close) then @close else ')'}">
      <xsl:for-each-group select="*" group-adjacent="self::mo and .=','">
        <xsl:choose>
          <xsl:when test="current-group()">
            <xsl:choose>
              <xsl:when test="count(.)=1">
                <xsl:apply-templates select="."/>
              </xsl:when>
              <xsl:otherwise>
                <mrow>
                  <xsl:apply-templates select="."/>
                </mrow>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <!-- Ignore this -->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </mfenced>
  </xsl:template>

  <!-- Strip any <mrow/>s containing only 1 child -->
  <xsl:template match="mrow[count(node())=1]">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>

