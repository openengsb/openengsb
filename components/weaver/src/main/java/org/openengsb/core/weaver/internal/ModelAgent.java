package org.openengsb.core.weaver.internal;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.weaver.internal.model.ManipulationUtils;

import javassist.CannotCompileException;

public class ModelAgent implements ClassFileTransformer {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ModelAgent());
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain domain,
            byte[] bytecode)
        throws IllegalClassFormatException {

        if(tryEnhancement(className)) {
            try {
                return ManipulationUtils.enhanceModel(bytecode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
        return bytecode;
    }
    
    private boolean tryEnhancement(String className) {
        List<String> filter = Arrays.asList("java", "$", "sun", "org/junit");
        for(String element : filter) {
            if(className.startsWith(element)) {
                return false;
            }
        }
        return true;
    }
}
