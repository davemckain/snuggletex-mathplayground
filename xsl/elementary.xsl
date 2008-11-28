<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s m"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:strip-space elements="m:*"/>

  <!--

  TODO:

  * logs to other bases?
  * Sets via {a,b,c} ?

  -->

  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="invertible-elementary-functions" as="xs:string+"
    select="('sin','cos','tan')"/>

  <xsl:variable name="elementary-functions" as="xs:string+"
    select="($invertible-elementary-functions,
            'ln', 'log', 'exp')"/>

  <xsl:function name="s:is-addition" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='+'])"/>
  </xsl:function>

  <xsl:function name="s:is-minus" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='-'])"/>
  </xsl:function>

  <xsl:function name="s:is-times" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='&#x2062;'])"/>
  </xsl:function>

  <xsl:function name="s:is-function-application" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='&#x2061;'])"/>
  </xsl:function>

  <xsl:template match="wrapper">
    <xsl:apply-templates select="math[1]"/>
  </xsl:template>

  <xsl:template match="math">
    <!-- Deal with any existing <semantics/> element -->
    <xsl:variable name="presentation-mathml" select="if (semantics) then semantics/*[1] else *" as="element()*"/>
    <xsl:variable name="other-annotations" select="if (semantics) then semantics/*[position() != 1] else ()" as="element()*"/>
    <!-- Try to create Content MathML -->
    <xsl:variable name="content-mathml" as="element()*">
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="$presentation-mathml"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- If everything converted fine then output the Content MathML, otherwise leave as-is -->
    <xsl:choose>
      <xsl:when test="$content-mathml//s:fail">
        <!-- Conversion failed. Output reason as a special annotation -->
        <math>
          <semantics>
            <xsl:copy-of select="$presentation-mathml"/>
            <xsl:copy-of select="$other-annotations"/>
            <xsl:for-each select="$content-mathml//s:fail">
              <annotation encoding="SnuggleTeX-to-Content-MathML-failure-reason">
                <xsl:value-of select="@reason"/>
              </annotation>
            </xsl:for-each>
          </semantics>
        </math>
      </xsl:when>
      <xsl:otherwise>
        <math>
          <semantics>
            <xsl:copy-of select="$content-mathml"/>
            <annotation-xml encoding="MathML-Presentation">
              <xsl:copy-of select="$presentation-mathml"/>
            </annotation-xml>
            <xsl:copy-of select="$other-annotations"/>
          </semantics>
        </math>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mrow">
    <xsl:call-template name="process-group">
      <xsl:with-param name="elements" select="*"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="mfenced[@open='(' and @close=')' and count(*)=1]">
    <!-- Treat this as (...), which basically means we treat the content as a single group -->
    <xsl:call-template name="process-group">
      <xsl:with-param name="elements" select="*[1]"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="mfenced">
    <!--
    No logic currently for other types of fences. It's not clear what to do anyway.
    For example, something like (1,2) could be a 2-dimensional vector or an open interval!
    -->
    <s:fail reason="Can't handle this type of fence"/>
  </xsl:template>

  <xsl:template name="process-group">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
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
      <xsl:when test="$elements[s:is-times(.)]">
        <!-- Explicit multiplication -->
        <xsl:call-template name="handle-multiplication-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[s:is-function-application(.)]">
        <!-- Function Application -->
        <xsl:call-template name="handle-function-application-group">
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
    </xsl:choose>
  </xsl:template>

  <!--
  Addition Expression. This is nice and easy since it is associative.
  We do however need to check for the pathological case of 'a+'
  -->
  <xsl:template name="handle-add-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <apply>
      <plus/>
      <xsl:for-each-group select="$elements" group-adjacent="s:is-addition(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:if test="not(following-sibling::*[1])">
              <s:fail reason="Nothing following addition operator"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="process-group">
              <xsl:with-param name="elements" select="current-group()"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </apply>
  </xsl:template>

  <!-- Subtraction Expression. Need to be very careful with this as it is not associative! -->
  <xsl:template name="handle-minus-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="minus-count" select="count($elements[s:is-minus(.)])" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$minus-count != 1">
        <!-- Something like 'a-b-c'. We handle this recursively as '(a-b)-c' -->
        <xsl:variable name="last-minus" select="$elements[s:is-minus(.)][position()=last()]" as="element()"/>
        <xsl:variable name="before-last-minus" select="$elements[. &lt;&lt; $last-minus]" as="element()+"/>
        <xsl:variable name="after-last-minus" select="$elements[. &gt;&gt; $last-minus]" as="element()*"/>
        <apply>
          <minus/>
          <xsl:call-template name="handle-minus-group">
            <xsl:with-param name="elements" select="$before-last-minus"/>
          </xsl:call-template>
          <xsl:call-template name="process-group">
            <xsl:with-param name="elements" select="$after-last-minus"/>
          </xsl:call-template>
        </apply>
      </xsl:when>
      <xsl:otherwise>
        <!-- Only one minus, so either '-a' or 'a-b' (or more pathologically '-' or 'a-') -->
        <xsl:variable name="minus" select="$elements[s:is-minus(.)]" as="element()"/>
        <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $minus]" as="element()*"/>
        <xsl:variable name="right-operand" select="$elements[. &gt;&gt; $minus]" as="element()*"/>
        <xsl:choose>
          <xsl:when test="empty($right-operand)">
            <s:fail reason="Nothing following subtraction operator"/>
          </xsl:when>
          <xsl:otherwise>
            <apply>
              <minus/>
              <xsl:call-template name="process-group">
                <xsl:with-param name="elements" select="$left-operand"/>
              </xsl:call-template>
              <xsl:call-template name="process-group">
                <xsl:with-param name="elements" select="$right-operand"/>
              </xsl:call-template>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Explicit Multiplicative Expression. This is again easy since it is associative. -->
  <xsl:template name="handle-multiplication-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <apply>
      <times/>
      <xsl:for-each-group select="$elements" group-adjacent="s:is-times(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:if test="not(following-sibling::*[1])">
              <s:fail reason="Nothing following multiplication operator"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="process-group">
              <xsl:with-param name="elements" select="current-group()"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </apply>
  </xsl:template>

  <!--
  Function Application. Denoting 'o' as 'apply function' here, then 'fogoh'
  is treated as 'fo(goh)' so it's quite easy to implement this.
  -->
  <xsl:template name="handle-function-application-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="first-apply" select="$elements[s:is-function-application(.)][1]" as="element()"/>
    <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $first-apply]" as="element()+"/>
    <xsl:variable name="after-first-apply" select="$elements[. &gt;&gt; $first-apply]" as="element()*"/>
    <xsl:choose>
      <xsl:when test="count($left-operand)!=1">
        <s:fail reason="Expected single element preceding function application"/>
      </xsl:when>
      <xsl:when test="empty($after-first-apply)">
        <s:fail reason="Expected something after the function application"/>
      </xsl:when>
      <xsl:otherwise>
        <apply>
          <xsl:call-template name="create-elementary-function-operator">
            <xsl:with-param name="operand-element" select="$left-operand"/>
          </xsl:call-template>
          <xsl:call-template name="process-group">
            <xsl:with-param name="elements" select="$after-first-apply"/>
          </xsl:call-template>
        </apply>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="create-elementary-function-operator">
    <xsl:param name="operand-element" as="element()" required="yes"/>
    <xsl:choose>
      <xsl:when test="$operand-element[self::msup and *[1][self::mi] and *[2][self::mn and .='-1']]">
        <!-- It looks like an inverse function. Make sure we know about it -->
        <xsl:variable name="function" select="string($operand-element/*[1])" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$invertible-elementary-functions=$function">
            <xsl:element name="arc{$function}"/>
          </xsl:when>
          <xsl:otherwise>
            <s:fail reason="Unknown inverse function {$function}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$operand-element[self::mi]">
        <xsl:variable name="function" select="string($operand-element)" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$elementary-functions=$function">
            <!-- Create Content MathML element with same name as content of <mi/> element -->
            <xsl:element name="{$function}"/>
          </xsl:when>
          <xsl:otherwise>
            <s:fail reason="Unknown function {$function}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <s:fail reason="Unhandled operand element {$operand-element/local-name()}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="mn">
    <cn><xsl:value-of select="."/></cn>
  </xsl:template>

  <xsl:template match="mi">
    <ci><xsl:value-of select="."/></ci>
  </xsl:template>

  <xsl:template match="mfrac">
    <!-- Fractions are relatively easy to cope with here! -->
    <apply>
      <divide/>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- TODO: Should this be activated via a parameter? -->
  <xsl:template match="msup[*[1][self::mi and .='e']]">
    <!-- Treat as exponential -->
    <apply>
      <exp/>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <xsl:template match="msup">
    <!-- We interpret these as powers -->
    <apply>
      <power/>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <xsl:template match="msqrt">
    <apply>
      <root/>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <xsl:template match="mroot">
    <apply>
      <root/>
      <degree>
        <xsl:call-template name="process-group">
          <xsl:with-param name="elements" select="*[1]"/>
        </xsl:call-template>
      </degree>
      <xsl:call-template name="process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>


</xsl:stylesheet>
