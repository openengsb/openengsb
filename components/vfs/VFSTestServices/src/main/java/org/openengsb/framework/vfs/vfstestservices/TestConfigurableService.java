package org.openengsb.framework.vfs.vfstestservices;

import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;

public class TestConfigurableService implements ConfigurableService {

    private List<String> propertyList;
    private boolean success;
    private String name;
    private int returnCount;
    private int returns = 0;

    public TestConfigurableService(List<String> propertyList, boolean success) {
        this(propertyList, success, "ConfigurableService");
    }

    public TestConfigurableService(List<String> propertyList, boolean success, String name) {
        this(propertyList, success, name, 1);
    }

    public TestConfigurableService(List<String> propertyList, boolean success, String name, int returnCount) {
        this.propertyList = propertyList;
        this.success = success;
        this.name = name;
        this.returnCount = returnCount;
    }

    @Override
    public boolean reconfigure() {
        returns++;

        if (returns > returnCount) {
            success = true;
        }

        System.out.print(
            "ConfigurableService Name: " + name + " has been reconfigured with return state: " + success + "\n");
        return success;
    }

    @Override
    public List<String> getPropertyList() {
        return propertyList;
    }
}
