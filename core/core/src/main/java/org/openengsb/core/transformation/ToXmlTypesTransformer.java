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
package org.openengsb.core.transformation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextTransformer;
import org.openengsb.core.model.Event;
import org.openengsb.core.xmlmapping.XMLBean;
import org.openengsb.core.xmlmapping.XMLContext;
import org.openengsb.core.xmlmapping.XMLEvent;
import org.openengsb.core.xmlmapping.XMLField;
import org.openengsb.core.xmlmapping.XMLMapEntry;
import org.openengsb.core.xmlmapping.XMLMapEntryList;
import org.openengsb.core.xmlmapping.XMLMapable;
import org.openengsb.core.xmlmapping.XMLMapableList;
import org.openengsb.core.xmlmapping.XMLPrimitive;
import org.openengsb.core.xmlmapping.XMLReference;

public class ToXmlTypesTransformer {

    private Map<ObjectId, Integer> objectIdToId = new HashMap<ObjectId, Integer>();

    private int counter = 0;

    XMLMapable toMapable(Object o) {
        if (o == null) {
            XMLMapable m = new XMLMapable();
            m.setNull("null");
            return m;
        }

        ObjectId objectId = new ObjectId(System.identityHashCode(o), o.getClass());
        if (objectIdToId.containsKey(objectId)) {
            return toReference(objectId, o);
        }
        XMLMapable m = new XMLMapable();
        m.setId(counter);
        objectIdToId.put(objectId, counter++);
        if (o instanceof Event) {
            m.setEvent(toXmlEvent((Event) o));
        } else if (o instanceof Context) {
            m.setContext(toXmlContext((Context) o));
        } else if (o instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) o;
            List<XMLMapEntry> mapEntries = toMapEntries(map);
            XMLMapEntryList mapEntryList = new XMLMapEntryList();
            mapEntryList.setMapEntries(mapEntries);
            m.setMap(mapEntryList);
        } else if (o instanceof List<?>) {
            List<XMLMapable> mapables = toXmlList((List<?>) o);
            XMLMapableList mapableList = new XMLMapableList();
            mapableList.setMapables(mapables);
            m.setList(mapableList);
        } else if (isPrimitive(o)) {
            XMLPrimitive primitive = toPrimitive(o);
            m.setPrimitive(primitive);
        } else {
            m.setBean(toXmlBean(o));
        }
        return m;
    }

    private XMLMapable toReference(ObjectId objectId, Object o) {
        XMLMapable m = new XMLMapable();
        XMLReference ref = new XMLReference();
        ref.setId(objectIdToId.get(objectId));
        m.setReference(ref);
        return m;
    }

    private List<XMLMapable> toXmlList(List<?> list) {
        List<XMLMapable> result = new ArrayList<XMLMapable>(list.size());
        for (Object o : list) {
            result.add(toMapable(o));
        }
        return result;
    }

    private XMLBean toXmlBean(Object o) {
        XMLBean xmlBean = new XMLBean();
        List<XMLField> fields = new ArrayList<XMLField>();
        for (Field field : o.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            XMLMapable value = toMapable(TransformerUtil.getValue(field, o));

            XMLField xmlField = new XMLField();
            xmlField.setFieldName(field.getName());
            xmlField.setValue(value);

            fields.add(xmlField);
        }
        xmlBean.setClassName(o.getClass().getName());
        xmlBean.setFields(fields);
        return xmlBean;
    }

    private boolean isPrimitive(Object o) {
        return PrimitiveTypes.contains(o.getClass());
    }

    private XMLContext toXmlContext(Context context) {
        return ContextTransformer.toXmlContext(context);
    }

    XMLEvent toXmlEvent(Event event) {
        XMLEvent xmlEvent = new XMLEvent();
        xmlEvent.setClassName(event.getClass().getName());
        xmlEvent.setDomain(event.getDomain());
        xmlEvent.setName(event.getName());
        xmlEvent.setToolConnector(event.getToolConnector());

        List<XMLMapEntry> elements = new ArrayList<XMLMapEntry>();

        for (String key : event.getKeys()) {
            XMLMapEntry entry = new XMLMapEntry();
            entry.setKey(toMapable(key));
            entry.setValue(toMapable(event.getValue(key)));
            elements.add(entry);
        }

        xmlEvent.setElements(elements);
        return xmlEvent;
    }

    private List<XMLMapEntry> toMapEntries(Map<?, ?> map) {
        List<XMLMapEntry> elements = new ArrayList<XMLMapEntry>();
        for (Object key : map.keySet()) {
            XMLMapEntry entry = new XMLMapEntry();
            entry.setKey(toMapable(key));
            entry.setValue(toMapable(map.get(key)));
            elements.add(entry);
        }

        return elements;
    }

    private XMLPrimitive toPrimitive(Object o) {
        XMLPrimitive p = new XMLPrimitive();

        if (o instanceof String) {
            p.setString((String) o);
            return p;
        }

        if (o instanceof Integer) {
            p.setInt((Integer) o);
            return p;
        }

        if (o instanceof Long) {
            p.setLong((Long) o);
            return p;
        }

        if (o instanceof Byte) {
            p.setByte((Byte) o);
            return p;
        }

        if (o instanceof Boolean) {
            p.setBoolean((Boolean) o);
            return p;
        }

        if (o instanceof Double) {
            p.setDouble((Double) o);
            return p;
        }

        if (o instanceof Float) {
            p.setFloat((Float) o);
            return p;
        }

        if (o instanceof Short) {
            p.setShort((Short) o);
            return p;
        }

        if (o instanceof byte[]) {
            p.setBase64Binary((byte[]) o);
            return p;
        }

        throw new IllegalStateException("Type not supported: " + o.getClass());
    }

    private class ObjectId {
        private int systemId;
        private Class<?> type;

        public ObjectId(int systemId, Class<? extends Object> type) {
            this.systemId = systemId;
            this.type = type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + systemId;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ObjectId other = (ObjectId) obj;
            if (systemId != other.systemId)
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
    }
}
