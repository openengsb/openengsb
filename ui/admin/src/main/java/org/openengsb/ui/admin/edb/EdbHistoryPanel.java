/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.ui.admin.edb;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.security.annotation.SecurityAttributes;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@SecurityAttributes({
    @SecurityAttribute(key = "org.openengsb.ui.component", value = "EDB"),
})
@PaxWicketMountPoint(mountPoint = "edbhistory")
public class EdbHistoryPanel extends BasePage {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.LONG,
        Locale.getDefault());
    @PaxWicketBean(name = "edbService")
    private EngineeringDatabaseService edbService;

    public EdbHistoryPanel() {
        init("");
    }

    public EdbHistoryPanel(String uuid) {
        init(uuid);
    }

    private void init(String uuid) {
        setOutputMarkupId(true);
        List<EDBLogEntry> history = edbService.getLog("null/null/" + uuid, 0L, System.currentTimeMillis());
        add(new ListView<EDBLogEntry>("history", history) {
            @Override
            protected void populateItem(ListItem<EDBLogEntry> item) {
                EDBLogEntry modelObject = item.getModelObject();
                String committer = modelObject.getCommit().getCommitter();
                Long timestamp = modelObject.getCommit().getTimestamp();
                Date date = new Date(timestamp);

                item.add(new Label("time", DATE_FORMAT.format(date)));
                item.add(new Label("committer", committer));
            }
        });
    }
}
