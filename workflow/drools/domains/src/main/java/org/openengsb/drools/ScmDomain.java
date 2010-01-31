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

import org.openengsb.drools.model.MergeResult;
import org.openengsb.drools.model.ScmLogEntry;

public interface ScmDomain extends Domain {

    void add(String fileToAdd);

    MergeResult update(String updatePath);

    void switchBranch(String branchName);

    MergeResult merge(String branchName);

    ScmLogEntry[] log(String[] files, String startRevision, String endRevision);

    String[] listBranches();

    MergeResult doImport(String sourcePath, String destinationPath, String commitMessage, String author);

    void export(String destinationPath);

    String diff(String oldFile, String newFile, String oldRevision, String newRevision);

    void delete(String file);

    MergeResult commit(String author, String commitMessage, String subPath);

    MergeResult checkout(String author);

    void branch(String branchName, String commitMessage);

    String blame(String file, String revision);

}
