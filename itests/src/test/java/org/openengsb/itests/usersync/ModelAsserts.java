package org.openengsb.itests.usersync;

import static org.junit.Assert.assertEquals;

import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.User;

public final class ModelAsserts {

    public static void assertEqualUser(User user1, User user2) {
        assertEquals(user1.getUsername(), user2.getUsername());
        assertEquals(user1.getAttributes().size(), user2.getAttributes().size());

        for (int i = 0; i < user1.getAttributes().size(); i++) {
            Attribute attr1 = user1.getAttributes().get(i);
            Attribute attr2 = user2.getAttributes().get(i);

            assertEquals(attr1.getAttributeName(), attr2.getAttributeName());
            assertEquals(attr1.getUuid(), attr2.getUuid());
            assertEquals(attr1.getValues().size(), attr2.getValues().size());

            for (int n = 0; n < attr1.getValues().size(); n++) {
                assertEquals(attr1.getValues().get(n), attr2.getValues().get(n));
            }
        }

        assertEquals(user1.getCredentials().size(), user2.getCredentials().size());

        for (int i = 0; i < user1.getCredentials().size(); i++) {
            Credential cred1 = user1.getCredentials().get(i);
            Credential cred2 = user2.getCredentials().get(i);

            assertEquals(cred1.getUuid(), cred2.getUuid());
            assertEquals(cred1.getType(), cred2.getType());
            assertEquals(cred1.getValue(), cred2.getValue());

        }
    }

}
