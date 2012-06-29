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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * This util class does the byte code manipulation to enhance domain models. It uses Javassist as code manipulation
 * library.
 */
public final class ManipulationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManipulationUtils.class);
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
        initiated = true;
    }

    /**
     * Try to enhance the object defined by the given byte code. Returns the enhanced class or the original class, if
     * the given class is no model, as byte array. There may be class loaders appended, if needed.
     */
    public static byte[] enhanceModel(byte[] byteCode, ClassLoader... loaders) throws IOException,
        CannotCompileException {
        CtClass cc = doModelModifications(byteCode, loaders);
        if (cc == null) {
            return null;
        }
        byte[] newClass = cc.toBytecode();
        cc.defrost();
        cc.detach();
        return newClass;
    }

    private static CtClass doModelModifications(byte[] byteCode, ClassLoader... loaders) {
        if (!initiated) {
            initiate();
        }
        CtClass cc = null;
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            cc = cp.makeClass(stream);
            if (!JavassistUtils.hasAnnotation(cc, Model.class.getName())) {
                return null;
            }
            LOGGER.info("Model to enhance: {}", cc.getName());

            LoaderClassPath[] classloaders = new LoaderClassPath[loaders.length];
            for (int i = 0; i < loaders.length; i++) {
                classloaders[i] = new LoaderClassPath(loaders[i]);
                cp.appendClassPath(classloaders[i]);
            }
            CtClass inter = cp.get(OpenEngSBModel.class.getName());
            cc.addInterface(inter);
            addTail(cc);
            addOpenEngSBModelEntryMethod(cc);
            addRemoveOpenEngSBModelEntryMethod(cc);
            addRetrieveInternalModelId(cc);
            addGetOpenEngSBModelEntries(cc);
            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
            LOGGER.info("Finished model enhancing for class {}", cc.getName());
            for (int i = 0; i < loaders.length; i++) {
                cp.removeClassPath(classloaders[i]);
            }
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
        }
        return cc;
    }

    private static void addTail(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtField field = CtField.make("private Map openEngSBModelTail = new HashMap();", clazz);
        clazz.addField(field);
    }

    private static void addOpenEngSBModelEntryMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
        CtMethod method =
            new CtMethod(CtClass.voidType, "addOpenEngSBModelEntry", new CtClass[]{ cp.get(OpenEngSBModelEntry.class
                .getName()) }, clazz);
        method.setBody("{ openEngSBModelTail.put($1.getKey(), $1); }");
        clazz.addMethod(method);
    }

    private static void addRemoveOpenEngSBModelEntryMethod(CtClass clazz) throws NotFoundException,
        CannotCompileException {
        CtMethod method =
            new CtMethod(CtClass.voidType, "removeOpenEngSBModelEntry", new CtClass[]{ cp.get(String.class
                .getName()) }, clazz);
        method.setBody("{ openEngSBModelTail.remove($1); }");
        clazz.addMethod(method);
    }

    private static void addRetrieveInternalModelId(CtClass clazz) throws NotFoundException,
        CannotCompileException {
        String modelIdField = null;
        for (CtField field : clazz.getDeclaredFields()) {
            if (JavassistUtils.hasAnnotation(field, OpenEngSBModelId.class.getName())) {
                modelIdField = field.getName();
                break;
            }
        }
        CtMethod method =
            new CtMethod(cp.get(Object.class.getName()), "retrieveInternalModelId", new CtClass[]{}, clazz);
        if (modelIdField == null) {
            method.setBody("{ return null; }");
        } else {
            method.setBody(String.format("{ return %s; }", modelIdField));
        }
        clazz.addMethod(method);
    }

    private static void addGetOpenEngSBModelEntries(CtClass clazz) throws NotFoundException,
        CannotCompileException, ClassNotFoundException {
        CtMethod m = new CtMethod(cp.get(List.class.getName()), "getOpenEngSBModelEntries", new CtClass[]{}, clazz);

        StringBuilder builder = new StringBuilder();
        builder.append("{ \nList elements = new ArrayList();\n");
        builder.append("elements.addAll(openEngSBModelTail.values());\n");
        for (CtField field : clazz.getDeclaredFields()) {
            String property = field.getName();
            if (property.equals("openEngSBModelTail")
                    || JavassistUtils.hasAnnotation(field, IgnoredModelField.class.getName())) {
                continue;
            }
            CtClass fieldType = field.getType();
            if (fieldType.equals(cp.get(File.class.getName()))) {
                String wrapperName = property + "wrapper";
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
            } else {
                builder.append("elements.add(new OpenEngSBModelEntry(\"");
                builder.append(property).append("\", ").append(property).append(", ");
                builder.append(fieldType.getName()).append(".class));\n");
            }
        }
        builder.append("return elements; } ");
        try {
            m.setBody(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(builder.toString());
        }
        clazz.addMethod(m);
    }

    private static void addFileFunction(CtClass clazz, String property)
        throws NotFoundException, CannotCompileException {
        String wrapperName = property + "wrapper";
        String funcName = "set";
        funcName = funcName + Character.toUpperCase(wrapperName.charAt(0));
        funcName = funcName + wrapperName.substring(1);
        String setterName = "set";
        setterName = setterName + Character.toUpperCase(property.charAt(0));
        setterName = setterName + property.substring(1);
        CtClass[] params = new CtClass[]{ cp.get(FileWrapper.class.getName()) };
        CtMethod newFunc = new CtMethod(CtClass.voidType, funcName, params, clazz);
        newFunc.setBody("{ " + setterName + "($1.returnFile());\n }");
        clazz.addMethod(newFunc);
    }
}
