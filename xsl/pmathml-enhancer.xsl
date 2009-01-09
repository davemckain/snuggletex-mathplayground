<!--

$Id$

This stylesheet "enhances" raw Presentation MathML to try to infer
semantics for the types of mathematics we are interested in.

This should be able to be applied to any MathML document, but is
currently designed primarily to be applied to the output from SnuggleTeX.

TODO: Think about plus-or-minus operator??
TODO: Should we specify precedence for other infix operators? (Later... nothing to do with MathAssess)

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

  <xsl:output method="xml" encoding="US-ASCII" indent="yes"/>

  <!-- ************************************************************ -->

  <xsl:template match="math">
    <!-- Deal with any existing <semantics/> element -->
    <xsl:variable name="presentation-mathml" select="if (semantics) then (if (semantics/mrow) then semantics/mrow/* else semantics/*[1]) else *" as="element()*"/>
    <xsl:variable name="annotations" select="if (semantics) then semantics/*[position() != 1] else ()" as="element()*"/>
    <!-- Enhance the existing PMathML -->
    <xsl:variable name="enhanced-pmathml" as="element()*">
      <!-- First of all, infer parentheses. It's easiest if we do this as a separate step
           as the underlying algorithm changes the tree structure a lot. -->
      <xsl:variable name="after-fencing">
        <xsl:call-template name="fence-parentheses">
          <xsl:with-param name="elements" select="$presentation-mathml"/>
        </xsl:call-template>
      </xsl:variable>
      <!-- Then do rest of the inference process -->
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="$after-fencing/*"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- Now we build up the resulting <math/> element -->
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
        <xsl:when test="$enhanced-pmathml/descendant-or-self::s:fail">
          <!-- Some failure was found that compromised the enhancement process. We'll
          put out an annotation containing the full details. -->
          <semantics>
            <xsl:call-template name="maybe-wrap-in-mrow">
              <xsl:with-param name="elements" as="element()+">
                <xsl:apply-templates select="$enhanced-pmathml" mode="strip-failures"/>
              </xsl:with-param>
            </xsl:call-template>
            <xsl:copy-of select="$annotations"/>
            <annotation-xml encoding="Presentation-enhancement-failure">
              <xsl:copy-of select="$enhanced-pmathml"/>
            </annotation-xml>
          </semantics>
        </xsl:when>
        <xsl:otherwise>
          <!-- Everything succeeded fine -->
          <xsl:choose>
            <xsl:when test="semantics">
              <semantics>
                <xsl:call-template name="maybe-wrap-in-mrow">
                  <xsl:with-param name="elements" select="$enhanced-pmathml"/>
                </xsl:call-template>
                <xsl:copy-of select="$annotations"/>
              </semantics>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="$enhanced-pmathml"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Failure stripping -->

  <xsl:template match="*|text()" mode="strip-failures">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="strip-failures"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="s:fail" mode="strip-failures">
    <merror>
      <mtext><xsl:value-of select="@message"/></mtext>
    </merror>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:variable name="s:parentheses" as="element()+">
    <s:pair open='(' close=')'/>
    <s:pair open='[' close=']'/>
    <s:pair open='{{' close='}}'/>
    <s:pair open='&lt;' close='&gt;'/>
  </xsl:variable>

  <xsl:function name="s:is-open-parenthesis" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $s:parentheses[@open=$element]])"/>
  </xsl:function>

  <xsl:function name="s:is-close-parenthesis" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $s:parentheses[@close=$element]])"/>
  </xsl:function>

  <xsl:function name="s:get-matching-closer-value" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="$s:parentheses[@open=$element]/@close"/>
  </xsl:function>

  <xsl:template name="fence-parentheses">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:call-template name="fence-parentheses-until">
      <xsl:with-param name="elements" select="$elements"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="fence-parentheses-until">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:param name="closer-value" as="xs:string" select="''" required="no"/>
    <xsl:variable name="first-opener" as="element()?" select="$elements[s:is-open-parenthesis(.)][1]"/>
    <xsl:variable name="first-closer" as="element()?" select="$elements[s:is-close-parenthesis(.)][1]"/>
    <xsl:variable name="first-opener-or-closer" as="element()?" select="($first-opener | $first-closer)[1]"/>
    <xsl:choose>
      <xsl:when test="exists($first-opener-or-closer) and string($first-opener-or-closer)=string($first-closer)">
        <!-- The first parenthesis found is a closer -->
        <xsl:choose>
          <xsl:when test="$closer-value!=''">
            <xsl:choose>
              <xsl:when test="string($first-closer)=$closer-value">
                <!-- This is our terminating closer, so return original sequence -->
                <xsl:copy-of select="$elements"/>
              </xsl:when>
              <xsl:otherwise>
                <!-- Wrong closer -->
                <s:fail message="First parenthesis found was the closer '{$first-closer}'">
                  <xsl:copy-of select="$elements"/>
                </s:fail>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <!-- No closer was expected here -->
            <s:fail message="Found unopened closer '{$first-closer}'">
              <xsl:copy-of select="$elements"/>
            </s:fail>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="exists($first-opener-or-closer) and string($first-opener-or-closer)=string($first-opener)">
        <!-- The first parenthesis found is an opener -->
        <xsl:variable name="following-first-opener" as="element()*" select="$elements[. &gt;&gt; $first-opener]"/>
        <xsl:variable name="required-closer-value" as="xs:string" select="s:get-matching-closer-value($first-opener)"/>
        <xsl:variable name="fenced-followers" as="element()*">
          <xsl:call-template name="fence-parentheses-until">
            <xsl:with-param name="elements" select="$following-first-opener"/>
            <xsl:with-param name="closer-value" select="$required-closer-value"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- First closer in result will be matching closer -->
        <xsl:variable name="matching-closer" as="element()?" select="$fenced-followers[self::mo and .=$required-closer-value][1]"/>
        <xsl:choose>
          <xsl:when test="exists($matching-closer)">
            <xsl:variable name="preceding-opener" as="element()*" select="$elements[. &lt;&lt; $first-opener]"/>
            <xsl:variable name="preceding-matching-closer" as="element()*" select="$fenced-followers[. &lt;&lt; $matching-closer]"/>
            <xsl:variable name="following-matching-closer" as="element()*" select="$fenced-followers[. &gt;&gt; $matching-closer]"/>
            <xsl:variable name="first-fenced" as="element()+">
              <xsl:copy-of select="$preceding-opener"/>
              <mfenced open="{string($first-opener)}" close="{$required-closer-value}">
                <!-- TODO: Maybe want to group across natural comma separators here? -->
                <xsl:call-template name="maybe-wrap-in-mrow">
                  <xsl:with-param name="elements" select="$preceding-matching-closer"/>
                </xsl:call-template>
              </mfenced>
              <xsl:copy-of select="$following-matching-closer"/>
            </xsl:variable>
            <xsl:call-template name="fence-parentheses-until">
              <xsl:with-param name="elements" select="$first-fenced"/>
              <xsl:with-param name="closer-value" select="$closer-value"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <s:fail message="Could not find matching closer for '{$first-opener}'">
              <xsl:copy-of select="$fenced-followers"/>
            </s:fail>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- No parentheses in this group so descend into children and fence them -->
        <xsl:apply-templates select="$elements" mode="fence-children"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mrow" mode="fence-children">
    <mrow>
      <xsl:call-template name="fence-parentheses">
        <xsl:with-param name="elements" select="*"/>
      </xsl:call-template>
    </mrow>
  </xsl:template>

  <xsl:template match="*|text()" mode="fence-children">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="fence-children"/>
    </xsl:copy>
  </xsl:template>

  <!-- ************************************************************ -->

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

  <xsl:function name="s:is-equals" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='='])"/>
  </xsl:function>

  <xsl:function name="s:is-addition" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='+'])"/>
  </xsl:function>

  <xsl:function name="s:is-minus" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='-'])"/>
  </xsl:function>

  <xsl:function name="s:is-divide" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='/'])"/>
  </xsl:function>

  <xsl:function name="s:is-explicit-multiplication" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and (.='*' or .='&#xd7;')])"/>
  </xsl:function>

  <xsl:function name="s:is-invertible-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mi and $invertible-elementary-functions=string(.)])"/>
  </xsl:function>

  <xsl:function name="s:is-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mi and $elementary-functions=string(.)])"/>
  </xsl:function>

  <!-- Tests for the equivalent of \sin, \sin^{.}. Result need not make any actual sense! -->
  <xsl:function name="s:is-recognised-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="s:is-elementary-function($element)
      or ($element[self::msup and s:is-elementary-function(*[1])])"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <xsl:template match="mrow">
    <mrow>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*"/>
      </xsl:call-template>
    </mrow>
  </xsl:template>

  <!-- Default template for other MathML elements -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template name="process-group">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <!-- TODO: Might be worth making a table of n-ary operators and their precedence?
         This would make it easy to add other things below... -->
    <xsl:choose>
      <xsl:when test="$elements[s:is-equals(.)]">
        <!-- Equals -->
        <xsl:call-template name="handle-equals-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[s:is-addition(.)]">
        <!-- Addition -->
        <xsl:call-template name="handle-add-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[s:is-minus(.)]">
        <!-- Subtraction -->
        <xsl:call-template name="handle-minus-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[s:is-explicit-multiplication(.)]">
        <!-- Explicit Multiplication, detected in various ways -->
        <xsl:call-template name="handle-explicit-multiplication-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($elements) &gt; 1">
        <!-- Need to infer function applications and multiplications, leave other operators as-is -->
        <xsl:call-template name="handle-default-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($elements)=1">
        <!-- "Atom" -->
        <xsl:apply-templates select="$elements[1]"/>
      </xsl:when>
      <xsl:when test="empty($elements)">
        <!-- Empty -> empty -->
      </xsl:when>
      <xsl:otherwise>
        <s:fail message="Could not process group">
          <xsl:copy-of select="$elements"/>
        </s:fail>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Equals group. This is associative and easy. -->
  <xsl:template name="handle-equals-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:for-each-group select="$elements" group-adjacent="s:is-equals(.)">
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <!-- Copy the equals operator -->
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="process-group">
                <xsl:with-param name="elements" select="current-group()"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <!-- Similar for addition -->
  <xsl:template name="handle-add-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:for-each-group select="$elements" group-adjacent="s:is-addition(.)">
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="process-group">
                <xsl:with-param name="elements" select="current-group()"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <!-- Subtraction Expression. Need to be more careful here as it is not associative -->
  <xsl:template name="handle-minus-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="minus-count" select="count($elements[s:is-minus(.)])" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$minus-count != 1">
        <!-- Something like 'a-b-c'. We handle this recursively as '(a-b)-c' -->
        <xsl:variable name="last-minus" select="$elements[s:is-minus(.)][position()=last()]" as="element()"/>
        <xsl:variable name="before-last-minus" select="$elements[. &lt;&lt; $last-minus]" as="element()+"/>
        <xsl:variable name="after-last-minus" select="$elements[. &gt;&gt; $last-minus]" as="element()*"/>
        <mrow>
          <xsl:call-template name="handle-minus-group">
            <xsl:with-param name="elements" select="$before-last-minus"/>
          </xsl:call-template>
        </mrow>
        <xsl:copy-of select="$last-minus"/>
        <xsl:call-template name="process-group">
          <xsl:with-param name="elements" select="$after-last-minus"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- Only one minus, so either '-a' or 'a-b' (or more pathologically '-' or 'a-').
             We will allow the pathoological cases here. -->
        <xsl:variable name="minus" select="$elements[s:is-minus(.)]" as="element()"/>
        <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $minus]" as="element()*"/>
        <xsl:variable name="right-operand" select="$elements[. &gt;&gt; $minus]" as="element()*"/>
        <xsl:call-template name="process-group">
          <xsl:with-param name="elements" select="$left-operand"/>
        </xsl:call-template>
        <xsl:copy-of select="$minus"/>
        <xsl:call-template name="process-group">
          <xsl:with-param name="elements" select="$right-operand"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Explicit multiplication -->
  <xsl:template name="handle-explicit-multiplication-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:for-each-group select="$elements" group-adjacent="s:is-explicit-multiplication(.)">
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="process-group">
                <xsl:with-param name="elements" select="current-group()"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="handle-default-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <!-- We split over any operators present and try to do magic on the bits in-between -->
    <xsl:for-each-group select="$elements" group-adjacent="boolean(self::mo)">
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="handle-consecutive-tokens">
            <xsl:with-param name="elements" select="current-group()"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="handle-consecutive-tokens">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <!-- Split into groups starting with applications of zero or more standard functions -->
    <xsl:for-each-group select="$elements" group-starting-with="*[s:is-recognised-function(.)
        and not(preceding-sibling::*[1][s:is-recognised-function(.)])]">
      <xsl:variable name="multiplicative-group" as="element()+" select="current-group()"/>
      <xsl:if test="position()!=1">
        <!-- Add an "Invisible Times" -->
        <mo>&#x2062;</mo>
      </xsl:if>
      <!-- Now apply any functions at the start of this sub-expression -->
      <xsl:call-template name="maybe-wrap-in-mrow">
        <xsl:with-param name="elements" as="element()*">
          <xsl:call-template name="apply-leading-functions">
            <xsl:with-param name="elements" select="$multiplicative-group"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="apply-leading-functions">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:choose>
      <xsl:when test="$elements[1][s:is-recognised-function(.)] and count($elements)!=1">
        <!-- This is a (prefix) function application. Copy the operator as-is -->
        <xsl:copy-of select="$elements[1]"/>
        <!-- Add an "Apply Function" operator -->
        <mo>&#x2061;</mo>
        <!-- Process the rest recursively -->
        <xsl:call-template name="apply-leading-functions">
          <xsl:with-param name="elements" select="$elements[position()!=1]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="maybe-wrap-in-mrow">
          <xsl:with-param name="elements" as="element()*">
            <!-- This is all of the stuff after the apply function -->
            <xsl:for-each select="$elements">
              <xsl:if test="position()!=1">
                <!-- Add an "Invisible Times" -->
                <mo>&#x2062;</mo>
              </xsl:if>
              <!-- Descend into the element itself -->
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
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

