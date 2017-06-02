/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.web.common.markup;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.model.IModel;
import org.dcm4chee.web.common.util.DateUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 07, 2010
 */
public class SimpleDateTimeField extends FormComponentPanel<Date> implements ITextFormatProvider {

    private static final long serialVersionUID = 1L;
    
    private boolean max;
    private DateTextField dateField;
    private TimeField timeField;
    private boolean withoutTime;

    private static Logger log = LoggerFactory.getLogger(SimpleDateTimeField.class);
    
    public SimpleDateTimeField(String id, IModel<Date> model) {
        super(id, model);
        setType(Date.class);
        dateField = new DateTextField("dateField", new DateModel(this), 
                new StyleDateConverter("S-", false) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected DateTimeFormatter getFormat() {
                        String pattern = DateUtils.getDatePattern(getComponent());
                        return DateTimeFormat.forPattern(pattern).withLocale(getLocale())
                                        .withPivotYear(2000);
                    }
            });
        dateField.add(new DatePicker() {
            private static final long serialVersionUID = 1L;
            protected boolean enableMonthYearSelection() {
                    return true;
            }
        });
        add(dateField);
        timeField = new TimeField("timeField", new TimeModel(this)) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isVisible() {
                return !withoutTime;
            }
        };
        add(timeField);
    }
    public SimpleDateTimeField(String id, IModel<Date> model, boolean max) {
        this(id, model);
        this.max = max;
    }
    
    public SimpleDateTimeField setWithoutTime(boolean b) {
        withoutTime = b;
        return this;
    }
    
    DateTextField getDateField() {
        return this.dateField;
    }
    
    public TimeField getTimeField() {
        return this.timeField;
    }

    public boolean isWithoutTime() {
        return withoutTime;
    }
    
    @Override
    public void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if ( tag.getAttribute("class") == null )
            tag.put("class", "dateTimeField");
    }
    
    @Override
    public String getInput() {
        return dateField.getInput() + ", " + timeField.getInput();
    }
    
    @Override
    protected void convertInput() {
        Date d = dateField.getConvertedInput();
        Date t = timeField.getConvertedInput();
        if (d == null) {
            if (t != null) {
                timeField.setConvertedInput(null);
            }
            setConvertedInput(null);
        } else {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(d);
            if (t == null) {
                DateUtils.setTimeToMinOrMax(cal, max);
                timeField.getModel().setObject(cal.getTime());
                timeField.setConvertedInput(cal.getTime());
            } else {                
                GregorianCalendar calT = new GregorianCalendar();
                calT.setTime(t);
                if (max && calT.get(Calendar.HOUR_OF_DAY) == 0 &&
                        calT.get(Calendar.MINUTE) == 0 && calT.get(Calendar.SECOND) == 0) {
                    DateUtils.setTimeToMinOrMax(cal, true);
                } else {
                    int h = calT.get(Calendar.HOUR_OF_DAY);
                    int m = calT.get(Calendar.MINUTE);
                    cal.set(Calendar.HOUR_OF_DAY, h);
                    cal.set(Calendar.MINUTE, m);
                    if (h == 23 && m == 59) {
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);                        
                    } else {
                        cal.set(Calendar.SECOND, calT.get(Calendar.SECOND));
                        cal.set(Calendar.MILLISECOND, calT.get(Calendar.MILLISECOND));
                    }
                }
            }
            setConvertedInput(cal.getTime());
        }
        log.debug("Converted Input:{}", getConvertedInput());
    }
    
    public Component addToDateField(final IBehavior... behaviors) {
        this.dateField.add(behaviors);
        return this;
    }
    public Component addToTimeField(final IBehavior... behaviors) {
        this.timeField.add(behaviors);
        return this;
    }

    public String getTextFormat() {
        return DateUtils.getDatePattern(this);
    }
    
    private class DateModel implements IModel<Date> {
        
        private static final long serialVersionUID = 1L;
        
        protected SimpleDateTimeField tf;

        public DateModel(SimpleDateTimeField tf) {
            this.tf = tf;
        }
        
        public Date getObject() {
            return tf.getModelObject();
        }

        public void setObject(Date object) {
            if ( object != null) {
                IModel<Date> model = tf.getModel();
                Date oldDate = model.getObject();
                if (oldDate == null) {
                    model.setObject(object);
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(oldDate);
                    Calendar newCal = Calendar.getInstance();
                    newCal.setTime(object);
                    cal.set(Calendar.YEAR, newCal.get(Calendar.YEAR));
                    cal.set(Calendar.MONTH, newCal.get(Calendar.MONTH));
                    cal.set(Calendar.DAY_OF_MONTH, newCal.get(Calendar.DAY_OF_MONTH));
                    model.setObject(cal.getTime());
                }
            }
        }

        public void detach() {}
    }
    
    private class TimeModel extends DateModel {
        private static final long serialVersionUID = 1L;

        public TimeModel(SimpleDateTimeField tf) {
            super(tf);
        }
        
        public void setObject(Date object) {
            IModel<Date> model = tf.getModel();
            Date oldDate = model.getObject();
            if (oldDate == null) {
                if (object != null)
                    model.setObject(object);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(oldDate);
                if (object != null) {
                    Calendar newCal = Calendar.getInstance();
                    newCal.setTime(object);
                    cal.set(Calendar.HOUR_OF_DAY, newCal.get(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, newCal.get(Calendar.MINUTE));
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                }
                model.setObject(cal.getTime());
            }
        }
    }
}
