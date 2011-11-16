package org.openengsb.core.console.internal.completer;

import static org.openengsb.core.console.internal.util.ServiceCommandArguments.CREATE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.DELETE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.LIST;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.UPDATE;

import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;
import org.openengsb.core.console.internal.util.ServicesHelper;

/**
 *
 */
public class ServiceCompleter implements Completer {

    private ServicesHelper servicesHelper;

    public ServiceCompleter(ServicesHelper helper) {
        this.servicesHelper = helper;
    }

    /**
     * @param buffer     it's the beginning string typed by the user
     * @param cursor     it's the position of the cursor
     * @param candidates the list of completions proposed to the user
     */
    public int complete(String buffer, int cursor, List<String> candidates) {

        StringsCompleter delegate = new StringsCompleter();
        if (buffer == null) {
            delegate = printStandardCommands(delegate);
        } else {
            try {
                ServiceCommandArguments arguments = ServiceCommandArguments.valueOf(buffer.toUpperCase());
                switch (arguments) {
                    case LIST:
                        return delegate.complete(buffer, cursor, candidates);
                    case CREATE:
                        // TODO
                        break;
                    case UPDATE:
                        // TODO
                        break;
                    case DELETE:
                        break;
                    default:
                        break;
                }
            } catch (IllegalArgumentException ex) {
                delegate = printStandardCommands(delegate);
            }
        }
        return delegate.complete(buffer, cursor, candidates);
    }

    private StringsCompleter printStandardCommands(StringsCompleter delegate) {
        delegate.getStrings().add(LIST.toString().toLowerCase());
        delegate.getStrings().add(CREATE.toString().toLowerCase());
        delegate.getStrings().add(UPDATE.toString().toLowerCase());
        delegate.getStrings().add(DELETE.toString().toLowerCase());
        return delegate;
    }

}
