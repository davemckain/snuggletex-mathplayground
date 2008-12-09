<!--

$Id$

This stylesheet fixes the raw MathML produced by ASCIIMathML, inferring
certain bits of semantics.

TODO: I've not done this yet :-)

Copyright (c) 2008 The University of Edinburgh
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
        <xsl:apply-templates/>
        <annotation encoding="ASCIIMathML">
          <xsl:value-of select="normalize-space(@title)"/>
        </annotation>
      </semantics>
    </math>
  </xsl:template>

  <xsl:template match="math/mstyle">
    <xsl:copy-of select="*"/>
  </xsl:template>

</xsl:stylesheet>
