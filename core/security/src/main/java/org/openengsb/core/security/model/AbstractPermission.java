/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.security.model;

import java.lang.reflect.Method;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.model.Permission;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractPermission implements Permission {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = true)
    private String context;

    protected AbstractPermission() {
    }

    protected AbstractPermission(String context) {
        this.context = context;
    }

    @Override
    public final boolean permits(Object service, Method operation, Object[] args) {
        if (context == null) {
            return internalPermits(service, operation, args);
        }
        String currentContext = ContextHolder.get().getCurrentContextId();
        if (ObjectUtils.notEqual(context, currentContext)) {
            return false;
        }
        return internalPermits(service, operation, args);
    }

    protected abstract boolean internalPermits(Object service, Method operation, Object[] args);

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractPermission other = (AbstractPermission) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

}
