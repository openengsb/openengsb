package org.openengsb.core.transformation;

import java.util.List;

import org.openengsb.core.xmlmapping.XMLBean;
import org.openengsb.core.xmlmapping.XMLContext;
import org.openengsb.core.xmlmapping.XMLEvent;
import org.openengsb.core.xmlmapping.XMLMapEntry;
import org.openengsb.core.xmlmapping.XMLMapable;
import org.openengsb.core.xmlmapping.XMLPrimitive;

public class FromXmlTypesTransformer {

    static Object toObject(XMLMapable mapable) {
        if (mapable.ifPrimitive()) {
            return toObject(mapable.getPrimitive());
        } else if (mapable.ifList()) {
            return xmlListToObject(mapable.getLists()); // ...
        } else if (mapable.ifMap()) {
            return xmlMapToObject(mapable.getMaps());
        } else if (mapable.ifEvent()) {
            return toObject(mapable.getEvent());
        } else if (mapable.ifContext()) {
            return toObject(mapable.getContext());
        } else if (mapable.ifBean()) {
            return toObject(mapable.getBean());
        }
        throw new IllegalStateException();
    }

    private static Object toObject(XMLBean bean) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Object toObject(XMLContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Object xmlListToObject(List<XMLMapable> list) {
        return null; // TODO
    }

    private static Object xmlMapToObject(List<XMLMapEntry> map) {
        return null; // TODO
    }

    private static Object toObject(XMLEvent event) {
        return null; // TODO
    }

    private static Object toObject(XMLPrimitive primitive) {
        if (primitive.ifBoolean()) {
            return primitive.isBoolean();
        }
        if (primitive.ifByte()) {
            return primitive.getByte();
        }
        if (primitive.ifDouble()) {
            return primitive.getDouble();
        }
        if (primitive.ifFloat()) {
            return primitive.getFloat();
        }
        if (primitive.ifInt()) {
            return primitive.getInt();
        }
        if (primitive.ifShort()) {
            return primitive.getShort();
        }
        if (primitive.ifString()) {
            return primitive.getString();
        }
        throw new IllegalStateException();
    }

}
