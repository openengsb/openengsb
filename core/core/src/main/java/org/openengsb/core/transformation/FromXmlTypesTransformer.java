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
import org.openengsb.core.xmlmapping.XMLMapable;
import org.openengsb.core.xmlmapping.XMLPrimitive;

public class FromXmlTypesTransformer {

    private Map<String, Object> references = new HashMap<String, Object>();

    Object toObject(XMLMapable mapable) {
        if (mapable.ifNull()) {
            return null;
        } else if (mapable.ifPrimitive()) {
            return toObject(mapable.getPrimitive(), mapable.getId());
        } else if (mapable.ifList()) {
            return toList(mapable.getList().getMapables(), mapable.getId());
        } else if (mapable.ifMap()) {
            return toMap(mapable.getMap().getMapEntries(), mapable.getId());
        } else if (mapable.ifEvent()) {
            return toEvent(mapable.getEvent(), mapable.getId());
        } else if (mapable.ifContext()) {
            return toContext(mapable.getContext(), mapable.getId());
        } else if (mapable.ifBean()) {
            return toBean(mapable.getBean(), mapable.getId());
        } else if (mapable.ifReference()) {
            return references.get(mapable.getReference().getId());
        }
        throw new IllegalStateException();
    }

    private Object toBean(XMLBean bean, String id) {
        Class<?> clazz = TransformerUtil.simpleGetClass(bean.getClassName());
        Object beanObject = TransformerUtil.getInstance(clazz);
        references.put(id, beanObject);
        for (XMLField xmlField : bean.getFields()) {
            Field field = TransformerUtil.getField(clazz, xmlField.getFieldName());
            TransformerUtil.setValue(field, beanObject, toObject(xmlField.getValue()));
        }
        return beanObject;
    }

    private Context toContext(XMLContext context, String id) {
        Context result = ContextTransformer.toContext(context);
        references.put(id, result);
        return result;
    }

    private Object toList(List<XMLMapable> list, String id) {
        List<Object> result = new ArrayList<Object>(list.size());
        references.put(id, result);
        for (XMLMapable m : list) {
            result.add(toObject(m));
        }
        return result;
    }

    private Object toMap(List<XMLMapEntry> map, String id) {
        Map<Object, Object> result = new HashMap<Object, Object>(map.size());
        references.put(id, result);
        for (XMLMapEntry entry : map) {
            result.put(toObject(entry.getKey()), toObject(entry.getValue()));
        }
        return result;
    }

    Event toEvent(XMLEvent xmlEvent, String id) {
        Event event = null;
        try {
            Class<?> clazz = TransformerUtil.simpleGetClass(xmlEvent.getClassName());
            event = (Event) TransformerUtil.getInstance(clazz);
            event.setDomain(xmlEvent.getDomain());
            event.setName(xmlEvent.getName());
        } catch (Exception e) {
            // fallback
            event = new Event(xmlEvent.getDomain(), xmlEvent.getName());
        }
        event.setToolConnector(xmlEvent.getToolConnector());
        references.put(id, event);
        for (XMLMapEntry entry : xmlEvent.getElements()) {
            event.setValue((String) toObject(entry.getKey()), toObject(entry.getValue()));
        }
        return event;
    }

    private Object toObject(XMLPrimitive primitive, String string) {
        Object result = null;
        if (primitive.ifBoolean()) {
            result = primitive.isBoolean();
        } else if (primitive.ifByte()) {
            result = primitive.getByte();
        } else if (primitive.ifDouble()) {
            result = primitive.getDouble();
        } else if (primitive.ifFloat()) {
            result = primitive.getFloat();
        } else if (primitive.ifInt()) {
            result = primitive.getInt();
        } else if (primitive.ifShort()) {
            result = primitive.getShort();
        } else if (primitive.ifString()) {
            result = primitive.getString();
        } else if (primitive.ifLong()) {
            result = primitive.getLong();
        } else if (primitive.ifBase64Binary()) {
            result = primitive.getBase64Binary();
        }

        if (result != null) {
            references.put(string, result);
            return result;
        }
        throw new IllegalStateException();
    }

}
