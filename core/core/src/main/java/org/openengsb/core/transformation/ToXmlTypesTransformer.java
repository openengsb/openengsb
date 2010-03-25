package org.openengsb.core.transformation;

import java.util.List;
import java.util.Map;

import org.openengsb.contextcommon.Context;
import org.openengsb.core.model.Event;
import org.openengsb.core.xmlmapping.XMLMapEntry;
import org.openengsb.core.xmlmapping.XMLMapable;
import org.openengsb.core.xmlmapping.XMLPrimitive;

public class ToXmlTypesTransformer {

    static XMLMapable toMapable(Object o) {
        XMLMapable m = new XMLMapable();
        if (o instanceof Event) {

        } else if (o instanceof Context) {

        } else if (o instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) o;
            List<XMLMapEntry> mapEntries = toMapEntries(map);
            m.setMaps(mapEntries);
        } else if (o instanceof List<?>) {

        } else {
            XMLPrimitive primitive = toPrimitive(o);
            m.setPrimitive(primitive);
        }

        return m;
    }

    private static List<XMLMapEntry> toMapEntries(Map<?, ?> map) {
        // TODO Auto-generated method stub
        return null;
    }

    private static XMLPrimitive toPrimitive(Object o) {
        XMLPrimitive p = new XMLPrimitive();

        if (o instanceof String) {
            p.setString((String) o);
            return p;
        }

        if (o instanceof Integer) {
            p.setInt((Integer) o);
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

        throw new IllegalStateException("Type not supported: " + o.getClass());
    }
}
