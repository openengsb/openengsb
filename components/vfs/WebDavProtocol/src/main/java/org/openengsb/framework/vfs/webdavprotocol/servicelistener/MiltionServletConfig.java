/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

import io.milton.servlet.MiltonServlet;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 *
 * @author Richard
 */
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