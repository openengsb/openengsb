package org.openengsb.core.common.validation;

import org.openengsb.core.common.l10n.BundleStrings;
import org.openengsb.core.common.l10n.LocalizableString;
import org.osgi.framework.BundleContext;

public class AbstractValidator {

    private final BundleStrings bundleStrings;

    public AbstractValidator(BundleContext context) {
        this.bundleStrings = new BundleStrings(context.getBundle());
    }

    protected LocalizableString getString(String key, String... parameters) {
        return bundleStrings.getString(key, parameters);
    }

}
