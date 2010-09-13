package org.openengsb.ui.web.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserService implements UserDetailsService {

    private Map<String, UserDetails> data = new HashMap<String, UserDetails>();
    {
        Collection<GrantedAuthority> auth = new ArrayList<GrantedAuthority>();
        auth.add(new GrantedAuthorityImpl("ROLE_USER"));
        User u1 = new User("test", "password", true, true, true, true, auth);
        data.put("test", u1);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        return data.get(username);
    }

}