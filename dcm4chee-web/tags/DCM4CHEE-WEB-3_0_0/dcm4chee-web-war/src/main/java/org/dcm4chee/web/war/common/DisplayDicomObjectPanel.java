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

package org.dcm4chee.web.war.common;

import java.util.Iterator;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.ElementDictionary;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since Apr 07, 2011
 */
public class DisplayDicomObjectPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private static ElementDictionary dict = ElementDictionary.getDictionary();
    private final DicomObject dcmObj;
    private final WebMarkupContainer table;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("dicom.");
    
    public DisplayDicomObjectPanel(String id, final ModalWindow window, DicomObject dcmObj, String attrModelName) {
        super(id);
        
        if (DisplayDicomObjectPanel.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(DisplayDicomObjectPanel.BaseCSS));
        
        IModel<String> title = new ResourceModel("dicom.display.title."+attrModelName).wrapOnAssignment(this);
        try {
            title.getObject();
        } catch (Exception x) {
            title = new Model<String>("DICOM Object");
        }
        add(new Label("title", title));
        this.dcmObj = new BasicDicomObject();
        dcmObj.copyTo(this.dcmObj);
        table = new WebMarkupContainer("table");
        addHdrLabels(table);
        table.setOutputMarkupId(true);
        add(table);
        RepeatingView rv = new RepeatingView("elements") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onPopulate() {
                removeAll();
                addDicomObject(this, DisplayDicomObjectPanel.this.dcmObj, "",
                        new int[0]);
            }
        };
        table.add(rv);
    }

    private void addHdrLabels(WebMarkupContainer table) {
        table.add(new Label("nameHdr", new ResourceModel("dicom.nameHdr")).add(tooltipBehaviour));
        table.add(new Label("tagHdr", new ResourceModel("dicom.tagHdr")).add(tooltipBehaviour));
        table.add(new Label("vrHdr", new ResourceModel("dicom.vrHdr")).add(tooltipBehaviour));
        table.add(new Label("lenHdr", new ResourceModel("dicom.lenHdr")).add(tooltipBehaviour));
        table.add(new Label("valueHdr", new ResourceModel("dicom.valueHdr")).add(tooltipBehaviour));
    }

    protected DicomObject getDicomObject() {
        return dcmObj;
    }

    protected void onApply() {}

    protected void onSubmit() {}

    protected void onCancel() {}

    private void addDicomObject(RepeatingView rv, DicomObject dcmObj,
            String nesting, int[] itemPath) {
        final SpecificCharacterSet cs = dcmObj.getSpecificCharacterSet();
        final String nesting1 = nesting + '>';
        for (Iterator<DicomElement> it = dcmObj.iterator(); it.hasNext();) {
            DicomElement el = it.next();
            final int[] tagPath = new int[itemPath.length+1];
            tagPath[itemPath.length] = el.tag();
            System.arraycopy(itemPath, 0, tagPath, 0, itemPath.length);
            WebMarkupContainer elrow = new WebMarkupContainer(rv.newChildId());
            rv.add(elrow);
            elrow.add(
                    new ElementFragment("fragment", el, cs, tagPath, nesting));
            if (el.hasDicomObjects()) {
                int numitems = el.countItems();
                for (int i = 0; i < numitems; i++) {
                    final int[] itemPath1 = new int[tagPath.length+1];
                    System.arraycopy(tagPath, 0, itemPath1, 0, tagPath.length);
                    itemPath1[tagPath.length] = i;
                    WebMarkupContainer itemrow =
                        new WebMarkupContainer(rv.newChildId());
                    rv.add(itemrow);
                    DicomObject item = el.getDicomObject(i);
                    itemrow.add(new ItemFragment("fragment", tagPath, i,
                            nesting1));
                    addDicomObject(rv, item, nesting1, itemPath1);
                }
            }
        }
    }

    public class ElementFragment extends Fragment {

        private static final long serialVersionUID = 1L;

        public ElementFragment(String id, DicomElement el,
                SpecificCharacterSet cs, final int[] tagPath, String nesting) {
            super(id, "element", DisplayDicomObjectPanel.this);
            final int tag = el.tag();
            add(new Label("name", nesting + dict.nameOf(tag)));
            add(new Label("tag", TagUtils.toString(tag)));
            add(new Label("vr", el.vr().toString()));
            add(new Label("length", Integer.toString(el.length())));
            add(new TextField<String>("value", el.hasItems() ? 
                                                new Model<String>("") 
                                              : new DicomElementModel(el, cs, tagPath))
                .setVisible(!el.hasItems()).setEnabled(false));
        }
     }

    public class ItemFragment extends Fragment {

        private static final long serialVersionUID = 1L;

        public ItemFragment(String id, final int[] tagPath,
                final int itemIndex, String nesting) {
            super(id, "item", DisplayDicomObjectPanel.this);
            add(new Label("name", nesting + "Item #" + (itemIndex+1)));
        }
     }

    private class DicomElementModel extends Model<String> {

        private static final long serialVersionUID = 1L;
        private final int[] tagPath;
        private final int vr;
 
        public DicomElementModel(DicomElement el, SpecificCharacterSet cs,
                int[] tagPath) {
            super(el.getValueAsString(cs, 64));
            this.vr = el.vr().code();
            this.tagPath = tagPath.clone();
        }

        @Override
        public void setObject(String object) {
            Object prev = super.getObject();
            if (vr != 0) {
                if (object == null) {
                    DisplayDicomObjectPanel.this.dcmObj.putNull(tagPath, 
                            VR.valueOf(vr));
                } else if (!object.equals(prev)) {
                    DisplayDicomObjectPanel.this.dcmObj.putString(tagPath,
                            VR.valueOf(vr), object);
                }
            }
            super.setObject(object);
        }
    }
}
