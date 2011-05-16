package org.openengsb.core.common.security;

import java.security.PrivateKey;

public class SingletonPrivateKeySource implements PrivateKeySource {

    private PrivateKey key;

    public SingletonPrivateKeySource(PrivateKey key) {
        this.key = key;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return key;
    }
}
