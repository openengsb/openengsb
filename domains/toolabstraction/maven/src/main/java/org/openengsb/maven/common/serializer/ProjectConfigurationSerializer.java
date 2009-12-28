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

package org.openengsb.maven.common.serializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xpath.CachedXPathAPI;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * The ProjectConfigurationSerializer creates a dom source of the
 * <tt>ProjectConfiguration</tt> object and reverse. </br> There are constants
 * used to find the elements in the dom source
 * 
 */
public class ProjectConfigurationSerializer extends AbstractSerializer {

    private static final String PROJECTCONFIGURATION = "projectConfiguration";

    private static final String GOAL_LIST = "goalList";
    private static final String GOAL = "goal";
    private static final String BASEDIRECTORY = "baseDirectory";

    private static final CachedXPathAPI xpath = new CachedXPathAPI();

    /**
     * Creates a dom source from the <tt>ProjectConfiguration</tt>
     * 
     * @param source - source of the out message
     * @param projectConfiguration - configuration of the project
     * @return dom source - the generated dom source
     */
    public static Source serialize(Source source, ProjectConfiguration projectConfiguration) {

        Element projectConfigurationElement = null;
        try {
            projectConfigurationElement = getDocument().createElement(
                    ProjectConfigurationSerializer.PROJECTCONFIGURATION);
            projectConfigurationElement.setAttribute(ProjectConfigurationSerializer.BASEDIRECTORY, String
                    .valueOf(projectConfiguration.getBaseDirectory().getAbsolutePath()));

            if (projectConfiguration.getGoals() != null) {
                Element goalList = getDocument().createElement(ProjectConfigurationSerializer.GOAL_LIST);

                for (String s : projectConfiguration.getGoals()) {
                    Element goal = getDocument().createElement(ProjectConfigurationSerializer.GOAL);
                    goal.setTextContent(s);
                    goalList.appendChild(goal);
                }

                projectConfigurationElement.appendChild(goalList);
            }

            return new DOMSource(projectConfigurationElement);

        } catch (DOMException e) {

        } catch (ParserConfigurationException e) {

        }

        return null;
    }

    /**
     * Creates a <tt>ProjectConfiguration-Object</tt> from the message
     * 
     * @param message - a <tt>NormalizedMessage</tt>, that would be transformed
     *        into a dom source
     * @return projectConfiguration - result of the tests
     */
    public static ProjectConfiguration deserialize(NormalizedMessage message) {
        ProjectConfiguration projectConfiguration = null;

        Node projectConfigNode;

        try {
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(message);

            // xpath funktioniert hier irgendwie nicht, scheinbar nur bei
            // untergeordnete
            projectConfigNode = messageXml.getNode();

            if (projectConfigNode != null) {

                projectConfiguration = new ProjectConfiguration();

                projectConfiguration.setBaseDirectory(new File(ProjectConfigurationSerializer.xpath.selectSingleNode(
                        projectConfigNode, "@" + ProjectConfigurationSerializer.BASEDIRECTORY).getNodeValue()));

                Node goalList = ProjectConfigurationSerializer.xpath.selectSingleNode(projectConfigNode,
                        ProjectConfigurationSerializer.GOAL_LIST);

                List<String> goals = new ArrayList<String>();
                NodeList list = goalList.getChildNodes();

                for (int i = 0; i < list.getLength(); i++) {
                    Node n = list.item(i);
                    goals.add(n.getTextContent());
                }

            }

        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return projectConfiguration;
    }

    /**
     * Method only for testing the creation of the ProjectConfiguration from a
     * source
     * 
     * @param source - source that includes the projectConfiguration pattern
     * @return projectConfiguration - the created result
     */
    public static ProjectConfiguration deserializeSource(Source source) {
        ProjectConfiguration projectConfiguration = null;

        Node projectConfigNode;

        try {
            projectConfiguration = new ProjectConfiguration();

            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = sourceTransformer.toDOMSource(source);

            projectConfigNode = ProjectConfigurationSerializer.xpath.selectSingleNode(messageXml.getNode(),
                    ProjectConfigurationSerializer.PROJECTCONFIGURATION);

            projectConfigNode = messageXml.getNode();

            if (projectConfigNode != null) {

                projectConfiguration = new ProjectConfiguration();

                projectConfiguration.setBaseDirectory(new File(ProjectConfigurationSerializer.xpath.selectSingleNode(
                        projectConfigNode, "@" + ProjectConfigurationSerializer.BASEDIRECTORY).getNodeValue()));

                Node goalList = ProjectConfigurationSerializer.xpath.selectSingleNode(projectConfigNode,
                        ProjectConfigurationSerializer.GOAL_LIST);

                List<String> goals = new ArrayList<String>();
                NodeList list = goalList.getChildNodes();

                for (int i = 0; i < list.getLength(); i++) {
                    Node n = list.item(i);
                    goals.add(n.getTextContent());
                }

                projectConfiguration.setGoals(goals);

            }

        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return projectConfiguration;

    }
}
