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

package org.openengsb.core.weaver.service.internal.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.openengsb.core.api.model.FileWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.IgnoredModelField;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.util.ModelUtils;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * This util class does the byte code manipulation to enhance domain models. It uses Javassist as code manipulation
 * library.
 */
public final class ManipulationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManipulationUtils.class);
    private static final String TAIL_FIELD = ModelUtils.MODEL_TAIL_FIELD_NAME;
    private static final String LOGGER_FIELD = "_INTERNAL_LOGGER";
    private static ClassPool cp = ClassPool.getDefault();
    private static boolean initiated = false;

    private ManipulationUtils() {
    }

    /**
     * Appends a class loader to the class pool.
     */
    public static void appendClassLoader(ClassLoader loader) {
        cp.appendClassPath(new LoaderClassPath(loader));
    }

    private static void initiate() {
        cp.importPackage("java.util");
        cp.importPackage("java.lang.reflect");
        cp.importPackage("org.openengsb.core.api.model");
        cp.importPackage("org.slf4j");
        cp.importPackage("org.osgi.framework");
        initiated = true;
    }

    /**
     * Try to enhance the object defined by the given byte code. Returns the enhanced class or null, if the given class
     * is no model, as byte array. The version of the model will be set statical to 1.0.0. There may be class loaders
     * appended, if needed.
     */
    public static byte[] enhanceModel(byte[] byteCode, ClassLoader... loaders) throws IOException,
        CannotCompileException {
        return enhanceModel(byteCode, new Version("1.0.0"), loaders);
    }

    /**
     * Try to enhance the object defined by the given byte code. Returns the enhanced class or null, if the given class
     * is no model, as byte array. There may be class loaders appended, if needed.
     */
    public static byte[] enhanceModel(byte[] byteCode, Version modelVersion, ClassLoader... loaders)
        throws IOException, CannotCompileException {
        CtClass cc = doModelModifications(byteCode, modelVersion, loaders);
        if (cc == null) {
            return null;
        }
        byte[] newClass = cc.toBytecode();
        cc.defrost();
        cc.detach();
        return newClass;
    }

    /**
     * Try to perform the actual model enhancing.
     */
    private static CtClass doModelModifications(byte[] byteCode, Version modelVersion, ClassLoader... loaders) {
        if (!initiated) {
            initiate();
        }
        CtClass cc = null;
        LoaderClassPath[] classloaders = new LoaderClassPath[loaders.length];
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            cc = cp.makeClass(stream);
            if (!JavassistUtils.hasAnnotation(cc, Model.class.getName())) {
                return null;
            }
            LOGGER.debug("Model to enhance: {}", cc.getName());
            for (int i = 0; i < loaders.length; i++) {
                classloaders[i] = new LoaderClassPath(loaders[i]);
                cp.appendClassPath(classloaders[i]);
            }
            doEnhancement(cc, modelVersion);
            LOGGER.info("Finished model enhancing for class {}", cc.getName());
        } catch (IOException e) {
            LOGGER.error("IOException while trying to enhance model", e);
        } catch (RuntimeException e) {
            LOGGER.error("RuntimeException while trying to enhance model", e);
        } catch (CannotCompileException e) {
            LOGGER.error("CannotCompileException while trying to enhance model", e);
        } catch (NotFoundException e) {
            LOGGER.error("NotFoundException while trying to enhance model", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("ClassNotFoundException while trying to enhance model", e);
        } finally {
            for (int i = 0; i < loaders.length; i++) {
                if (classloaders[i] != null) {
                    cp.removeClassPath(classloaders[i]);
                }
            }
        }
        return cc;
    }

    /**
     * Does the steps for the model enhancement.
     */
    private static void doEnhancement(CtClass cc, Version modelVersion) throws CannotCompileException,
        NotFoundException, ClassNotFoundException {
        CtClass inter = cp.get(OpenEngSBModel.class.getName());
        cc.addInterface(inter);
        addFields(cc);
        addGetOpenEngSBModelTail(cc);
        addSetOpenEngSBModelTail(cc);
        addRetrieveModelName(cc);
        addRetrieveModelVersion(cc, modelVersion);
        addOpenEngSBModelEntryMethod(cc);
        addRemoveOpenEngSBModelEntryMethod(cc);
        addRetrieveInternalModelId(cc);
        addRetrieveInternalModelTimestamp(cc);
        addRetrieveInternalModelVersion(cc);
        addGetOpenEngSBModelEntries(cc);
        cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
    }

    /**
     * Adds the fields for the model tail and the logger to the class.
     */
    private static void addFields(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtField tail = CtField.make(String.format("private Map %s = new HashMap();", TAIL_FIELD), clazz);
        clazz.addField(tail);
        String loggerDefinition = "private static final Logger %s = LoggerFactory.getLogger(%s.class.getName());";
        CtField logger = CtField.make(String.format(loggerDefinition, LOGGER_FIELD, clazz.getName()), clazz);
        clazz.addField(logger);
    }

    /**
     * Adds the getOpenEngSBModelTail method to the class.
     */
    private static void addGetOpenEngSBModelTail(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtClass[] params = generateClassField();
        CtMethod method = new CtMethod(cp.get(List.class.getName()), "getOpenEngSBModelTail", params, clazz);
        StringBuilder body = new StringBuilder();
        body.append(createTrace("Called getOpenEngSBModelTail"));
        body.append("return new ArrayList(").append(TAIL_FIELD).append(".values());");
        method.setBody(createMethodBody(body.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the setOpenEngSBModelTail method to the class.
     */
    private static void addSetOpenEngSBModelTail(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtClass[] params = generateClassField(List.class);
        CtMethod method = new CtMethod(CtClass.voidType, "setOpenEngSBModelTail", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called setOpenEngSBModelTail"));
        builder.append("if($1 != null) {for(int i = 0; i < $1.size(); i++) {");
        builder.append("OpenEngSBModelEntry entry = (OpenEngSBModelEntry) $1.get(i);");
        builder.append(TAIL_FIELD).append(".put(entry.getKey(), entry); } }");
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the retreiveModelName method to the class.
     */
    private static void addRetrieveModelName(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtClass[] params = generateClassField();
        CtMethod method = new CtMethod(cp.get(String.class.getName()), "retrieveModelName", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called retrieveModelName"));
        builder.append("return \"").append(clazz.getName()).append("\";");
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the retreiveModelName method to the class.
     */
    private static void addRetrieveModelVersion(CtClass clazz, Version modelVersion) throws CannotCompileException,
        NotFoundException {
        CtClass[] params = generateClassField();
        CtMethod method = new CtMethod(cp.get(String.class.getName()), "retrieveModelVersion", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called retrieveModelVersion"));
        builder.append("return \"").append(modelVersion.toString()).append("\";");
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the addOpenEngSBModelEntry method to the class.
     */
    private static void addOpenEngSBModelEntryMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
        CtClass[] params = generateClassField(OpenEngSBModelEntry.class);
        CtMethod method = new CtMethod(CtClass.voidType, "addOpenEngSBModelEntry", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called addOpenEngSBModelEntry"));
        builder.append("if ($1 != null) { ").append(TAIL_FIELD).append(".put($1.getKey(), $1);}");
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the removeOpenEngSBModelEntry method to the class.
     */
    private static void addRemoveOpenEngSBModelEntryMethod(CtClass clazz) throws NotFoundException,
        CannotCompileException {
        CtClass[] params = generateClassField(String.class);
        CtMethod method = new CtMethod(CtClass.voidType, "removeOpenEngSBModelEntry", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called removeOpenEngSBModelEntry"));
        builder.append("if ($1 != null) { ").append(TAIL_FIELD).append(".remove($1);}");
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the retrieveInternalModelId method to the class.
     */
    private static void addRetrieveInternalModelId(CtClass clazz) throws NotFoundException,
        CannotCompileException {
        CtField modelIdField = null;
        CtClass temp = clazz;
        while (temp != null) {
            for (CtField field : temp.getDeclaredFields()) {
                if (JavassistUtils.hasAnnotation(field, OpenEngSBModelId.class.getName())) {
                    modelIdField = field;
                    break;
                }
            }
            temp = temp.getSuperclass();
        }
        CtClass[] params = generateClassField();
        CtMethod method = new CtMethod(cp.get(Object.class.getName()), "retrieveInternalModelId", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called retrieveInternalModelId"));
        if (modelIdField == null || !isGetterExisting(modelIdField, clazz)) {
            builder.append("return null;");
        } else {
            builder.append(String.format("return %s;",
                getPropertyGetter(modelIdField.getName(), modelIdField.getType().getName())));
        }
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the retrieveInternalModelTimestamp method to the class.
     */
    private static void addRetrieveInternalModelTimestamp(CtClass clazz) throws NotFoundException,
        CannotCompileException {
        CtClass[] params = generateClassField();
        CtMethod method = new CtMethod(cp.get(Long.class.getName()), "retrieveInternalModelTimestamp", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called retrieveInternalModelTimestamp"));
        builder.append(String.format("return (Long) ((OpenEngSBModelEntry)%s.get(\"%s\")).getValue();", TAIL_FIELD,
            EDBConstants.MODEL_TIMESTAMP));
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the retrieveInternalModelVersion method to the class.
     */
    private static void addRetrieveInternalModelVersion(CtClass clazz) throws NotFoundException,
        CannotCompileException {
        CtClass[] params = generateClassField();
        CtMethod method = new CtMethod(cp.get(Integer.class.getName()), "retrieveInternalModelVersion", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Called retrieveInternalModelVersion"));
        builder.append(String.format("return (Integer) ((OpenEngSBModelEntry)%s.get(\"%s\")).getValue();", TAIL_FIELD,
            EDBConstants.MODEL_VERSION));
        method.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(method);
    }

    /**
     * Adds the getOpenEngSBModelEntries method to the class.
     */
    private static void addGetOpenEngSBModelEntries(CtClass clazz) throws NotFoundException,
        CannotCompileException, ClassNotFoundException {
        CtClass[] params = generateClassField();
        CtMethod m = new CtMethod(cp.get(List.class.getName()), "toOpenEngSBModelEntries", params, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(createTrace("Add elements of the model tail"));
        builder.append("List elements = new ArrayList();\n");
        builder.append("elements.addAll(").append(TAIL_FIELD).append(".values());\n");
        builder.append(createTrace("Add properties of the model"));
        builder.append(createModelEntryList(clazz));
        builder.append("return elements;");
        m.setBody(createMethodBody(builder.toString()));
        clazz.addMethod(m);
    }

    /**
     * Generates the list of OpenEngSBModelEntries which need to be added. Also adds the entries of the super classes.
     */
    private static String createModelEntryList(CtClass clazz) throws NotFoundException, CannotCompileException {
        StringBuilder builder = new StringBuilder();
        CtClass tempClass = clazz;
        while (tempClass != null) {
            for (CtField field : tempClass.getDeclaredFields()) {
                String property = field.getName();
                if (property.equals(TAIL_FIELD) || property.equals(LOGGER_FIELD)
                        || JavassistUtils.hasAnnotation(field, IgnoredModelField.class.getName())) {
                    builder.append(createTrace(String.format("Skip property '%s' of the model", property)));
                    continue;
                }
                builder.append(handleField(field, clazz));
            }
            tempClass = tempClass.getSuperclass();
        }
        return builder.toString();
    }

    /**
     * Does the actions needed for a field.
     */
    private static String handleField(CtField field, CtClass clazz) throws NotFoundException, CannotCompileException {
        StringBuilder builder = new StringBuilder();
        CtClass fieldType = field.getType();
        String property = field.getName();
        if (fieldType.equals(cp.get(File.class.getName()))) {
            String wrapperName = property + "wrapper";
            builder.append(createTrace(String.format("Handle File type property '%s'", property)));
            builder.append("if(").append(property).append(" == null) {");
            builder.append("elements.add(new OpenEngSBModelEntry(\"");
            builder.append(wrapperName).append("\", null, FileWrapper.class));}\n");
            builder.append("else {");
            builder.append("FileWrapper ").append(wrapperName).append(" = new FileWrapper(");
            builder.append(property).append(");\n").append(wrapperName).append(".serialize();\n");
            builder.append("elements.add(new OpenEngSBModelEntry(\"");
            builder.append(wrapperName).append("\", ").append(wrapperName);
            builder.append(", ").append(wrapperName).append(".getClass()));}\n");
            addFileFunction(clazz, property);
        } else if (!isGetterExisting(field, clazz)) {
            LOGGER.warn(String.format("Ignoring property '%s' since there is no getter for it defined", property));
        } else if (fieldType.isPrimitive()) {
            builder.append(createTrace(String.format("Handle primitive type property '%s'", property)));
            CtPrimitiveType primitiveType = (CtPrimitiveType) fieldType;
            String wrapperName = primitiveType.getWrapperName();
            builder.append("elements.add(new OpenEngSBModelEntry(\"").append(property).append("\", ");
            builder.append(wrapperName).append(".valueOf(").append(getPropertyGetter(property, wrapperName));
            builder.append("), ").append(wrapperName).append(".class));\n");
        } else {
            builder.append(createTrace(String.format("Handle property '%s'", property)));
            builder.append("elements.add(new OpenEngSBModelEntry(\"");
            builder.append(property).append("\", ").append(getPropertyGetter(property, fieldType.getName()));
            builder.append(", ").append(fieldType.getName()).append(".class));\n");
        }
        return builder.toString();
    }

    /**
     * Returns true if the corresponding getter for the given field is existing, returns false if not
     */
    private static Boolean isGetterExisting(CtField field, CtClass clazz) throws NotFoundException {
        CtMethod method = new CtMethod(field.getType(), "descCreateMethod", new CtClass[]{}, clazz);
        String desc = method.getSignature();
        String getter = getPropertyGetter(field.getName(), field.getType().getName());
        getter = getter.substring(0, getter.length() - 2);
        try {
            clazz.getMethod(getter, desc);
        } catch (NotFoundException e) {
            LOGGER.debug(String.format("No getter with the name '%s' and the description '%s' found", getter, desc));
            return false;
        }
        return true;
    }

    /**
     * Returns the name of the corresponding getter to a properties name and type.
     */
    private static String getPropertyGetter(String property, String type) {
        if (type.equals("java.lang.Boolean") || type.equals("boolean")) {
            return String.format("is%s%s()", Character.toUpperCase(property.charAt(0)), property.substring(1));
        } else {
            return String.format("get%s%s()", Character.toUpperCase(property.charAt(0)), property.substring(1));
        }
    }

    /**
     * Generates a CtClass field out of a Class field.
     */
    private static CtClass[] generateClassField(Class<?>... classes) throws NotFoundException {
        CtClass[] result = new CtClass[classes.length];
        for (int i = 0; i < classes.length; i++) {
            result[i] = cp.get(classes[i].getName());
        }
        return result;
    }

    /**
     * Adds the functionality that the models can handle File objects themselves.
     */
    private static void addFileFunction(CtClass clazz, String property)
        throws NotFoundException, CannotCompileException {
        String wrapperName = property + "wrapper";
        String funcName = "set";
        funcName = funcName + Character.toUpperCase(wrapperName.charAt(0));
        funcName = funcName + wrapperName.substring(1);
        String setterName = "set";
        setterName = setterName + Character.toUpperCase(property.charAt(0));
        setterName = setterName + property.substring(1);
        CtClass[] params = generateClassField(FileWrapper.class);
        CtMethod newFunc = new CtMethod(CtClass.voidType, funcName, params, clazz);
        newFunc.setBody("{ " + setterName + "($1.returnFile());\n }");
        clazz.addMethod(newFunc);
    }

    /**
     * Returns the string which represents a logger tracing call with the given message
     */
    private static String createTrace(String message) {
        return String.format("%s.trace(\"%s\");\n", LOGGER_FIELD, message);
    }

    /**
     * Wraps a body string with a beginning and ending braces
     */
    private static String createMethodBody(String body) {
        return String.format("{%s}", body);
    }
}
