/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.swingclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class Start {

    private JmsService jmsService;

    public JmsService getJmsService() {
        return jmsService;
    }

    public void setJmsService(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        BeanFactory factory = context;
        final Start start = (Start) factory.getBean("start");
        Resource resource = context.getResource("classpath:/xbean.xml");

        final List<ClientEndpoint> services = getServices(resource.getInputStream());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OpenEngSBClient(start.getJmsService(), services);
            }
        });
    }

    private static List<ClientEndpoint> getServices(InputStream inputStream) {
        List<ClientEndpoint> services = new ArrayList<ClientEndpoint>();
        try {
            SAXReader saxReader = new SAXReader();
            org.dom4j.Document doc = saxReader.read(inputStream);

            @SuppressWarnings("unchecked")
            List<DefaultElement> selectNodes = (List<DefaultElement>) doc.selectNodes("//beans/jms:consumer");

            for (DefaultElement e : selectNodes) {
                String destinationName = e.attribute("destinationName").getValue();
                String targetService = e.attribute("targetService").getValue();
                services.add(new ClientEndpoint(destinationName, targetService));
            }

            return services;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
