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
package org.openengsb.config.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jbi.management.DeploymentServiceMBean;

public class DeploymentServiceMBeanMock implements DeploymentServiceMBean {

    private final List<String> deployedAssemblies = new ArrayList<String>();
    private final Random rand = new Random();

    @Override
    public boolean canDeployToComponent(String componentName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String deploy(String saZipURL) throws Exception {
        if (rand.nextInt(5) == 0) {
            return buildFailedResult("deploy");
        }
        File file = new File(saZipURL);
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        deployedAssemblies.add(name);
        return buildSuccessResult("deploy");
    }

    @Override
    public String[] getComponentsForDeployedServiceAssembly(String saName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getDeployedServiceAssemblies() throws Exception {
        return deployedAssemblies.toArray(new String[deployedAssemblies.size()]);
    }

    @Override
    public String[] getDeployedServiceAssembliesForComponent(String componentName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getDeployedServiceUnitList(String componentName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServiceAssemblyDescriptor(String saName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getState(String serviceAssemblyName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDeployedServiceUnit(String componentName, String suName) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String shutDown(String serviceAssemblyName) throws Exception {
        return buildSuccessResult("shutDown");
    }

    @Override
    public String start(String serviceAssemblyName) throws Exception {
        if (deployedAssemblies.contains(serviceAssemblyName)) {
            if (rand.nextInt(5) == 0) {
                return buildFailedResult("start");
            } else {
                return buildSuccessResult("start");
            }
        } else {
            return buildFailedResult("start");
        }

    }

    @Override
    public String stop(String serviceAssemblyName) throws Exception {
        return buildSuccessResult("stop");
    }

    @Override
    public String undeploy(String saName) throws Exception {
        if (deployedAssemblies.contains(saName)) {
            if (rand.nextInt(5) == 0) {
                return buildFailedResult("undeploy");
            }
            deployedAssemblies.remove(saName);
            return buildSuccessResult("undeploy");
        } else {
            return buildFailedResult("undeploy");
        }
    }

    private String buildSuccessResult(String taskId) {
        return buildResult(taskId, "SUCCESS");
    }

    private String buildFailedResult(String taskId) {
        return buildResult(taskId, "FAILED");
    }

    private String buildResult(String taskId, String result) {
        StringBuilder b = new StringBuilder();
        b.append("<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\" version=\"1.0\">\n");
        b.append("<jbi-task-result>\n");
        b.append("<frmwk-task-result>\n");
        b.append("<frmwk-task-result-details>\n");
        b.append("<task-result-details>\n");
        b.append("<task-id>" + taskId + "</task-id>\n");
        b.append("<task-result>" + result + "</task-result>\n");
        b.append("</task-result-details>\n");
        b.append("</frmwk-task-result-details>\n");
        b.append("</frmwk-task-result>\n");
        b.append("</jbi-task-result>\n");
        b.append("</jbi-task>\n");
        return b.toString();
    }
}
