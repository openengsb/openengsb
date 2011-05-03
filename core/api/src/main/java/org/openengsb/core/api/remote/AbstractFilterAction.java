package org.openengsb.core.api.remote;

public abstract class AbstractFilterAction<InputType, OutputType> implements FilterAction<InputType, OutputType> {

    private Class<InputType> inputType;
    private Class<OutputType> outputType;

    protected AbstractFilterAction(Class<InputType> inputType, Class<OutputType> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @Override
    public Class<InputType> getSupportedInputType() {
        return inputType;
    }

    @Override
    public Class<OutputType> getSupportedOutputType() {
        return outputType;
    }

}
