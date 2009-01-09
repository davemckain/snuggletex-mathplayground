<!--

$Id$

This stylesheet fixes the raw MathML produced by ASCIIMathML. The result
is intended to be equivalent to what is generated by SnuggleTeX, which
allows all of the existing up-conversion work to be applied here as well.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <!-- ************************************************************ -->

  <xsl:strip-space elements="m:*"/>

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

  <xsl:variable name="invertible-elementary-functions" as="xs:string+"
    select="('sin', 'cos', 'tan',
             'sec', 'csc' ,'cot',
             'sinh', 'cosh', 'tanh',
             'sech', 'csch', 'coth')"/>

  <xsl:variable name="elementary-functions" as="xs:string+"
    select="($invertible-elementary-functions,
            'arcsin', 'arccos', 'arctan',
            'arcsec', 'arccsc', 'arccot',
            'arcsinh', 'arccosh', 'arctanh',
            'arcsech', 'arccsch', 'arccoth',
            'ln', 'log', 'exp')"/>

  <!-- ************************************************************ -->

  <xsl:template match="math">
    <!-- Skip over the pointless <mstyle/> and move the @title to an annotation -->
    <math>
      <semantics>
        <xsl:call-template name="maybe-wrap-in-mrow">
          <xsl:with-param name="elements" as="element()*">
            <xsl:apply-templates/>
          </xsl:with-param>
        </xsl:call-template>
        <annotation encoding="ASCIIMathML">
          <xsl:value-of select="normalize-space(@title)"/>
        </annotation>
      </semantics>
    </math>
  </xsl:template>

  <xsl:template match="math/mstyle">
    <!-- Descend down -->
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Join unary minus and literal numbers together -->
  <xsl:template match="mo[.='-' and not(preceding-sibling::*) and following-sibling::*[1][self::mn]]">
    <mn>
      <xsl:text>-</xsl:text>
      <xsl:value-of select="following-sibling::*[1]"/>
    </mn>
  </xsl:template>

  <xsl:template match="mn[preceding-sibling::*[1][self::mo and .='-'] and not(preceding-sibling::*[2])]">
    <!-- This has been handled above -->
  </xsl:template>

  <!--
  ASCIIMathML outputs elementary functions as <mo/> instead of <mi/> and
  always wraps the result in an <mrow/> if they are not going to be wrapped
  in something else. It assumes that the operator only
  applies to the following token. We're going to change this behaviour
  by unwrapping the mrow.
  -->
  <xsl:template match="mrow[count(*)=2 and *[1][self::mo and $elementary-functions=string(.)]]">
    <mi>
      <xsl:value-of select="*[1]"/>
    </mi>
    <xsl:apply-templates select="*[2]"/>
  </xsl:template>

  <xsl:template match="mo[$elementary-functions=string(.)]">
    <mi>
      <xsl:value-of select="."/>
    </mi>
  </xsl:template>

  <xsl:template match="mrow">
    <!-- Process children to see if we really need this -->
    <xsl:variable name="processed-children" as="element()*">
      <xsl:apply-templates/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($processed-children)=1">
        <xsl:copy-of select="$processed-children"/>
      </xsl:when>
      <xsl:otherwise>
        <mrow>
          <xsl:copy-of select="$processed-children"/>
        </mrow>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="maybe-wrap-in-mrow">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1">
        <xsl:copy-of select="$elements"/>
      </xsl:when>
      <xsl:otherwise>
        <mrow>
          <xsl:copy-of select="$elements"/>
        </mrow>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>
