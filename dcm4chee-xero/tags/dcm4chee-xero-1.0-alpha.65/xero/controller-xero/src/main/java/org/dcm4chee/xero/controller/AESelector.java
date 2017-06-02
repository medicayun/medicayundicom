package org.dcm4chee.xero.controller;

import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Chooses which AE to use, if any */
public class AESelector<T> implements Filter<T> {
   private static Logger log = LoggerFactory.getLogger(AESelector.class);

   /** The key to use for a particular ae */
   public static final String AE="ae";
   
   /** If the AE is set, adds it to the available filter items, IF it is an actual ae */
   public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
      String ae = FilterUtil.getString(params,"ae");
      if( ae !=null && ae.matches("[a-zA-Z_]+") ) {
         log.info("Running Xero against ae {}", ae);
         Map<String,Object> model = FilterUtil.getModel(params);
         model.put(AE, "&ae="+ae);
      }
      return filterItem.callNextFilter(params);
   }
   
   

}
