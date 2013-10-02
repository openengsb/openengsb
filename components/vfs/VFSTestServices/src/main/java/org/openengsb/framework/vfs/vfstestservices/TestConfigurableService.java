package org.openengsb.framework.vfs.vfstestservices;

import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;

public class TestConfigurableService implements ConfigurableService {

    private List<String> propertyList;
    private boolean success;
    private String name = "ConfService";

    public TestConfigurableService(List<String> propertyList, boolean success) {
        this.propertyList = propertyList;
        this.success = success;
    }

    public TestConfigurableService(List<String> propertyList, boolean success, String name) {
        this.propertyList = propertyList;
        this.success = success;
        this.name = name;
    }

    @Override
    public boolean reconfigure() {
        System.out.print(
                "ConfigurableService Name: " + name + " has been reconfigured with return state: " + success + "\n");
        return success;
    }

    @Override
    public List<String> getPropertyList() {
        return propertyList;
    }
}
