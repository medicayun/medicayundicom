/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1591 $ $Date: 2005-03-11 20:09:52 +0800 (周五, 11 3月 2005) $
 * @since 09.03.2005
 *
 */
public class WaveformModel extends InstanceModel {
	Dataset dsSeq;
    public WaveformModel(Dataset ds) {
        super(ds);
    }


    public String getWaveformType() {
    	String cuid = ds.getString(Tags.SOPClassUID);
    	if ( UIDs.TwelveLeadECGWaveformStorage.equals(cuid) ) return "12-lead ECG";
    	if (  UIDs.GeneralECGWaveformStorage.equals(cuid) ) return "General ECG";
    	if (  UIDs.AmbulatoryECGWaveformStorage.equals(cuid) ) return "Ambulatory ECG";
    	if (  UIDs.HemodynamicWaveformStorage.equals(cuid) ) return "Hemodynamic";
    	if (  UIDs.CardiacElectrophysiologyWaveformStorage.equals(cuid) ) return "Cardiac Electrophysiology";
    	if (  UIDs.BasicVoiceAudioWaveformStorage.equals(cuid) ) return "Basic voice audio";
    	return "unkown";
    }
    
    
}