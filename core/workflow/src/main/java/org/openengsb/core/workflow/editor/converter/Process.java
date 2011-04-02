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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.editor.Action;
import org.openengsb.core.workflow.editor.End;
import org.openengsb.core.workflow.editor.Event;
import org.openengsb.core.workflow.editor.Workflow;

@XmlRootElement
public class Process {

    private int idCounter = 1;
    private Map<End, List<Integer>> endConnections = new LinkedHashMap<End, List<Integer>>();
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
        @XmlElement(name = "end", type = EndNode.class),
        @XmlElement(name = "split", type = Split.class),
        @XmlElement(name = "join", type = Join.class) })
    public List<Object> getNodes() {
        return nodes;
    }

    @XmlElementWrapper(name = "connections")
    @XmlElement(name = "connection")
    public List<Connection> getNodesConnection() {
        return connection;
    }

    @XmlAttribute
    public String getXmlns() {
        return "http://drools.org/drools-5.0/process";
    }

    public static final class Start {

        private Start() {

        }

        @XmlAttribute
        private int id = 1;
        @XmlAttribute
        private String name = "Start";
    }

    public static final class EndNode {

        private EndNode() {
        }

        @XmlAttribute
        private int id;
        @XmlAttribute
        private String name = "End";
    }

    public static final class Split {

        private Split() {
        }

        @XmlAttribute
        private int id;
        @XmlAttribute
        private String name;
        @XmlAttribute
        private int type = 1;
    }

    public static final class Join {

        private Join() {
        }

        @XmlAttribute
        private int id;
        @XmlAttribute
        private String name;
        @XmlAttribute
        private int type = 1;
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
        for (End end : process.endConnections.keySet()) {
            List<Integer> list = process.endConnections.get(end);
            int counter = process.getIdCounter();
            for (int id : list) {
                process.connection.add(new Connection(id, counter));
            }
            if (list.size() > 1) {
                Join node = new Join();
                node.name = "EndJoin";
                node.id = counter;
                counter = process.getIdCounter();
                process.addNode(node);
                process.connection.add(new Connection(node.id, counter));
            }
            EndNode endNode = new EndNode();
            endNode.id = counter;
            process.addNode(endNode);
        }
    }

    private static void addEnd(Process process, End end, int from) {
        if (!process.endConnections.containsKey(end)) {
            process.endConnections.put(end, new ArrayList<Integer>());
        }
        process.endConnections.get(end).add(from);
    }

    private static void processAction(Process process, Action root, int parentId) {
        int counter = process.getIdCounter();
        process.addNode(new ActionNode(counter, root.getMethodName(), root.getLocation(), root
            .getMethodName()));
        process.connection.add(new Connection(parentId, counter));
        if (root.isLeaf()) {
            End end;
            if (root.hasSharedEnd()) {
                end = root.getEnd();
            } else {
                end = new End("Default");
            }
            addEnd(process, end, counter);
        }
        int children = root.getActions().size() + root.getEvents().size();
        String splitName = "ActionSplit";
        counter = addSplit(process, counter, children, splitName);
        handleActions(process, root, counter);
        handleEvents(process, root, counter);
    }

    private static int addSplit(Process process, int counter, int children, String splitName) {
        if (children > 1) {
            Split split = new Split();
            split.id = process.getIdCounter();
            split.name = splitName;
            process.addNode(split);
            process.connection.add(new Connection(counter, split.id));
            counter = split.id;
        }
        return counter;
    }

    private static void handleEvents(Process process, Action root, int counter) {
        for (Event event : root.getEvents()) {
            processEvent(process, event, counter);
        }
    }

    private static void handleActions(Process process, Action root, int counter) {
        iterateOverActions(process, root, counter);
    }

    private static void iterateOverActions(Process process, Action root, int id) {
        for (Action action : root.getActions()) {
            processAction(process, action, id);
        }
    }

    private static void processEvent(Process process, Event event, int parentId) {
        String simpleName = event.getEvent().getSimpleName();
        int eventId = process.getIdCounter();
        process.addNode(new ActionEvent(eventId, simpleName, simpleName));
        Join join = new Join();
        join.id = process.getIdCounter();
        join.name = "EventJoin";
        process.addNode(join);
        process.connection.add(new Connection(parentId, join.id));
        process.connection.add(new Connection(eventId, join.id));
        int counter = join.id;
        counter = addSplit(process, counter, event.getActions().size() + event.getEvents().size(), "EventSplit");
        for (Action action : event.getActions()) {
            processAction(process, action, counter);
        }
        for (Event toProcess : event.getEvents()) {
            processEvent(process, toProcess, counter);
        }
    }
}
