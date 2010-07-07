package org.openengsb.persistence;

import java.util.List;

public interface PersistenceInternal {

    List<PersistenceObject> query(List<PersistenceObject> example);

    void create(List<PersistenceObject> elements);

    void update(PersistenceObject oldElement, PersistenceObject newElement);

    void delete(List<PersistenceObject> examples);

}
