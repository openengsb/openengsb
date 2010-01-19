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

import org.openengsb.drools.model.LogEntry;
import org.openengsb.drools.model.MergeResult;

public interface ScmDomain extends Domain {

    public void add(String fileToAdd);

    public MergeResult update(String updatePath);

    public void switchBranch(String branchName);

    public MergeResult merge(String branchName);

    public LogEntry[] log(String[] files, String startRevision, String endRevision);

    public String[] listBranches();

    public MergeResult doImport(String sourcePath, String destinationPath, String commitMessage, String author);

    public void export(String destinationPath);

    public String diff(String oldFile, String newFile, String oldRevision, String newRevision);

    public void delete(String file);

    public MergeResult commit(String author, String commitMessage, String subPath);

    public MergeResult checkout(String author);

    public void branch(String branchName, String commitMessage);

    public String blame(String file, String revision);

}
