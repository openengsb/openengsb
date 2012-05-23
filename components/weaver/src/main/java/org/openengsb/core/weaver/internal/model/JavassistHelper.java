package org.openengsb.core.weaver.internal.model;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

public final class JavassistHelper {

    private JavassistHelper() {
    }

    public static boolean hasAnnotation(CtClass clazz, String annotationName) {
        ClassFile cf = clazz.getClassFile2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            cf.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)
            cf.getAttribute(AnnotationsAttribute.visibleTag);
        return checkAnnotation(ainfo, ainfo2, annotationName);
    }

    public static boolean hasAnnotation(CtMethod method, String annotationName) {
        MethodInfo info = method.getMethodInfo();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            info.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)
            info.getAttribute(AnnotationsAttribute.visibleTag);
        return checkAnnotation(ainfo, ainfo2, annotationName);
    }

    private static boolean checkAnnotation(AnnotationsAttribute invisible, AnnotationsAttribute visible,
            String annotationName) {
        boolean exist1 = false;
        boolean exist2 = false;
        if (invisible != null) {
            exist1 = invisible.getAnnotation(annotationName) != null;
        }
        if (visible != null) {
            exist2 = visible.getAnnotation(annotationName) != null;
        }
        return exist1 || exist2;
    }

    public static String generatePropertyName(String methodName) {
        return methodName.substring(3).toLowerCase();
    }

}
