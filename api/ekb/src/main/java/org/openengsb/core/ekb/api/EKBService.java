package org.openengsb.core.ekb.api;

import java.util.List;
import java.util.UUID;

/**
 * EKBService interface agreed by CDL developers per 21.05.2014. Designed with
 * context-aware (thread-local) nature of EKB. This interface does not consider
 * model evolution, but should fully support instance versioning.
 * 
 * Added <T> T getModel(Class<T> model, String oid) per 16.06.2014 to enable
 * data access with oid instead of class
 * 
 */
public interface EKBService {

    /**
     * Persist EKBcommit in the EKBService implementation.
     * 
     * @param ekbCommit
     */
    void commit(EKBCommit ekbCommit);

    /**
     * Persist EKBcommit in the EKBService implementation. Additional check
     * whether parentRevision is the last revision.
     * 
     * @param ekbCommit
     * @param headRevision
     */
    void commit(EKBCommit ekbCommit, UUID headRevision);

    /**
     * Definition of transformation between related object. Provided a
     * transformation object/filename, it will define the transformation between
     * models as needed.
     * 
     * @param descriptor
     */
    void addTransformation(TransformationDescriptor descriptor);

    /**
     * EKBService querying method.
     * 
     * @param query the definition of Query, consist of Filter, Result types
     *        (and maybe aggregate function)
     * @return query result as list of object.
     */
    <T> List<T> query(Query query);

    /**
     * Query the EKB back-end using native implementation-specific (e.g., SPARQL
     * for ontologies or SQL for relational database).
     * 
     * @param query in native query languages.
     * @return native query result object.
     */
    Object nativeQuery(Object query);

    /**
     * Get last revision ID from current context. Will be used for additional
     * check within commit and delete function.
     * 
     * @return UUID
     */
    UUID getLastRevisionId();

    /**
     * Delete the head revision of commit. Additional check whether headRevision
     * is the last revision.
     * 
     * @param headRevision
     */
    void deleteCommit(UUID headRevision);

    /**
     * Load commit for particular revision.
     * 
     * @param revision
     * @return EKB commit
     */
    EKBCommit loadCommit(UUID revision);

    /**
     * Loads instance with the given oid. Do model checking, if the model is not
     * the class of the instance, return null.
     * 
     * @param model the expected instance class.
     * @param oid the universal id of the instance.
     * @return instance of T.
     */
    <T> T getModel(Class<T> model, String oid);

}
