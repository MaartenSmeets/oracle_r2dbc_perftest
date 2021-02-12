package nl.amis.smeetsm.agent;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
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
            CtField ctField = null;
            for (CtMethod method : methods) {
                modifier = method.getModifiers();
                if (Modifier.isSynchronized(modifier)) {
                    LOGGER.info("Method is synchronized: " + method.getName());

                    //https://paluch.biz/blog/183-carrier-kernel-thread-pinning-of-virtual-threads-project-loom.html
                    try {
                        ctField = ctClass.getDeclaredField("lockCustomAgent");
                    } catch (NotFoundException e) {
                        ctClass.addField(CtField.make("final java.util.concurrent.locks.Lock lockCustomAgent = new java.util.concurrent.locks.ReentrantLock();",ctClass));
                    }

                    //https://stackoverflow.com/questions/41156596/using-javassist-to-insert-try-finally-logic-that-wraps-the-original-method-logic
                    method.instrument(new ExprEditor(){
                        public void edit(MethodCall m) throws CannotCompileException {
                            m.replace("{ lockCustomAgent.lock(); " +
                                    "try { $_ = $proceed($$); } " +
                                    "finally { lockCustomAgent.unlock(); } " +
                                    "}");
                        }
                    });


                    method.insertBefore("lockCustomAgent.lock();\n" +
                            "        try {");
                    method.insertAfter("        } finally {\n" +
                            "            lockCustomAgent.unlock();\n" +
                            "        }");

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
