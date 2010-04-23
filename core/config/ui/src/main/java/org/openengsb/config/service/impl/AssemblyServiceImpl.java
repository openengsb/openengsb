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
package org.openengsb.config.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.management.DeploymentServiceMBean;

import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ServiceAssembly;
import org.openengsb.config.jbi.BeanInfo;
import org.openengsb.config.jbi.EndpointInfo;
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.service.AssemblyService;
import org.openengsb.config.service.ComponentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyServiceImpl implements AssemblyService {
    private static final Logger log = LoggerFactory.getLogger(AssemblyServiceImpl.class);
    private String deployPath;
    private DeploymentServiceMBean deploymentService;
    private ComponentService componentService;

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
        new File(deployPath).mkdirs();
    }

    public void setDeploymentService(DeploymentServiceMBean deploymentService) {
        this.deploymentService = deploymentService;
    }

    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Override
    public void deploy(ServiceAssembly sa) throws IOException {
        ServiceAssemblyInfo sai = new ServiceAssemblyInfo(sa.getName(), buildEndpointInfos(sa), buildBeanInfos(sa));
        File tmp = File.createTempFile(sa.getName(), ".zip");
        FileOutputStream fos = new FileOutputStream(tmp);
        sai.toZip(fos);
        File to = new File(deployPath, sa.getName() + ".zip");
        moveFile(tmp, to);
        try {
            JbiTaskResult result = new JbiTaskResult();
            log.info("deploying service assembly {}", sa.getName());
            result.setAndCheckResult(deploymentService.deploy(to.toURI().toURL().toString()));
            result.setAndCheckResult(deploymentService.start(sa.getName()));
        } catch (Exception e) {
            log.error("error deploying service assembly", e);
            throw new IOException(e);
        }
    }

    private void moveFile(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            if (!copyFile(from, to)) {
                throw new IOException();
            }
        }
    }

    private List<BeanInfo> buildBeanInfos(ServiceAssembly sa) {
        List<BeanInfo> beans = new ArrayList<BeanInfo>();
        for (PersistedObject po : sa.getBeans()) {
            ComponentType ctype = componentService.getComponent(po.getComponentType());
            beans.add(new BeanInfo(ctype.getBean(po.getDeclaredType()), po.getDetachedValues()));
        }
        return beans;
    }

    private List<EndpointInfo> buildEndpointInfos(ServiceAssembly sa) {
        List<EndpointInfo> endpoints = new ArrayList<EndpointInfo>();
        for (PersistedObject po : sa.getEndpoints()) {
            ComponentType ctype = componentService.getComponent(po.getComponentType());
            endpoints.add(new EndpointInfo(ctype.getEndpoint(po.getDeclaredType()), po.getDetachedValues()));
        }
        return endpoints;
    }

    private boolean copyFile(File from, File to) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            byte[] buffer = new byte[2048];
            while (true) {
                int len = in.read(buffer);
                if (len != -1) {
                    out.write(buffer, 0, len);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("deploying service assembly failed", e);
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return to.exists();
    }

    @Override
    public boolean isDeployed(ServiceAssembly sa) {
        try {
            String[] assemblies = deploymentService.getDeployedServiceAssemblies();
            for (int i = 0; i < assemblies.length; ++i) {
                if (sa.getName().equals(assemblies[i])) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return false;
        }
        return false;
    }

    @Override
    public void undeploy(ServiceAssembly sa) throws IOException {
        log.info("undeploying service assembly {}", sa.getName());
        try {
            JbiTaskResult result = new JbiTaskResult();
            result.setAndCheckResult(deploymentService.stop(sa.getName()));
            result.setAndCheckResult(deploymentService.shutDown(sa.getName()));
            String undeploy = deploymentService.undeploy(sa.getName());
            // TODO Fixes a bug in ServiceMix: result must be non-null
            if (undeploy != null) {
                result.setAndCheckResult(undeploy);
            }
        } catch (Exception e) {
            log.error("undeploying of service assembly failed", e);
            throw new IOException(e);
        }
    }
}
