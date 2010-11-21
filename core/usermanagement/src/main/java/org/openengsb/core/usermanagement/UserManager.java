package org.openengsb.core.usermanagement;

import org.openengsb.core.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.usermanagement.exceptions.UserNotFoundException;
import org.openengsb.core.usermanagement.model.User;

public interface UserManager {

    public void createUser(User user) throws UserExistsException;

    public void updateUser(User oldUser, User newUser) throws UserNotFoundException;

    public void deleteUser(String username) throws UserNotFoundException;

    public User loadUserByUsername(String username) throws UserNotFoundException;

}
