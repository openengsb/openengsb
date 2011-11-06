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
    Simple highlighter for HTML output. Follows the Eclipse color scheme.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl"
                version='1.0'>

  <xsl:template match='xslthl:keyword'>
    <span class="hl-keyword"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:comment'>
    <span class="hl-comment"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:oneline-comment'>
    <span class="hl-comment"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:multiline-comment'>
    <span class="hl-multiline-comment"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:tag'>
    <span class="hl-tag"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:attribute'>
    <span class="hl-attribute"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:value'>
    <span class="hl-value"><xsl:value-of select='.'/></span>
  </xsl:template>
  
  <xsl:template match='xslthl:string'>
    <span class="hl-string"><xsl:value-of select='.'/></span>
  </xsl:template>

</xsl:stylesheet>
