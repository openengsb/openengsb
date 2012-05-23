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

package org.openengsb.ui.admin.global;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class BookmarkablePageLabelLink<Type extends WebPage> extends BookmarkablePageLink<Type> {

    private String label;

    /**
     * @param id
     * @param pageClass
     * @param parameters
     */
    public BookmarkablePageLabelLink(String id, Class<Type> pageClass, PageParameters parameters,
            String label) {
        super(id, pageClass, parameters);
        this.label = label;
    }

    /**
     * @param id
     * @param pageClass
     */
    public BookmarkablePageLabelLink(String id, Class<Type> pageClass, String label) {
        super(id, pageClass);
        this.label = label;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.apache.wicket.markup.html.link.AbstractLink#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     */
    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        replaceComponentTagBody(markupStream, openTag, this.label);
    }
}
