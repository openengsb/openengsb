package org.openengsb.core.api.remote;

public abstract class FilterActionWithoutResult<InputType> implements FilterAction<InputType, InputType> {

    @Override
    public InputType apply(InputType input) throws FilterException {
        doApply(input);
        return input;
    };

    protected abstract void doApply(InputType input) throws FilterException;

}
