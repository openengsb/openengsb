package org.openengsb.core.security;

import org.openengsb.core.common.AbstractOpenEngSBService;
import org.springframework.security.core.context.SecurityContextHolder;

public class DummyServiceImpl extends AbstractOpenEngSBService implements DummyService {

    public DummyServiceImpl() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
    }

    public DummyServiceImpl(String id) {
        super(id);
    }

    @Override
    public int getTheAnswerToLifeTheUniverseAndEverything() {
        return 42;
    }

    @Override
    public int test() {
        return -1;
    }
}
