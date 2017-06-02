/*$Id: TemplateImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.dcm4che.srom.Template;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class TemplateImpl implements Template {
    // Constants -----------------------------------------------------
    public static final Template TID_2010 =
            new TemplateImpl("2010","DCMR",null,null);
    
    private static final int MEANING_PROMPT_LEN = 20;

    // Attributes ----------------------------------------------------
    private final String templateIdentifier;
    private final String mappingResource;
    private final Long templateVersion;
    private final Long templateLocalVersion;

    // Constructors --------------------------------------------------
    public TemplateImpl(String templateIdentifier, String mappingResource,
            Date templateVersion, Date templateLocalVersion)
    {
        if ((this.templateIdentifier = templateIdentifier).length() == 0)
            throw new IllegalArgumentException();
        if ((this.mappingResource = mappingResource).length() == 0)
            throw new IllegalArgumentException();
        this.templateVersion = templateVersion != null
                ? new Long(templateVersion.getTime()) : null;
        this.templateLocalVersion = templateLocalVersion != null
                ? new Long(templateLocalVersion.getTime()) : null;
    }

    public TemplateImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.TemplateIdentifier),
            ds.getString(Tags.MappingResource),
            ds.getDate(Tags.TemplateVersion),
            ds.getDate(Tags.TemplateLocalVersion));
    }
    
    public static Template newTemplate(Dataset ds) throws DcmValueException {
        return ds != null ? new TemplateImpl(ds) : null;
    }
        
    // Methodes ------------------------------------------------------
    public String getTemplateIdentifier() { return templateIdentifier; }
    public String getMappingResource() { return mappingResource; }
    public Date getTemplateVersion() {
        return templateVersion != null
                ? new Date(templateVersion.longValue()) : null;
    }
    public Date getTemplateLocalVersion() {
        return templateLocalVersion != null
                ? new Date(templateLocalVersion.longValue()) : null;
    }
    
    //compares code value,coding scheme designator only
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof TemplateImpl))
            return false;
        TemplateImpl o = (TemplateImpl)obj;
        if (!templateIdentifier.equals(o.templateIdentifier))
            return false;
        if (!mappingResource.equals(o.mappingResource))
            return false;
        if (templateVersion == null
                ? o.templateVersion != null
                : !templateVersion.equals(o.templateVersion))
            return false;
        if (templateLocalVersion == null
                ? o.templateLocalVersion != null
                : !templateLocalVersion.equals(o.templateVersion))
            return false;
        return true;
    }        

    public int hashCode() { return templateIdentifier.hashCode(); }
    
    public String toString() {
        return "TID" + templateIdentifier + "@" + mappingResource;
    }

    public void toDataset(Dataset ds) {
        ds.putCS(Tags.TemplateIdentifier, templateIdentifier);
        ds.putCS(Tags.MappingResource, mappingResource);
        if (templateVersion != null) {
            ds.putDT(Tags.TemplateVersion,
                    new Date(templateVersion.longValue()));
        }
        if (templateLocalVersion != null) {
            ds.putDT(Tags.TemplateLocalVersion,
                    new Date(templateLocalVersion.longValue()));
            ds.putCS(Tags.TemplateExtensionFlag, "Y");
        }
    }
}
