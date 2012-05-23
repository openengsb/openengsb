package org.openengsb.core.weaver.internal.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.openengsb.core.api.ekb.annotations.Model;
import org.openengsb.core.api.model.FileWrapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelId;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

public final class ManipulationUtils {
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

    public static byte[] enhanceModel(byte[] byteCode) throws IOException, CannotCompileException {
        CtClass cc = doModelModifications(byteCode);
        byte[] newClass = cc.toBytecode();
        cc.defrost();
        cc.detach();
        return newClass;
    }

    private static CtClass doModelModifications(byte[] byteCode) {
        if (!initiated) {
            initiate();
        }
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            if (!JavassistHelper.hasAnnotation(cc, Model.class.getName())) {
                return cc;
            }
            System.out.println("Model to enhance: " + cc.getName());
            CtClass inter = cp.get(OpenEngSBModel.class.getName());
            cc.addInterface(inter);

            addTail(cc);
            addOpenEngSBModelEntryMethod(cc);
            addRemoveOpenEngSBModelEntryMethod(cc);
            addGetOpenEngSBModelEntries(cc);

            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
            return cc;
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
        return null;
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
    
    private static void addRemoveOpenEngSBModelEntryMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
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
                    builder.append("FileWrapper ").append(wrapperName).append(" = new FileWrapper(");
                    builder.append(methodName).append("());\n").append(wrapperName).append(".serialize();\n");
                    builder.append("elements.add(new OpenEngSBModelEntry(\"");
                    builder.append(wrapperName).append("\", ").append(wrapperName);
                    builder.append(", ").append(wrapperName).append(".getClass()));\n");
                    addFileFunction(clazz, property);
                } else {
                    builder.append("elements.add(new OpenEngSBModelEntry(\"");
                    builder.append(property).append("\", ").append(methodName).append("()");
                    builder.append(", ").append(methodName).append("().getClass()));\n");
                }
            }
            if (methodName.startsWith("set") && JavassistHelper.hasAnnotation(method,
                OpenEngSBModelId.class.getName())) {
                CtField field = new CtField(cp.get(String.class.getName()), "modelId", clazz);
                clazz.addField(field);
                method.insertAfter("modelId = \"\"+$1;");
                CtMethod idGetter = new CtMethod(cp.get(String.class.getName()), "getModelId", new CtClass[]{}, clazz);
                idGetter.setBody("{ return modelId; }");
                clazz.addMethod(idGetter);
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
