<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Austrian Association for Software Tool Integration (AASTI)
    under one or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information regarding copyright
    ownership. The AASTI licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file except in compliance
    with the License. You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->

<!-- 
  This stylesheet is based upon the Spring Framework stylesheet.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl"                
                version="1.0">
                
    <xsl:import href="urn:docbkx:stylesheet"/>
    <xsl:import href="highlight.xsl"/>
  
    <!--###################################################
                    HTML Settings
   ################################################### -->
  
    <!-- These extensions are required for table printing and other stuff -->
    <xsl:param name="tablecolumns.extension">0</xsl:param>
    <xsl:param name="graphicsize.extension">0</xsl:param>
    <xsl:param name="ignore.image.scaling">1</xsl:param>

    <!--###################################################
                     Table Of Contents
   ################################################### -->

    <!-- Generate the TOCs for named components only -->
    <xsl:param name="generate.toc">
        book toc
    </xsl:param>

    <!-- Show only Sections up to level 3 in the TOCs -->
    <xsl:param name="toc.section.depth">3</xsl:param>

    <!--###################################################
                        Labels
   ################################################### -->

    <!-- Label Chapters and Sections (numbering) -->
    <xsl:param name="chapter.autolabel">1</xsl:param>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="section.label.includes.component.label" select="1"/>

    <!--###################################################
                        Callouts
   ################################################### -->

    <!-- Use images for callouts instead of (1) (2) (3) -->
    <xsl:param name="callout.graphics">1</xsl:param>

    <!-- Place callout marks at this column in annotated areas -->
    <xsl:param name="callout.defaultcolumn">90</xsl:param>

    <!--###################################################
                      Admonitions
   ################################################### -->

    <!-- Use nice graphics for admonitions -->
    <xsl:param name="admon.graphics">1</xsl:param>
  <xsl:param name="admon.graphics.path">images/admons/</xsl:param>
    <!--###################################################
                         Misc
   ################################################### -->
    <!-- Placement of titles -->
    <xsl:param name="formal.title.placement">
        figure after
        example before
        equation before
        table before
        procedure before
    </xsl:param>
    <xsl:template match="author" mode="titlepage.mode">
        <xsl:if test="name(preceding-sibling::*[1]) = 'author'">
            <xsl:text>, </xsl:text>
        </xsl:if>
        <span class="{name(.)}">
            <xsl:call-template name="person.name"/> 
            (<xsl:value-of select="affiliation"/>)
            <xsl:apply-templates mode="titlepage.mode" select="./contrib"/>
        </span>
    </xsl:template>
    <xsl:template match="authorgroup" mode="titlepage.mode">
        <div class="{name(.)}">
            <h2>Authors</h2>
            <p/>
            <xsl:apply-templates mode="titlepage.mode"/>
        </div>
    </xsl:template>
   <!--###################################################
                     Headers and Footers
    ################################################### -->
    <xsl:template name="user.header.navigation">
    </xsl:template>

</xsl:stylesheet>

