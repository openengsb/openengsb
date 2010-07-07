package org.openengsb.persistence;

import java.util.List;

public interface Persistence {

    List<Object> query(List<Object> example);

    void create(List<Object> elements);

    void update(Object oldElement, Object newElement);

    void delete(List<Object> examples);

}
