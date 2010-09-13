package org.openengsb.core.common.validation;

import java.util.Collections;
import java.util.Map;

public class FormValidationResultImpl implements FormValidationResult {

    private final boolean valid;
    private final Map<String, String> attributeErrorMessages;

    public FormValidationResultImpl(boolean valid, Map<String, String> attributeErrorMessages) {
        super();
        this.valid = valid;
        this.attributeErrorMessages = attributeErrorMessages;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public Map<String, String> getAttributeErrorMessages() {
        return Collections.unmodifiableMap(this.attributeErrorMessages);
    }

}
