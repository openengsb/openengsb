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

import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.ekb.annotations.Model;
import org.openengsb.core.api.model.FileWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelId;
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

public final class ManipulationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManipulationUtils.class);
    private static ClassPool cp = ClassPool.getDefault();
    private static boolean initiated = false;

    private ManipulationUtils() {
    }

    public static void appendClassLoader(ClassLoader loader) {
        cp.appendClassPath(new LoaderClassPath(loader));
    }

    private static void initiate() {
        cp.importPackage("java.util");
        cp.importPackage("java.lang.reflect");
        cp.importPackage("org.openengsb.core.api.model");
        initiated = true;
    }

    public static byte[] enhanceModel(byte[] byteCode, ClassLoader... loaders) throws IOException,
        CannotCompileException {
        CtClass cc = doModelModifications(byteCode, loaders);
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
            if (!JavassistHelper.hasAnnotation(cc, Model.class.getName())) {
                return cc;
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
            addGetOpenEngSBModelEntries(cc);

            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
            LOGGER.info("Finished model enhancing for class {}", cc.getName());

            for (int i = 0; i < loaders.length; i++) {
                cp.removeClassPath(classloaders[i]);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (RuntimeException e1) {
            e1.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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

    private static void addGetOpenEngSBModelEntries(CtClass clazz) throws NotFoundException,
        CannotCompileException, ClassNotFoundException {
        CtMethod m = new CtMethod(cp.get(List.class.getName()), "getOpenEngSBModelEntries", new CtClass[]{}, clazz);

        StringBuilder builder = new StringBuilder();
        builder.append("{ \nList elements = new ArrayList();\n");
        builder.append("elements.addAll(openEngSBModelTail.values());\n");
        for (CtMethod method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            String property = JavassistHelper.generatePropertyName(methodName);
            if (methodName.startsWith("get") && !methodName.equals("getOpenEngSBModelEntries")) {
                if (method.getReturnType().equals(cp.get(File.class.getName()))) {
                    String wrapperName = property + "wrapper";
                    builder.append("if(").append(methodName).append("() == null) {");
                    builder.append("elements.add(new OpenEngSBModelEntry(\"");
                    builder.append(wrapperName).append("\", null, FileWrapper.class));}\n");
                    builder.append("else {");
                    builder.append("FileWrapper ").append(wrapperName).append(" = new FileWrapper(");
                    builder.append(methodName).append("());\n").append(wrapperName).append(".serialize();\n");
                    builder.append("elements.add(new OpenEngSBModelEntry(\"");
                    builder.append(wrapperName).append("\", ").append(wrapperName);
                    builder.append(", ").append(wrapperName).append(".getClass()));}\n");
                    addFileFunction(clazz, property);
                } else {
                    builder.append("elements.add(new OpenEngSBModelEntry(\"");
                    builder.append(property).append("\", ").append(methodName).append("()");
                    builder.append(", ").append(method.getReturnType().getName()).append(".class));\n");
                }
            }
            if (methodName.startsWith("set") && JavassistHelper.hasAnnotation(method,
                OpenEngSBModelId.class.getName())) {
                StringBuilder idBuilder = new StringBuilder();
                idBuilder.append("openEngSBModelTail.put(\"").append(EDBConstants.MODEL_OID).append("\",");
                idBuilder.append(" new OpenEngSBModelEntry(\"").append(EDBConstants.MODEL_OID).append("\",");
                idBuilder.append("$1, ").append(method.getParameterTypes()[0].getName()).append(".class));\n");
                method.insertAfter(idBuilder.toString());
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
