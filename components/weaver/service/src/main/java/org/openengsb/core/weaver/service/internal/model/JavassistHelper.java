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
        return String.format("%s%s", Character.toLowerCase(methodName.charAt(3)), methodName.substring(4));
    }

}
