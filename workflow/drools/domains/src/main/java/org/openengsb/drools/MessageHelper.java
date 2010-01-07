/**

Copyright 2010 OpenEngSB Division, Vienna University of Technology

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE\-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

package org.openengsb.drools;

import java.io.Serializable;

/**
 * Helper that is responsible for executing tasks specified as consequences in
 * rules.
 * 
 */
public interface MessageHelper extends Domain, Serializable {
    /**
     * Sends an email to the specified address, with the specified content.
     * 
     * @param email email-address to send the notification to.
     * @param subject subject-line of the email
     * @param body body of the email
     * @return true if the execution was successful
     */
    boolean sendNotification(String email, String subject, String body);

    /**
     * Creates an issue in the issue-tracker.
     * 
     * @param subject one-line description of the issue
     * @param body long description of the issue
     * @return true if the execution was successful
     */
    boolean createIssue(String subject, String body);

    // boolean call(String name, Collection<Object> args);

}
