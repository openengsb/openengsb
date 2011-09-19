package org.openengsb.core.security.model;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class TestBean {

    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<BeanData> members;

    public TestBean(String name) {

        this.name = name;
    }

    public TestBean() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<BeanData> getMembers() {
        return members;
    }

    public void setMembers(Collection<BeanData> members) {
        this.members = members;
    }

}
