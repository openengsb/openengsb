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

package org.openengsb.ui.common.model;

import org.apache.wicket.model.IModel;
import org.openengsb.core.api.OAuthData;

/**
 * The wicket {@link IModel} storing {@link OAuthData} in a reusable object.
 */
public class OAuthPageModel implements IModel<OAuthData> {
    private static final long serialVersionUID = -4841795218087845120L;
    private IModel<OAuthData> oAuthContainingModel;

    public OAuthPageModel(IModel<OAuthData> oAuthContainingModel) {
        this.oAuthContainingModel = oAuthContainingModel;
    }

    @Override
    public OAuthData getObject() {
        return oAuthContainingModel.getObject();
    }

    @Override
    public void setObject(OAuthData object) {
        oAuthContainingModel.setObject(object);
    }

    @Override
    public void detach() {
        oAuthContainingModel.detach();
    }

}
