package org.dcm4chex.archive.mbean;

import org.dcm4chex.archive.common.ActionOrder;

/**
 * Timer order message for JMS queue.
 */
public class TimerOrder extends ActionOrder {
		
	private static final long serialVersionUID = 4379396988616021498L;

	public TimerOrder(String actionMethod)
	{		
		super(actionMethod);
	}	
}
