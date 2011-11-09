package org.openengsb.core.console.internal.completer;

import com.google.common.base.Strings;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.openengsb.core.console.internal.ServiceCommands;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;

import java.util.List;

import static org.openengsb.core.console.internal.util.ServiceCommandArguments.LIST;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.CREATE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.UPDATE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.DELETE;

/**
 *
 */
public class ServiceCompleter implements Completer {

    /**
     * @param buffer     it's the beginning string typed by the user
     * @param cursor     it's the position of the cursor
     * @param candidates the list of completions proposed to the user
     */
    public int complete(String buffer, int cursor, List<String> candidates) {

        StringsCompleter delegate = new StringsCompleter();
        if (buffer != null) {
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
            }

        } else {
            delegate.getStrings().add(LIST.toString().toLowerCase());
            delegate.getStrings().add(CREATE.toString().toLowerCase());
            delegate.getStrings().add(UPDATE.toString().toLowerCase());
            delegate.getStrings().add(DELETE.toString().toLowerCase());
        }
        return delegate.complete(buffer, cursor, candidates);
    }

}
