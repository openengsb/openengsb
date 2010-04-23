/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.service.impl;

import java.util.Map;

import org.openengsb.config.dao.PersistedObjectDao;
import org.openengsb.config.domain.Attribute;
import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ReferenceAttribute;
import org.openengsb.config.domain.ValueAttribute;
import org.openengsb.config.service.EditorService;
import org.springframework.beans.factory.annotation.Autowired;

public class EditorServiceImpl implements EditorService {

    @Autowired
    private PersistedObjectDao dao;

    @Override
    public void updatePersistedObject(PersistedObject po, Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            Attribute a = po.getAttributes().get(entry.getKey());
            if (a instanceof ValueAttribute) {
                ValueAttribute va = (ValueAttribute) a;
                va.setValue(entry.getValue());
            } else if (a instanceof ReferenceAttribute) {
                ReferenceAttribute ra = (ReferenceAttribute) a;
                ra.setReference(dao.findByName(entry.getValue()));
            } else {
                throw new UnsupportedOperationException();
            }
        }
        po.setName(po.extractNewName(updates));
        dao.persist(po);
    }

    @Override
    public boolean validateNameUpdate(PersistedObject po, Map<String, String> updates) {
        String newname = po.extractNewName(updates);
        if (po.getName().equals(newname)) {
            return true;
        }
        return dao.findByName(po.getServiceAssembly(), newname) == null;
    }


    public void setPersistedObjectDao(PersistedObjectDao dao) {
        this.dao = dao;
    }
}
