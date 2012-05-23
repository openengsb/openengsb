package org.openengsb.core.weaver.internal;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.openengsb.core.weaver.internal.model.ManipulationUtils;

import javassist.CannotCompileException;

public class ModelAgent implements ClassFileTransformer {
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("MyAgent was started - new version");
        inst.addTransformer(new ModelAgent());
    }

    @Override
    public byte[] transform(ClassLoader arg0, String arg1, Class<?> arg2, ProtectionDomain arg3, byte[] arg4)
        throws IllegalClassFormatException {

        if (arg1.startsWith("java") || arg1.startsWith("$") || arg1.startsWith("sun")
                || arg1.startsWith("org/junit")) {
            return arg4;
        }
        
        try {
            return ManipulationUtils.enhanceModel(arg4);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return arg4;
    }
}
