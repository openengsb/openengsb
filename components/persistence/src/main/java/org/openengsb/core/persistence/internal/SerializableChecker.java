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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.persistence.internal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that analyzes objects for non-serializable nodes. Construct, then call {@link #check(Object)} with the
 * object you want to check. When a non-serializable object is found, a {@link WicketNotSerializableException} is thrown
 * with a message that shows the trace up to the not-serializable object. The exception is thrown for the first
 * non-serializable instance it encounters, so multiple problems will not be shown.
 * <p>
 * As this class depends heavily on JDK's serialization internals using introspection, analyzing may not be possible,
 * for instance when the runtime environment does not have sufficient rights to set fields accessible that would
 * otherwise be hidden. You should call {@link SerializableChecker#isAvailable()} to see whether this class can operate
 * properly. If it doesn't, you should fall back to e.g. re-throwing/ printing the {@link NotSerializableException} you
 * probably got before using this class.
 * </p>
 *
 * @author eelcohillenius
 * @author Al Maw
 */
public final class SerializableChecker extends ObjectOutputStream {

    /** log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializableChecker.class);

    /**
     * Exception that is thrown when a non-serializable object was found.
     */
    public static final class ObjectDbNotSerializableException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        ObjectDbNotSerializableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Does absolutely nothing.
     */
    private static class NoopOutputStream extends OutputStream {
        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(byte[] b) {
        }

        @Override
        public void write(byte[] b, int i, int l) {
        }

        @Override
        public void write(int b) {
        }
    }

    private abstract static class ObjectOutputAdaptor implements ObjectOutput {

        @Override
        public void writeBoolean(boolean v) throws IOException {
        }

        @Override
        public void writeByte(int v) throws IOException {
        }

        @Override
        public void writeShort(int v) throws IOException {
        }

        @Override
        public void writeChar(int v) throws IOException {
        }

        @Override
        public void writeInt(int v) throws IOException {
        }

        @Override
        public void writeLong(long v) throws IOException {
        }

        @Override
        public void writeFloat(float v) throws IOException {
        }

        @Override
        public void writeDouble(double v) throws IOException {
        }

        @Override
        public void writeBytes(String s) throws IOException {
        }

        @Override
        public void writeChars(String s) throws IOException {
        }

        @Override
        public void writeUTF(String s) throws IOException {
        }

        @Override
        public void writeObject(Object obj) throws IOException {
        }

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

    }

    /** Holds information about the field and the resulting object being traced. */
    private static final class TraceSlot {
        private final String fieldDescription;

        private final Object object;

        TraceSlot(Object object, String fieldDescription) {
            super();
            this.object = object;
            this.fieldDescription = fieldDescription;
        }

        @Override
        public String toString() {
            return object.getClass() + " - " + fieldDescription;
        }
    }

    private static final NoopOutputStream DUMMY_OUTPUT_STREAM = new NoopOutputStream();

    /** Whether we can execute the tests. If false, check will just return. */
    private static boolean available = true;

    // this hack - accessing the serialization API through introspection - is
    // the only way to use Java serialization for our purposes without writing
    // the whole thing from scratch (and even then, it would be limited). This
    // way of working is of course fragile for internal API changes, but as we
    // do an extra check on availability and we report when we can't use this
    // introspection fu, we'll find out soon enough and clients on this class
    // can fall back on Java's default exception for serialization errors (which
    // sucks and is the main reason for this attempt).
    private static Method lookupMethod;

    private static Method getClassDataLayoutMethod;

    private static Method getNumObjFieldsMethod;

    private static Method getObjFieldValuesMethod;

    private static Method getFieldMethod;

    private static Method hasWriteReplaceMethodMetod;

    private static Method invokeWriteReplaceMethod;

    static {
        try {
            lookupMethod = ObjectStreamClass.class.getDeclaredMethod("lookup", new Class[]{
                Class.class, Boolean.TYPE });
            lookupMethod.setAccessible(true);

            getClassDataLayoutMethod = ObjectStreamClass.class.getDeclaredMethod(
                "getClassDataLayout", (Class[]) null);
            getClassDataLayoutMethod.setAccessible(true);

            getNumObjFieldsMethod = ObjectStreamClass.class.getDeclaredMethod(
                "getNumObjFields", (Class[]) null);
            getNumObjFieldsMethod.setAccessible(true);

            getObjFieldValuesMethod = ObjectStreamClass.class.getDeclaredMethod(
                "getObjFieldValues", new Class[]{ Object.class, Object[].class });
            getObjFieldValuesMethod.setAccessible(true);

            getFieldMethod = ObjectStreamField.class.getDeclaredMethod("getField", (Class[]) null);
            getFieldMethod.setAccessible(true);

            hasWriteReplaceMethodMetod = ObjectStreamClass.class.getDeclaredMethod(
                "hasWriteReplaceMethod", (Class[]) null);
            hasWriteReplaceMethodMetod.setAccessible(true);

            invokeWriteReplaceMethod = ObjectStreamClass.class.getDeclaredMethod(
                "invokeWriteReplace", new Class[]{ Object.class });
            invokeWriteReplaceMethod.setAccessible(true);
        } catch (Exception e) {
            LOGGER.warn("SerializableChecker not available", e);
            available = false;
        }
    }

    /**
     * Gets whether we can execute the tests. If false, calling {@link #check(Object)} will just return and you are
     * advised to rely on the {@link NotSerializableException}. Clients are advised to call this method prior to calling
     * the check method.
     *
     * @return whether security settings and underlying API etc allow for accessing the serialization API using
     *         introspection
     */
    public static boolean isAvailable() {
        return available;
    }

    /** object stack that with the trace path. */
    private final LinkedList<TraceSlot> traceStack = new LinkedList<TraceSlot>();

    /** set for checking circular references. */
    private final Map<Object, Object> checked = new IdentityHashMap<Object, Object>();

    /** string stack with current names pushed. */
    private final LinkedList<String> nameStack = new LinkedList<String>();

    /** root object being analyzed. */
    private Object root;

    /** set of classes that had no writeObject methods at lookup (to avoid repeated checking) */
    private final Set<Class<?>> writeObjectMethodMissing = new HashSet<Class<?>>();

    /** current simple field name. */
    private String simpleName = "";

    /** current full field description. */
    private String fieldDescription;

    /** Exception that should be set as the cause when throwing a new exception. */
    private final NotSerializableException exception;

    private final Stack<Object> stack = new Stack<Object>();

    /**
     * Construct.
     *
     * @param exception exception that should be set as the cause when throwing a new exception
     *
     * @throws IOException
     */
    public SerializableChecker(NotSerializableException exception) throws IOException {
        this.exception = exception;
    }

    /**
     * @see java.io.ObjectOutputStream#reset()
     */
    @Override
    public void reset() throws IOException {
        root = null;
        checked.clear();
        fieldDescription = null;
        simpleName = null;
        traceStack.clear();
        nameStack.clear();
        writeObjectMethodMissing.clear();
    }

    private void check(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            if (stack.contains(obj)) {
                return;
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Wasn't possible to check the object " + obj.getClass()
                    + " possible due an problematic implementation of equals method");
            /*
             * Can't check if this obj were in stack, giving up because we don't want to throw an invaluable exception
             * to user. The main goal of this checker is to find non serializable data
             */
            return;
        }

        stack.push(obj);
        try {
            internalCheck(obj);
        } finally {
            stack.pop();
        }
    }

    private void internalCheck(Object obj) {
        if (obj == null) {
            return;
        }
        Class<?> cls = obj.getClass();
        nameStack.add(simpleName);
        traceStack.add(new TraceSlot(obj, fieldDescription));
        if (!(obj instanceof Serializable) && (!Proxy.isProxyClass(cls))) {
            throw new ObjectDbNotSerializableException(toPrettyPrintedStack(obj.getClass().getName()), exception);
        }
        ObjectStreamClass desc;
        for (;;) {
            try {
                desc = (ObjectStreamClass) lookupMethod.invoke(null, cls, Boolean.TRUE);
                obj = invokeWriteReplaceMethod.invoke(desc, obj);
                Class<?> repCl = obj.getClass();
                if (!(Boolean) hasWriteReplaceMethodMetod.invoke(desc, (Object[]) null)
                        || obj == null
                        || repCl == cls) {
                    break;
                }
                cls = repCl;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (cls.isPrimitive()) {
            LOGGER.trace("skip primitive check");
        } else if (cls.isArray()) {
            checked.put(obj, null);
            Class<?> ccl = cls.getComponentType();
            if (!(ccl.isPrimitive())) {
                Object[] objs = (Object[]) obj;
                for (int i = 0; i < objs.length; i++) {
                    String arrayPos = "[" + i + "]";
                    simpleName = arrayPos;
                    fieldDescription += arrayPos;
                    check(objs[i]);
                }
            }
        } else if (obj instanceof Externalizable && (!Proxy.isProxyClass(cls))) {
            Externalizable extObj = (Externalizable) obj;
            try {
                extObj.writeExternal(new ObjectOutputAdaptor() {
                    private int count = 0;

                    @Override
                    public void writeObject(Object streamObj) throws IOException {
                        if (checked.containsKey(streamObj)) {
                            return;
                        }

                        checked.put(streamObj, null);
                        String arrayPos = "[write:" + count++ + "]";
                        simpleName = arrayPos;
                        fieldDescription += arrayPos;

                        check(streamObj);
                    }
                });
            } catch (Exception e) {
                if (e instanceof ObjectDbNotSerializableException) {
                    throw (ObjectDbNotSerializableException) e;
                }
                LOGGER.warn("error delegating to Externalizable : " + e.getMessage() + ", path: " + currentPath());
            }
        } else {
            Method writeObjectMethod = null;
            if (!writeObjectMethodMissing.contains(cls)) {
                try {
                    writeObjectMethod =
                        cls.getDeclaredMethod("writeObject", new Class[]{ java.io.ObjectOutputStream.class });
                } catch (SecurityException e) {
                    writeObjectMethodMissing.add(cls);
                } catch (NoSuchMethodException e) {
                    writeObjectMethodMissing.add(cls);
                }
            }
            final Object original = obj;
            if (writeObjectMethod != null) {
                class InterceptingObjectOutputStream extends ObjectOutputStream {
                    private int counter;

                    InterceptingObjectOutputStream() throws IOException {
                        super(DUMMY_OUTPUT_STREAM);
                        enableReplaceObject(true);
                    }

                    @Override
                    protected Object replaceObject(Object streamObj) throws IOException {
                        if (streamObj == original) {
                            return streamObj;
                        }
                        counter++;
                        if (checked.containsKey(streamObj)) {
                            return null;
                        }
                        checked.put(streamObj, null);
                        String arrayPos = "[write:" + counter + "]";
                        simpleName = arrayPos;
                        fieldDescription += arrayPos;
                        check(streamObj);
                        return streamObj;
                    }
                }
                try {
                    InterceptingObjectOutputStream ioos = new InterceptingObjectOutputStream();
                    ioos.writeObject(obj);
                } catch (Exception e) {
                    if (e instanceof ObjectDbNotSerializableException) {
                        throw (ObjectDbNotSerializableException) e;
                    }
                    LOGGER.warn("error delegating to writeObject : " + e.getMessage() + ", path: " + currentPath());
                }
            } else {
                Object[] slots;
                try {
                    slots = (Object[]) getClassDataLayoutMethod.invoke(desc, (Object[]) null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                for (Object slot : slots) {
                    ObjectStreamClass slotDesc;
                    try {
                        Field descField = slot.getClass().getDeclaredField("desc");
                        descField.setAccessible(true);
                        slotDesc = (ObjectStreamClass) descField.get(slot);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    checked.put(obj, null);
                    checkFields(obj, slotDesc);
                }
            }
        }
        traceStack.removeLast();
        nameStack.removeLast();
    }

    private void checkFields(Object obj, ObjectStreamClass desc) {
        int numFields;
        try {
            numFields = (Integer) getNumObjFieldsMethod.invoke(desc, (Object[]) null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        if (numFields > 0) {
            int numPrimFields;
            ObjectStreamField[] fields = desc.getFields();
            Object[] objVals = new Object[numFields];
            numPrimFields = fields.length - objVals.length;
            try {
                getObjFieldValuesMethod.invoke(desc, obj, objVals);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < objVals.length; i++) {
                if (objVals[i] instanceof String || objVals[i] instanceof Number || objVals[i] instanceof Date
                        || objVals[i] instanceof Boolean || objVals[i] instanceof Class) {
                    continue;
                }

                // Check for circular reference.
                if (checked.containsKey(objVals[i])) {
                    continue;
                }

                ObjectStreamField fieldDesc = fields[numPrimFields + i];
                Field field;
                try {
                    field = (Field) getFieldMethod.invoke(fieldDesc, (Object[]) null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

                field.getName();
                simpleName = field.getName();
                fieldDescription = field.toString();
                check(objVals[i]);
            }
        }
    }

    /**
     * @return name from root to current node concatenated with slashes
     */
    private StringBuilder currentPath() {
        StringBuilder b = new StringBuilder();
        for (Iterator<String> it = nameStack.iterator(); it.hasNext();) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append('/');
            }
        }
        return b;
    }

    /**
     * Dump with indentation.
     *
     * @param type the type that couldn't be serialized
     * @return A very pretty dump
     */
    private String toPrettyPrintedStack(String type) {
        StringBuilder result = new StringBuilder();
        StringBuilder spaces = new StringBuilder();
        result.append("Unable to serialize class: ");
        result.append(type);
        result.append("\nField hierarchy is:");
        for (Iterator<TraceSlot> i = traceStack.listIterator(); i.hasNext();) {
            spaces.append("  ");
            TraceSlot slot = i.next();
            result.append("\n").append(spaces).append(slot.fieldDescription);
            result.append(" [class=").append(slot.object.getClass().getName());
            result.append("]");
        }
        result.append(" <----- field that is not serializable");
        return result.toString();
    }

    /**
     * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
     */
    @Override
    protected void writeObjectOverride(Object obj) throws IOException {
        if (!available) {
            return;
        }
        root = obj;
        check(root);
    }
}
