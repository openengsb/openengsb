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

package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

import io.milton.servlet.MiltonServlet;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class MiltionServletConfig implements ServletConfig {

    private HashMap<String, String> parameters = new HashMap<String, String>();

    public MiltionServletConfig() {
        parameters.put("resource.factory.class",
                "org.openengsb.framework.vfs.webDavProtocol.Factories.ResourceFactoryImpl");
    }

    @Override
    public String getServletName() {
        return MiltonServlet.class.toString();
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public String getInitParameter(String string) {
        if (parameters.containsKey(string)) {
            return parameters.get(string);
        }
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        return new InitEnum();
    }

    public class InitEnum implements Enumeration {

        private int readCount = 0;

        public InitEnum() {
        }

        @Override
        public boolean hasMoreElements() {
            if (readCount < parameters.size()) {
                return true;
            }

            return false;
        }

        @Override
        public Object nextElement() {
            return (String) (parameters.keySet().toArray()[readCount++]);
        }
    }
}
