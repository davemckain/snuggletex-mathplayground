<!--

$Id$

This stylesheet is intended to be pipelined after the P->C stylesheet
and converts the subset of Content MathML produced by that stylesheet
into Maxima input.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:output method="text"/>

  <xsl:variable name="elementary-functions">
    <!-- The resulting Maxima function name is encoded within an input Content MathML element -->
    <sin>sin</sin>
    <cos>cos</cos>
    <tan>tan</tan>
    <sec>sec</sec>
    <csc>csc</csc>
    <cot>cot</cot>
    <arcsin>asin</arcsin>
    <arccos>acos</arccos>
    <arctan>atan</arctan>
    <arcsec>asec</arcsec>
    <arccsc>acsc</arccsc>
    <arccot>acot</arccot>
    <sinh>sinh</sinh>
    <cosh>cosh</cosh>
    <tanh>tanh</tanh>
    <sech>sech</sech>
    <csch>csch</csch>
    <coth>coth</coth>
    <arcsinh>asinh</arcsinh>
    <arccosh>acosh</arccosh>
    <arctanh>atanh</arctanh>
    <arcsech>asech</arcsech>
    <arccsch>acsch</arccsch>
    <arccoth>acoth</arccoth>
    <exp>exp</exp>
    <ln>log</ln><!-- NB! -->
    <log></log><!-- No Maxima built-in for this -->
  </xsl:variable>

  <xsl:function name="s:is-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($elementary-functions/*[local-name()=$element/local-name()])"/>
  </xsl:function>

  <xsl:function name="s:get-maxima-function" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="string($elementary-functions/*[local-name()=$element/local-name()])"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <xsl:template match="math">
    <xsl:choose>
      <xsl:when test="semantics">
        <xsl:apply-templates select="semantics/*[1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="apply[*[1][self::eq]]">
    <!-- Equals -->
    <xsl:for-each select="*[position() != 1]">
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()">
        <xsl:text> = </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="apply[*[1][self::plus]]">
    <!-- Sum -->
    <xsl:text>(</xsl:text>
    <xsl:for-each select="*[position() != 1]">
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()">
        <xsl:text> + </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::minus]]">
    <!-- Difference, which is either unary or binary -->
    <xsl:text>(</xsl:text>
    <xsl:choose>
      <xsl:when test="count(*)=1">
        <!-- Unary version -->
        <xsl:text>-</xsl:text>
        <xsl:apply-templates select="*[2]"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Binary version -->
        <xsl:apply-templates select="*[2]"/>
        <xsl:text> - </xsl:text>
        <xsl:apply-templates select="*[3]"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::times]]">
    <!-- Product -->
    <xsl:text>(</xsl:text>
    <xsl:for-each select="*[position()!=1]">
      <xsl:apply-templates select="."/>
      <xsl:if test="position()!=last()">
        <xsl:text> * </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::divide]]">
    <!-- Quotient, which is always binary -->
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[2]"/>
    <xsl:text> / </xsl:text>
    <xsl:apply-templates select="*[3]"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::power]]">
    <!-- Power, which is always binary -->
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[2]"/>
    <xsl:text> ^ </xsl:text>
    <xsl:apply-templates select="*[3]"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::exp]]">
    <!-- Exponential operator, which is unary -->
    <xsl:text>exp(</xsl:text>
    <xsl:apply-templates select="*[2]"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::root] and not(degree)]">
    <!-- Square Root -->
    <xsl:text>sqrt(</xsl:text>
    <xsl:apply-templates select="*[2]"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::root] and degree]">
    <!-- nth Root -->
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[not(degree) and not(root)]"/>
    <xsl:text>)^(1/</xsl:text>
    <xsl:apply-templates select="degree/*"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Elementary Function -->
  <xsl:template match="apply[*[1][s:is-elementary-function(current()/*[1])]]">
    <xsl:value-of select="s:get-maxima-function(*[1])"/>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[position() != 1]"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Power of an Elementary Function. For example:

  <apply>
    <apply>
      <power/>
      <sin/>
      <cn>2</cn>
    </apply>
    <ci>x</ci>
  </apply>

  -->
  <xsl:template match="apply[*[1][self::apply and *[1][self::power] and s:is-elementary-function(*[2]) and *[3][self::cn]]]">
    <xsl:value-of select="s:get-maxima-function(*[1]/*[2])"/>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[position() != 1]"/>
    <xsl:text>)^</xsl:text>
    <xsl:apply-templates select="*[1]/*[3]"/>
  </xsl:template>

  <xsl:template match="apply">
    <xsl:message>Could not handle apply with first child <xsl:value-of select="*[1]/local-name()"/></xsl:message>
  </xsl:template>

  <xsl:template match="*[../*[1][self::apply]]" priority="-1">
    <!-- This will be pulled in as appropriate -->
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="interval">
    <xsl:message>No support for interval</xsl:message>
  </xsl:template>

  <xsl:template match="set">
    <xsl:text>{</xsl:text>
    <xsl:for-each select="*">
      <xsl:apply-templates select="."/>
      <xsl:if test="position()!=last()">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="list">
    <xsl:text>[</xsl:text>
    <xsl:for-each select="*">
      <xsl:apply-templates select="."/>
      <xsl:if test="position()!=last()">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="exponentiale">
    <xsl:text>%e</xsl:text>
  </xsl:template>

  <xsl:template match="imaginaryi">
    <xsl:text>%i</xsl:text>
  </xsl:template>

  <xsl:template match="pi">
    <xsl:text>%pi</xsl:text>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="ci">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="cn">
    <xsl:value-of select="."/>
  </xsl:template>

</xsl:stylesheet>
