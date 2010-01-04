package org.openengsb.swingclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

        final List<String> services = getServices(resource.getInputStream());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OpenEngSBClient(start.getJmsService(), services);
            }
        });
    }

    private static List<String> getServices(InputStream inputStream) {
        List<String> services = new ArrayList<String>();
        try {
            SAXReader saxReader = new SAXReader();
            org.dom4j.Document doc = saxReader.read(inputStream);

            List<Attribute> selectNodes = doc.selectNodes("//beans/jms:consumer/@destinationName");

            for (Attribute a : selectNodes) {
                services.add(a.getValue());
            }
            
            return services;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
