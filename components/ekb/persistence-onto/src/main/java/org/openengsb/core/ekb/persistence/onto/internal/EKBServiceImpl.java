package org.openengsb.core.ekb.persistence.onto.internal;

import java.util.List;
import java.util.UUID;

import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBService;
import org.openengsb.core.ekb.api.Query;
import org.openengsb.core.ekb.api.TransformationDescriptor;

/**
 * Hello world!
 * 
 */
public class EKBServiceImpl implements EKBService {

    @Override
    public void commit(EKBCommit ekbCommit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit(EKBCommit ekbCommit, UUID headRevision) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addTransformation(TransformationDescriptor descriptor) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> List<T> query(Query query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object nativeQuery(Object query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID getLastRevisionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteCommit(UUID headRevision) {
        // TODO Auto-generated method stub

    }

    @Override
    public EKBCommit loadCommit(UUID revision) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getModel(Class<T> model, String oid) {
        // TODO Auto-generated method stub
        return null;
    }
}
