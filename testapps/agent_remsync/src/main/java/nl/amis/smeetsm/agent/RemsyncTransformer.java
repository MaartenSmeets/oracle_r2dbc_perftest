package nl.amis.smeetsm.agent;

import javassist.*;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class RemsyncTransformer implements ClassFileTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemsyncTransformer.class);
    private ScopedClassPoolFactoryImpl scopedClassPoolFactory = new ScopedClassPoolFactoryImpl();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        LOGGER.info("[Agent] Transforming class: " + className);
        int modifier = 0;
        try {
            ClassPool classPool = scopedClassPoolFactory.create(loader, ClassPool.getDefault(),
                    ScopedClassPoolRepositoryImpl.getInstance());
            //ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod[] methods = ctClass.getMethods();
            for (CtMethod method : methods) {
                modifier = method.getModifiers();
                if (Modifier.isSynchronized(modifier)) {
                    LOGGER.info("Method is synchronized: " + method.getName());
                    modifier = Modifier.clear(modifier, Modifier.SYNCHRONIZED);
                    method.setModifiers(modifier);
                }
            }
            byteCode = ctClass.toBytecode();
            ctClass.detach();
        } catch (CannotCompileException | IOException e) {
            LOGGER.error("Exception", e);
        }
        return byteCode;
    }
}
