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

package org.openengsb.core.workflow.editor.converter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.editor.Action;
import org.openengsb.core.workflow.editor.Event;
import org.openengsb.core.workflow.editor.Workflow;

@XmlRootElement
public class Process {

    private int idCounter = 1;
    private List<Integer> endConnections = new ArrayList<Integer>();
    private String type;
    private String name;
    private String id;

    private String packageName;
    List<Object> nodes = new ArrayList<Object>();

    List<Connection> connection = new ArrayList<Connection>();

    public Process() {
    }

    private Process(String type, String name, String id, String packageName) {
        super();
        this.type = type;
        this.name = name;
        this.id = id;
        this.packageName = packageName;
    }

    private int getIdCounter() {
        idCounter++;
        return idCounter;
    }

    private void addNode(Object node) {
        this.nodes.add(node);
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public String getId() {
        return id;
    }

    @XmlAttribute(name = "package-name")
    public String getPackageName() {
        return packageName;
    }

    @XmlElementWrapper
    @XmlElements({ @XmlElement(name = "start", type = Start.class),
        @XmlElement(name = "actionNode", type = ActionNode.class),
        @XmlElement(name = "eventNode", type = ActionEvent.class),
        @XmlElement(name = "end", type = End.class) })
    public List<Object> getNodes() {
        return nodes;
    }

    @XmlElementWrapper(name = "connections")
    public List<Connection> getConnection() {
        return connection;
    }

    public static final class Start {

        private Start() {

        }

        @XmlAttribute
        private int id = 1;
        @XmlAttribute
        private String name = "Start";
    }

    public static final class End {

        private End() {
        }

        @XmlAttribute
        private int id;
        @XmlAttribute
        private String name = "End";
    }

    public static Process build(Workflow workflow) {
        Process process =
            new Process("RuleFlow", workflow.getName(), workflow.getName(), RuleBaseElementId.DEFAULT_RULE_PACKAGE);
        process.addNode(new Start());
        processAction(process, workflow.getRoot(), 1);
        addEndConnections(process);
        return process;
    }

    private static void addEndConnections(Process process) {
        End end = new End();
        end.id = process.getIdCounter();
        process.addNode(end);
        for (Integer toEnd : process.endConnections) {
            process.connection.add(new Connection(toEnd, end.id));
        }
    }

    private static void processAction(Process process, Action root, int parentId) {
        int counter = process.getIdCounter();
        process.addNode(new ActionNode(counter, root.getMethodName(), root.getLocation(), root
            .getMethodName()));
        process.connection.add(new Connection(parentId, counter));
        for (Action action : root.getActions()) {
            processAction(process, action, counter);
        }
        for (Event event : root.getEvents()) {
            processEvent(process, event, counter);
        }
        if (root.getActions().size() == 0) {
            process.endConnections.add(counter);
        }
    }

    private static void processEvent(Process process, Event event, int parentId) {
        String simpleName = event.getEvent().getSimpleName();
        int counter = process.getIdCounter();
        process.addNode(new ActionEvent(counter, simpleName, simpleName));
        process.connection.add(new Connection(parentId, counter));
        for (Action action : event.getActions()) {
            processAction(process, action, counter);
        }
        if (event.getActions().size() == 0) {
            process.endConnections.add(counter);
        }
    }
}
