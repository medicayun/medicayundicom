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
package org.dcm4chee.web.war.tc;

import java.util.ArrayList;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 27, 2011
 */
public class TCDetailsBibliographyTab extends Panel {

    private static final long serialVersionUID = 1L;

    public TCDetailsBibliographyTab(final String id) {
        super(id);

        ListView<String> list = new ListView<String>("bibliography-list",
                new Model<ArrayList<String>>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public ArrayList<String> getObject() {
                        TCDetails o = getTCObject();
                        ArrayList<String> biblio = o != null ? new ArrayList<String>(
                                o.getBibliographicReferences()) : null;

                        return biblio != null ? biblio : new ArrayList<String>(
                                0);
                    }
                }) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<String> item) {
                WebMarkupContainer wmc = new WebMarkupContainer(
                        "bibliography-separator") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean isVisible() {
                        return item.getIndex() < getList().size() - 1;
                    }
                };

                item.add(new MultiLineLabel("bibliography-text", item
                        .getModelObject()));
                item.add(wmc);
            }
        };

        add(list);
    }

    private TCDetails getTCObject() {
        return (TCDetails) getDefaultModelObject();
    }
}
