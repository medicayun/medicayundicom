<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="VerifyingOrganization">Verifying Organization</xsl:param>
  <xsl:template match="/hl7">
    <dataset>
      <xsl:call-template name="common-attrs"/>
      <xsl:apply-templates select="PID"/>
      <xsl:apply-templates select="OBR[1]"/>
      <xsl:apply-templates select="ZBU"/>
      <!-- Identical Documents Sequence -->
      <attr tag="0040A525" vr="SQ">
        <xsl:apply-templates select="OBR[position()&gt;1]" mode="identical"/>
      </attr>
      <!-- Referenced Request Sequence -->
      <attr tag="0040A370" vr="SQ">
        <xsl:apply-templates select="OBR" mode="request"/>
      </attr>
      <!--Content Sequence-->
      <attr tag="0040A730" vr="SQ">
        <xsl:apply-templates select="ZBU" mode="obsctx"/>
        <xsl:apply-templates select="OBR[1]" mode="clinicalInfo"/>
        <xsl:apply-templates select="OBX"/>
        <xsl:apply-templates select="ZBU" mode="summary"/>
      </attr>
    </dataset>
  </xsl:template>
  <xsl:template name="common-attrs">
    <!-- Specific Character Set -->
    <attr tag="00080005" vr="CS">ISO_IR 100</attr>
    <!--SOP Class UID = Basic Text SR -->
    <attr tag="00080016" vr="UI">1.2.840.10008.5.1.4.1.1.88.11</attr>
    <!--Modality-->
    <attr tag="00080060" vr="CS">SR</attr>
    <!--Manufacturer-->
    <attr tag="00080070" vr="LO">Tiani Medgraph AG</attr>
    <!--Referring Physician's Name-->
    <attr tag="00080090" vr="PN"/>
    <!--Referenced Performed Procedure Step Sequence -->
    <attr tag="00081111" vr="SQ"/>
    <!--Study ID-->
    <attr tag="00200010" vr="SH"/>
    <!--Series Number-->
    <attr tag="00200011" vr="IS"/>
    <!--Instance Number-->
    <attr tag="00200013" vr="IS">1</attr>
    <!--Value Type-->
    <attr tag="0040A040" vr="CS" len="10">CONTAINER</attr>
    <!--Concept Name Code Sequence-->
    <attr tag="0040A043" vr="SQ" len="-1">
      <item off="360" len="-1">
        <!--Code Value-->
        <attr tag="00080100" vr="SH" len="8">11528-7</attr>
        <!--Coding Scheme Designator-->
        <attr tag="00080102" vr="SH" len="2">LN</attr>
        <!--Code Meaning-->
        <attr tag="00080104" vr="LO" len="16">Radiology Report</attr>
      </item>
    </attr>
    <!--Continuity Of Content-->
    <attr tag="0040A050" vr="CS" len="8">SEPARATE</attr>
    <!--Completion Flag-->
    <attr tag="0040A491" vr="CS">COMPLETE</attr>
    <!--Verification Flag-->
    <attr tag="0040A493" vr="CS">VERIFIED</attr>
    <!--Content Template Sequence-->
    <attr tag="0040A504" vr="SQ" len="-1">
      <item off="666" len="-1">
        <!--Mapping Resource-->
        <attr tag="00080105" vr="CS" len="4">DCMR</attr>
        <!--Template Identifier-->
        <attr tag="0040DB00" vr="CS" len="4">2000</attr>
      </item>
    </attr>
  </xsl:template>
  <xsl:template match="PID">
    <!--Patient's Name-->
    <attr tag="00100010" vr="PN">
      <xsl:call-template name="xpn2pn">
        <xsl:with-param name="xpn" select="field[5]"/>
      </xsl:call-template>
    </attr>
    <!--Patient ID-->
    <attr tag="00100020" vr="LO">
      <xsl:value-of select="field[3]/text()"/>
    </attr>
    <!--Issuer of Patient ID-->
    <attr tag="00100021" vr="LO">
      <xsl:value-of select="field[3]/component[3]"/>
    </attr>
    <!--Patient's Birth Date-->
    <attr tag="00100030" vr="DA">
      <xsl:value-of select="field[7]/text()"/>
    </attr>
    <!--Patient's Sex-->
    <attr tag="00100040" vr="CS">
      <xsl:value-of select="field[8]/text()"/>
    </attr>
  </xsl:template>
  <xsl:template name="xpn2pn">
    <xsl:param name="xpn"/>
    <xsl:param name="xpn25" select="$xpn/component"/>
    <xsl:value-of select="$xpn/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$xpn25[1]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$xpn25[2]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$xpn25[4]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$xpn25[3]/text()"/>
  </xsl:template>
  <xsl:template name="cn2pn">
    <xsl:param name="cn26"/>
    <xsl:value-of select="$cn26[1]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$cn26[2]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$cn26[3]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$cn26[5]/text()"/>
    <xsl:text>^</xsl:text>
    <xsl:value-of select="$cn26[4]/text()"/>
  </xsl:template>
  <xsl:template match="OBR">
    <xsl:variable name="acc_no" select="field[3]/text()"/>
    <xsl:variable name="study_dt" select="field[22]/text()"/>
    <!--Study Date-->
    <attr tag="00080020" vr="DA">
      <xsl:value-of select="substring($study_dt,1,8)"/>
    </attr>
    <!--Study Time-->
    <attr tag="00080030" vr="TM">
      <xsl:value-of select="substring($study_dt,9)"/>
    </attr>
    <!--Accession Number-->
    <attr tag="00080050" vr="SH">
      <xsl:value-of select="$acc_no"/>
    </attr>
    <!--Study Instance UID-->
    <attr tag="0020000D" vr="UI">
      <xsl:value-of select="field[3]/component[2]"/>
    </attr>
    <!--Placer Order Number / Imaging Service Request-->
    <attr tag="00402016" vr="LO">
      <xsl:value-of select="$acc_no"/>
    </attr>
    <!--Filler Order Number / Imaging Service Request-->
    <attr tag="00402017" vr="LO">
      <xsl:value-of select="$acc_no"/>
    </attr>
  </xsl:template>
  <xsl:template match="OBR" mode="identical">
    <item>
      <!--Study Instance UID-->
      <attr tag="0020000D" vr="UI">
        <xsl:value-of select="field[3]/component[2]"/>
      </attr>
    </item>
  </xsl:template>
  <xsl:template match="OBR" mode="request">
    <xsl:variable name="acc_no" select="field[3]/text()"/>
    <item>
      <!--Accession Number-->
      <attr tag="00080050" vr="SH">
        <xsl:value-of select="$acc_no"/>
      </attr>
      <!--Referenced Study Sequence-->
      <attr tag="00081110" vr="SQ"/>
      <!--Study Instance UID-->
      <attr tag="0020000D" vr="UI">
        <xsl:value-of select="field[3]/component[2]"/>
      </attr>
      <!--Requested Procedure Description-->
      <attr tag="00321060" vr="LO"/>
      <!--Requested Procedure Code Sequence-->
      <attr tag="00321064" vr="SQ"/>
      <!--Requested Procedure ID-->
      <attr tag="00401001" vr="SH"/>
      <!--Placer Order Number / Imaging Service Request-->
      <attr tag="00402016" vr="LO">
        <xsl:value-of select="$acc_no"/>
      </attr>
      <!--Filler Order Number / Imaging Service Request-->
      <attr tag="00402017" vr="LO">
        <xsl:value-of select="$acc_no"/>
      </attr>
    </item>
  </xsl:template>
  <xsl:template match="ZBU">
    <xsl:variable name="date" select="field[6]"/>
    <xsl:variable name="time" select="field[7]"/>
    <!--Content Date-->
    <attr tag="00080023" vr="DA">
      <xsl:value-of select="$date"/>
    </attr>
    <!--Content Time-->
    <attr tag="00080033" vr="TM">
      <xsl:value-of select="$time"/>
    </attr>
    <!-- Verifying Observer Sequence -->
    <attr tag="0040A073" vr="SQ">
      <item>
        <!-- Verifying Organization -->
        <attr tag="0040A027" vr="LO">
          <xsl:value-of select="$VerifyingOrganization"/>
        </attr>
        <!-- Verification DateTime -->
        <attr tag="0040A030" vr="DT">
          <xsl:value-of select="concat($date,$time)"/>
        </attr>
        <!-- Verifying Observer Name -->
        <attr tag="0040A075" vr="PN">
          <xsl:call-template name="cn2pn">
            <xsl:with-param name="cn26" select="field[3]/subcomponent"/>
          </xsl:call-template>
        </attr>
        <!-- Verifying Observer Identification Code Sequence -->
        <attr tag="0040A088" vr="SQ"/>
      </item>
    </attr>
  </xsl:template>
  <xsl:template match="ZBU" mode="obsctx">
    <item>
      <!--Relationship Type-->
      <attr tag="0040A010" vr="CS">HAS CONCEPT MOD</attr>
      <!--Value Type-->
      <attr tag="0040A040" vr="CS">CODE</attr>
      <!--Concept Name Code Sequence-->
      <attr tag="0040A043" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">121049</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">DCM</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">Language of Content Item and Descendants</attr>
        </item>
      </attr>
      <!--Concept Code Sequence-->
      <attr tag="0040A168" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">de</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">RFC3066</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">German</attr>
        </item>
      </attr>
    </item>
    <item>
      <!--Relationship Type-->
      <attr tag="0040A010" vr="CS">HAS OBS CONTEXT</attr>
      <!--Value Type-->
      <attr tag="0040A040" vr="CS">PNAME</attr>
      <!--Concept Name Code Sequence-->
      <attr tag="0040A043" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">121008</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">DCM</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">Person Observer Name</attr>
        </item>
      </attr>
      <!--Person Name-->
      <attr tag="0040A123" vr="PN">
        <xsl:call-template name="cn2pn">
          <xsl:with-param name="cn26" select="field[4]/subcomponent"/>
        </xsl:call-template>
      </attr>
    </item>
  </xsl:template>
  <xsl:template match="OBR" mode="obsctx">
    <item>
      <!--Relationship Type-->
      <attr tag="0040A010" vr="CS">HAS OBS CONTEXT</attr>
      <!--Value Type-->
      <attr tag="0040A040" vr="CS">UIDREF</attr>
      <!--Concept Name Code Sequence-->
      <attr tag="0040A043" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">121018</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">DCM</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">Procedure Study Instance UID</attr>
        </item>
      </attr>
      <!-- UID -->
      <attr tag="0040A124" vr="UI">
        <xsl:value-of select="field[3]/component[2]"/>
      </attr>
    </item>
    <item>
      <!--Relationship Type-->
      <attr tag="0040A010" vr="CS">HAS OBS CONTEXT</attr>
      <!--Value Type-->
      <attr tag="0040A040" vr="CS">CODE</attr>
      <!--Concept Name Code Sequence-->
      <attr tag="0040A043" vr="SQ">
        <item>
          <attr tag="00080100" vr="SH">121023</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">DCM</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">Procedure Code</attr>
        </item>
      </attr>
      <!--Concept Code Sequence-->
      <attr tag="0040A168" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">
            <xsl:value-of select="field[4]/text()"/>
          </attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">99ORBIS</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">
            <xsl:value-of select="field[4]/component[1]"/>
          </attr>
        </item>
      </attr>
    </item>
  </xsl:template>
  <xsl:template match="OBR" mode="procedure">
    <item>
      <!--Relationship Type-->
      <attr tag="0040A010" vr="CS">CONTAINS</attr>
      <!--Value Type-->
      <attr tag="0040A040" vr="CS">CONTAINER</attr>
      <!--Concept Name Code Sequence-->
      <attr tag="0040A043" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">121064</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">DCM</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">Current Procedure Descriptions</attr>
        </item>
      </attr>
      <!--Continuity Of Content-->
      <attr tag="0040A050" vr="CS">SEPARATE</attr>
      <!--Content Sequence-->
      <attr tag="0040A730" vr="SQ">
        <item>
          <!--Relationship Type-->
          <attr tag="0040A010" vr="CS">CONTAINS</attr>
          <!--Value Type-->
          <attr tag="0040A040" vr="CS">TEXT</attr>
          <!--Concept Name Code Sequence-->
          <attr tag="0040A043" vr="SQ">
            <item>
              <!--Code Value-->
              <attr tag="00080100" vr="SH">121065</attr>
              <!--Coding Scheme Designator-->
              <attr tag="00080102" vr="SH">DCM</attr>
              <!--Code Meaning-->
              <attr tag="00080104" vr="LO">Procedure Description</attr>
            </item>
          </attr>
          <!--Text Value-->
          <attr tag="0040A160" vr="UT">
            <xsl:value-of select="field[4]/component[1]"/>
          </attr>
        </item>
      </attr>
    </item>
  </xsl:template>
  <xsl:template match="OBR" mode="clinicalInfo">
    <xsl:variable name="clinicalInfo" select="field[13]"/>
    <xsl:variable name="history" select="substring-before($clinicalInfo,'$')"/>
    <xsl:variable name="reason"
      select="substring-before(substring-after(substring-after($clinicalInfo,'$'),'$'),'$')"/>
    <xsl:if test="$history">
      <item>
        <!--Relationship Type-->
        <attr tag="0040A010" vr="CS">CONTAINS</attr>
        <!--Value Type-->
        <attr tag="0040A040" vr="CS">CONTAINER</attr>
        <!--Concept Name Code Sequence-->
        <attr tag="0040A043" vr="SQ">
          <item off="1370">
            <!--Code Value-->
            <attr tag="00080100" vr="SH">121060</attr>
            <!--Coding Scheme Designator-->
            <attr tag="00080102" vr="SH">DCM</attr>
            <!--Code Meaning-->
            <attr tag="00080104" vr="LO">History</attr>
          </item>
        </attr>
        <!--Continuity Of Content-->
        <attr tag="0040A050" vr="CS">SEPARATE</attr>
        <!--Content Sequence-->
        <attr tag="0040A730" vr="SQ">
          <item>
            <!--Relationship Type-->
            <attr tag="0040A010" vr="CS">CONTAINS</attr>
            <!--Value Type-->
            <attr tag="0040A040" vr="CS">TEXT</attr>
            <!--Concept Name Code Sequence-->
            <attr tag="0040A043" vr="SQ">
              <item>
                <!--Code Value-->
                <attr tag="00080100" vr="SH">121060</attr>
                <!--Coding Scheme Designator-->
                <attr tag="00080102" vr="SH">DCM</attr>
                <!--Code Meaning-->
                <attr tag="00080104" vr="LO">History</attr>
              </item>
            </attr>
            <!--Text Value-->
            <attr tag="0040A160" vr="UT">
              <xsl:value-of select="$history"/>
            </attr>
          </item>
        </attr>
      </item>
    </xsl:if>
    <xsl:if test="$reason">
      <item>
        <!--Relationship Type-->
        <attr tag="0040A010" vr="CS">CONTAINS</attr>
        <!--Value Type-->
        <attr tag="0040A040" vr="CS">CONTAINER</attr>
        <!--Concept Name Code Sequence-->
        <attr tag="0040A043" vr="SQ">
          <item off="1370">
            <!--Code Value-->
            <attr tag="00080100" vr="SH">111401</attr>
            <!--Coding Scheme Designator-->
            <attr tag="00080102" vr="SH">DCM</attr>
            <!--Code Meaning-->
            <attr tag="00080104" vr="LO">Reason for procedure</attr>
          </item>
        </attr>
        <!--Continuity Of Content-->
        <attr tag="0040A050" vr="CS">SEPARATE</attr>
        <!--Content Sequence-->
        <attr tag="0040A730" vr="SQ">
          <item>
            <!--Relationship Type-->
            <attr tag="0040A010" vr="CS">CONTAINS</attr>
            <!--Value Type-->
            <attr tag="0040A040" vr="CS">TEXT</attr>
            <!--Concept Name Code Sequence-->
            <attr tag="0040A043" vr="SQ">
              <item>
                <!--Code Value-->
                <attr tag="00080100" vr="SH">111401</attr>
                <!--Coding Scheme Designator-->
                <attr tag="00080102" vr="SH">DCM</attr>
                <!--Code Meaning-->
                <attr tag="00080104" vr="LO">Reason for procedure</attr>
              </item>
            </attr>
            <!--Text Value-->
            <attr tag="0040A160" vr="UT">
              <xsl:value-of select="$reason"/>
            </attr>
          </item>
        </attr>
      </item>
    </xsl:if>
  </xsl:template>
  <xsl:template match="OBX">
    <xsl:apply-templates select="preceding-sibling::OBR[1]" mode="procedure"/>
    <item>
      <!--Relationship Type-->
      <attr tag="0040A010" vr="CS">CONTAINS</attr>
      <!--Value Type-->
      <attr tag="0040A040" vr="CS">CONTAINER</attr>
      <!--Concept Name Code Sequence-->
      <attr tag="0040A043" vr="SQ">
        <item>
          <!--Code Value-->
          <attr tag="00080100" vr="SH">121076</attr>
          <!--Coding Scheme Designator-->
          <attr tag="00080102" vr="SH">DCM</attr>
          <!--Code Meaning-->
          <attr tag="00080104" vr="LO">Conclusions</attr>
        </item>
      </attr>
      <!--Continuity Of Content-->
      <attr tag="0040A050" vr="CS">SEPARATE</attr>
      <!--Content Sequence-->
      <attr tag="0040A730" vr="SQ">
        <xsl:apply-templates select="preceding-sibling::OBR[1]" mode="obsctx"/>
        <item>
          <!--Relationship Type-->
          <attr tag="0040A010" vr="CS">CONTAINS</attr>
          <!--Value Type-->
          <attr tag="0040A040" vr="CS">TEXT</attr>
          <!--Concept Name Code Sequence-->
          <attr tag="0040A043" vr="SQ">
            <item>
              <!--Code Value-->
              <attr tag="00080100" vr="SH">121077</attr>
              <!--Coding Scheme Designator-->
              <attr tag="00080102" vr="SH">DCM</attr>
              <!--Code Meaning-->
              <attr tag="00080104" vr="LO">Conclusion</attr>
            </item>
          </attr>
          <!--Text Value-->
          <attr tag="0040A160" vr="UT">
            <xsl:value-of select="field[5]"/>
          </attr>
        </item>
      </attr>
    </item>
  </xsl:template>
  <xsl:template match="ZBU" mode="summary">
    <xsl:variable name="summary" select="field[1]/text()"/>
    <xsl:if test="$summary">
      <item>
        <!--Relationship Type-->
        <attr tag="0040A010" vr="CS">CONTAINS</attr>
        <!--Value Type-->
        <attr tag="0040A040" vr="CS">CONTAINER</attr>
        <!--Concept Name Code Sequence-->
        <attr tag="0040A043" vr="SQ">
          <item>
            <!--Code Value-->
            <attr tag="00080100" vr="SH">121111</attr>
            <!--Coding Scheme Designator-->
            <attr tag="00080102" vr="SH">DCM</attr>
            <!--Code Meaning-->
            <attr tag="00080104" vr="LO">Summary</attr>
          </item>
        </attr>
        <!--Continuity Of Content-->
        <attr tag="0040A050" vr="CS">SEPARATE</attr>
        <!--Content Sequence-->
        <attr tag="0040A730" vr="SQ">
          <item>
            <!--Relationship Type-->
            <attr tag="0040A010" vr="CS">CONTAINS</attr>
            <!--Value Type-->
            <attr tag="0040A040" vr="CS">TEXT</attr>
            <!--Concept Name Code Sequence-->
            <attr tag="0040A043" vr="SQ">
              <item>
                <!--Code Value-->
                <attr tag="00080100" vr="SH">121111</attr>
                <!--Coding Scheme Designator-->
                <attr tag="00080102" vr="SH">DCM</attr>
                <!--Code Meaning-->
                <attr tag="00080104" vr="LO">Summary</attr>
              </item>
            </attr>
            <!--Text Value-->
            <attr tag="0040A160" vr="UT">
              <xsl:value-of select="$summary"/>
            </attr>
          </item>
        </attr>
      </item>
    </xsl:if>
    <xsl:variable name="recommendation" select="field[2]/text()"/>
    <xsl:if test="$recommendation">
      <item>
        <!--Relationship Type-->
        <attr tag="0040A010" vr="CS">CONTAINS</attr>
        <!--Value Type-->
        <attr tag="0040A040" vr="CS">CONTAINER</attr>
        <!--Concept Name Code Sequence-->
        <attr tag="0040A043" vr="SQ">
          <item>
            <!--Code Value-->
            <attr tag="00080100" vr="SH">121074</attr>
            <!--Coding Scheme Designator-->
            <attr tag="00080102" vr="SH">DCM</attr>
            <!--Code Meaning-->
            <attr tag="00080104" vr="LO">Recommendations</attr>
          </item>
        </attr>
        <!--Continuity Of Content-->
        <attr tag="0040A050" vr="CS">SEPARATE</attr>
        <!--Content Sequence-->
        <attr tag="0040A730" vr="SQ">
          <item>
            <!--Relationship Type-->
            <attr tag="0040A010" vr="CS">CONTAINS</attr>
            <!--Value Type-->
            <attr tag="0040A040" vr="CS">TEXT</attr>
            <!--Concept Name Code Sequence-->
            <attr tag="0040A043" vr="SQ">
              <item>
                <!--Code Value-->
                <attr tag="00080100" vr="SH">121075</attr>
                <!--Coding Scheme Designator-->
                <attr tag="00080102" vr="SH">DCM</attr>
                <!--Code Meaning-->
                <attr tag="00080104" vr="LO">Recommendation</attr>
              </item>
            </attr>
            <!--Text Value-->
            <attr tag="0040A160" vr="UT">
              <xsl:value-of select="$recommendation"/>
            </attr>
          </item>
        </attr>
      </item>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
