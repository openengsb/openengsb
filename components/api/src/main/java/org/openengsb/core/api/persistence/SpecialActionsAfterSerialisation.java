package org.openengsb.core.api.persistence;

/**
 * Sometimes it's required to execute special actions after serialisation (e.g. in case of using transient). In that
 * case that interface allows to reset such objects since the metohd is called directly after the object is regenerated.
 */
public interface SpecialActionsAfterSerialisation {

    /**
     * Execute anything required here.
     */
    void doSpecialActions();

}
