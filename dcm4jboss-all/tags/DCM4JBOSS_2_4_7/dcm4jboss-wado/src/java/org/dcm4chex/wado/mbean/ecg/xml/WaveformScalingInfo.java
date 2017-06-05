/*
 * Created on 28.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg.xml;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveformScalingInfo {
	private float pixPerUnit = -1f;
	private float pixPerSec = -1f;
	private float zeroLine = 0.5f;
	private String xScaleDesc = null;
	private String yScaleDesc = null;

	
	/**
	 * Creates a WaveformScalingInfo object.
	 * <p>
	 * zeroLine will be 0.5f (the default)
	 * 
	 * @param pixPerUnit
	 * @param pixPerSec
	 * @param scaleDesc
	 * @param scaleDesc2
	 */
	public WaveformScalingInfo(float pixPerSec,String xScaleDesc, float pixPerUnit, String yScaleDesc) {
		this.pixPerUnit = pixPerUnit;
		this.pixPerSec = pixPerSec;
		this.xScaleDesc = xScaleDesc;
		this.yScaleDesc = yScaleDesc;
	}

	/**
	 * @return Returns the pixPerSec.
	 */
	public float getPixPerSec() {
		return pixPerSec;
	}
	/**
	 * @return Returns the pixPerUnit.
	 */
	public float getPixPerUnit() {
		return pixPerUnit;
	}
	/**
	 * @return Returns the xScaleDesc.
	 */
	public String getXScaleDesc() {
		return xScaleDesc;
	}
	/**
	 * @return Returns the yScaleDesc.
	 */
	public String getYScaleDesc() {
		return yScaleDesc;
	}
	/**
	 * Returns the offset factor of the zero line ( line of value 0 ).
	 * <p>
	 * Default is 0.5f (in the middle of the area).
	 * <p>
	 * <dl>
	 * <dt>This value ( 0 &lt;= x &lt;= 1 ) is used to calculate the zero line position:</dt>
	 * <dd><b>  yPos0 = yTop+height*x</b></dd>
	 * <dd></dd>
	 * <dd>     yPos0....y-coord of zero line</dd>
	 * <dd>     yTop.....y-coord of upperleft corner of area</dd>
	 * <dd>     height...height of area</dd>
	 * <dd>     x........This zero line offset factor</dd>
	 * 
	 * @return Returns the zeroLine offset factor.
	 */
	public float getZeroLine() {
		return zeroLine;
	}
	/**
	 * Set the zero line offset factor.
	 * <p>
	 * @see getZeroLine()
	 * 
	 * @param zeroLine The zeroLine to set.
	 */
	public void setZeroLine(float zeroLine) {
		this.zeroLine = zeroLine;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WaveformScalingInfo: pixPerSec:").append(pixPerSec).append(" (").append(xScaleDesc);
		sb.append(") pixPerUnit:").append(pixPerUnit).append(" (").append(yScaleDesc).append(") zeroLine:"+zeroLine);
		return sb.toString();
	}
}
