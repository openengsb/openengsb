package org.openengsb.domains.notification.email.internal;

import java.util.Map;

public interface EmailNotifierBuilder {

    EmailNotifier createEmailNotifier(String string, Map<String, String> attributes);

    void updateEmailNotifier(EmailNotifier notifier, Map<String, String> attributes);

}
