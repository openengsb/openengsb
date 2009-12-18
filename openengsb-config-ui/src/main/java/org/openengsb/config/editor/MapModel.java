package org.openengsb.config.editor;

import java.util.Map;

import org.apache.wicket.model.IModel;

public class MapModel<K,V> implements IModel<V> {
    private static final long serialVersionUID = 1L;
    private final Map<K, V> map;
    private final K key;

    public MapModel(Map<K,V> map, K key) {
        this.map = map;
        this.key = key;

    }

    @Override
    public V getObject() {
        return map.get(key);
    }

    @Override
    public void setObject(V object) {
        map.put(key, object);
    }

    @Override
    public void detach() {
        // noop
    }
}
