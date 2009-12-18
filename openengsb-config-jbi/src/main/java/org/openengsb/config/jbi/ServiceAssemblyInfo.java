package org.openengsb.config.jbi;

import java.util.List;

public class ServiceAssemblyInfo {
    private final String name;
    private final List<ServiceUnitInfo> serviceUnits;

    public ServiceAssemblyInfo(String name, List<ServiceUnitInfo> serviceUnits) {
        this.name = name;
        this.serviceUnits = serviceUnits;
    }

    public String getName() {
        return name;
    }

    public List<ServiceUnitInfo> getServiceUnits() {
        return serviceUnits;
    }
}
