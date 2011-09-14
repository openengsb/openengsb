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
package org.openengsb.ui.common;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;

import com.google.common.collect.Maps;

public class WicketAction {

    private Map<String, Action> attachedMetaData = Maps.newTreeMap();
    private Action action;
    private Component component;

    public WicketAction() {
    }

    public WicketAction(Component component, Action action) {
        this.component = component;
        this.action = action;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Map<String, Action> getAttachedMetaData() {
        return attachedMetaData;
    }

    public void setAttachedMetaData(Map<String, Action> attachedMetaData) {
        this.attachedMetaData = attachedMetaData;
    }

}
