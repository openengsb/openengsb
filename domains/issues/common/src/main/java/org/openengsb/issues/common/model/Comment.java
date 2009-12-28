/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.openengsb.issues.common.model;

import java.util.Date;

public class Comment {

    private String id;
    private String reporter;
    private String text;
    private ViewState viewState;
    private Date creationTime;
    private Date lastChange;

    /**
     * Supplies the identifier of the comment
     *
     * @return id - identifier of the comment
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the comment to identify it.
     *
     * @param id - the id identifies the comment
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the reporter of the comment
     *
     * @return reporter - author of the comment
     */
    public String getReporter() {
        return reporter;
    }

    /**
     * Sets the reporter of the comment
     *
     * @param reporter - author of the comment
     */
    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    /**
     * Returns the text of the comment
     *
     * @return text - content of the comment
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the comment
     *
     * @param text - the content of the comment
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the view state of the comment
     *
     * @return viewState - public, private or any
     * @see ViewState
     */
    public ViewState getViewState() {
        return viewState;
    }

    /**
     * Sets the state of the view for the comment It decides if everybody is
     * allowed to read it. The default value is public.
     *
     * @param viewState - uses the enumeration for setting
     * @see ViewState
     */
    public void setViewState(ViewState viewState) {
        this.viewState = viewState;
    }

    /**
     * Returns the creation time of the comment
     *
     * @return creationTime - time the comment was created
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the creation time of the comment due to the issue tracker
     *
     * @param creationTime - time the comment was created
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Returns the last change time
     *
     * @return lastChange - the time the last change was committed
     */
    public Date getLastChange() {
        return lastChange;
    }

    /**
     * Sets the time the last change was updated
     *
     * @param lastChange - the time the last change was updated
     */
    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

}