package org.openengsb.framework.vfs.vfstestservices;

import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;

public class TestConfigurableService implements ConfigurableService {

    private List<String> propertyList;
    private boolean success;

    public TestConfigurableService(List<String> propertyList, boolean success) {
        this.propertyList = propertyList;
        this.success = success;
    }

    @Override
    public boolean reconfigure() {
        return success;
    }

    @Override
    public List<String> getPropertyList() {
        return propertyList;
    }
}
