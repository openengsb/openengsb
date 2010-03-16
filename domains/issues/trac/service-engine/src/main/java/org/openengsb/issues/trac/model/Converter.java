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

package org.openengsb.issues.trac.model;

import java.util.Hashtable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.openengsb.drools.model.Issue;
import org.openengsb.drools.model.Issue.IssuePriority;
import org.openengsb.drools.model.Issue.IssueType;

public class Converter {

    private Logger log = Logger.getLogger(Converter.class);

    private Hashtable<IssuePriority, String> priorityMapping;
    private Hashtable<IssueType, String> typeMapping;

    public Converter() {
        priorityMapping = new Hashtable<IssuePriority, String>();
        typeMapping = new Hashtable<IssueType, String>();

        priorityMapping.put(IssuePriority.HIGH, "major");
        priorityMapping.put(IssuePriority.IMMEDIATE, "blocker");
        priorityMapping.put(IssuePriority.LOW, "trivial");
        priorityMapping.put(IssuePriority.NONE, "none");
        priorityMapping.put(IssuePriority.NORMAL, "minor");
        priorityMapping.put(IssuePriority.URGENT, "critical");

        typeMapping.put(IssueType.BUG, "defect");
        typeMapping.put(IssueType.IMPROVEMENT, "enhancement");
        typeMapping.put(IssueType.NEW_FEATURE, "feature");
        typeMapping.put(IssueType.TASK, "task");
    }

    public TracIssue convertGenericIssueToSpecificIssue(Issue generic) {
        TracIssue specific = new TracIssue();

        if (generic.getId() != null) {
            try {
                specific.setId(Integer.parseInt(generic.getId()));
            } catch (NumberFormatException e) {
                log.fatal(String.format("Could not parse generic issue ID %s. The id must be an integer.", generic
                        .getId()), e);
            }
        }
        specific.setSummary(generic.getSummary());
        specific.setDescription(generic.getDescription());
        specific.setOwner(generic.getOwner());
        specific.setReporter(generic.getReporter());
//        specific.setPriority(priorityMapping.get(generic.getPriority()));
//        specific.setType(typeMapping.get(generic.getType()));
        specific.setVersion(generic.getAffectedVersion());

        return specific;
    }

    public Issue convertSpecificToGeneric(TracIssue specific) {
        throw new NotImplementedException();
    }
}
