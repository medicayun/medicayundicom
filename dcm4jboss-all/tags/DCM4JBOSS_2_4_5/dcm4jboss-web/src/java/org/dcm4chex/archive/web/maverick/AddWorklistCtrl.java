/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1700 $ $Date: 2005-04-15 20:50:21 +0800 (周五, 15 4月 2005) $
 * @since 5.10.2004
 *
 */
public class AddWorklistCtrl extends Dcm4JbossController {

    /** Popup message */
    private String popupMsg = null;

    private int studyPk;
	private static GPWLFeedDelegate delegate = null;;

	private String template;
	private String humanPerformer;
	private long scheduleDate = System.currentTimeMillis()+60000;
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public final int getStudyPk() {
        return studyPk;
    }

    public final void setStudyPk(int pk) {
        this.studyPk = pk;
    }

	/**
	 * @return Returns the humanPerformer.
	 */
	public String getHumanPerformer() {
		return humanPerformer;
	}
	/**
	 * @param humanPerformer The humanPerformer to set.
	 */
	public void setHumanPerformer(String humanPerformer) {
		this.humanPerformer = humanPerformer;
	}
	/**
	 * @return Returns the scheduleDate.
	 */
	public String getScheduleDate() {
		return formatter.format( new Date(scheduleDate) );
	}
	/**
	 * @param scheduleDate The scheduleDate to set.
	 * @throws ParseException
	 */
	public void setScheduleDate(String scheduleDate) throws ParseException {
		this.scheduleDate = formatter.parse(scheduleDate).getTime();
	}
	/**
	 * @return Returns the template.
	 */
	public String getTemplate() {
		return template;
	}
	/**
	 * @param template The template to set.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
    public List getHumanPerformerList() {
    	return delegate.getHumanPerformerList();
    }
    public List getTemplateList() {
    	return delegate.getTemplateList();
    }
    
	/**
	 * @return Returns the popupMsg.
	 */
	public String getPopupMsg() {
		return popupMsg;
	}

    protected void init() {
    	popupMsg = null;
        if ( delegate  == null ) {
        	delegate = new GPWLFeedDelegate();
        	try {
        		delegate.init( getCtx() );
        	} catch( Exception x ) {
        		x.printStackTrace();
        	}
        }
    }

    protected String perform() throws Exception {
    	init();
        HttpServletRequest rq = getCtx().getRequest();
        if (rq.getParameter("add") != null
                || rq.getParameter("add.x") != null) { return addWorklistItem(); }
        if (rq.getParameter("cancel") != null
                || rq.getParameter("cancel.x") != null) { return "cancel"; }
    	return "success";
    }

	/**
	 * @return
	 * @throws ParseException
	 */
	private String addWorklistItem() throws ParseException {
		if ( delegate.addWorklistItem( studyPk, template, humanPerformer, scheduleDate ) ) {
			//popupMsg = "New worklist item added!"; (see maverick.xml view type folder in command addWorklist)
			return "folder";
		} else {
			popupMsg = "Failed to add new worklist item!";
			return "success";//to open AddWorklistCtrl!
		}
	}
    
}