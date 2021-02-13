package nl.amis.smeetsm.agent;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class RemsyncTransformer implements ClassFileTransformer {
    private static void log(String line) {
        System.out.println(RemsyncTransformer.class.getName().toString()+" : "+line);
    }

    private final ScopedClassPoolFactoryImpl scopedClassPoolFactory = new ScopedClassPoolFactoryImpl();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        if (!className.startsWith("java/")) {
            //log("[Agent] Transforming class: " + className);
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
                        //log("[Agent] Method is synchronized: " + method.getName());
                        if (Modifier.isStatic(modifier)) {
                            //https://paluch.biz/blog/183-carrier-kernel-thread-pinning-of-virtual-threads-project-loom.html
                            try {
                                ctField = ctClass.getDeclaredField("lockCustomAgentStatic");
                            } catch (NotFoundException e) {
                                ctClass.addField(CtField.make("final static java.util.concurrent.locks.Lock lockCustomAgentStatic = new java.util.concurrent.locks.ReentrantLock();", ctClass));
                            }

                            //https://stackoverflow.com/questions/41156596/using-javassist-to-insert-try-finally-logic-that-wraps-the-original-method-logic
                            method.instrument(new ExprEditor() {
                                public void edit(MethodCall m) throws CannotCompileException {
                                    m.replace("{ lockCustomAgentStatic.lock(); try { $_ = $proceed($$); } finally { lockCustomAgentStatic.unlock(); } }");
                                }
                            });
                        } else {
                            try {
                                ctField = ctClass.getDeclaredField("lockCustomAgent");
                            } catch (NotFoundException e) {
                                ctClass.addField(CtField.make("final java.util.concurrent.locks.Lock lockCustomAgent = new java.util.concurrent.locks.ReentrantLock();", ctClass));
                            }

                            method.instrument(new ExprEditor() {
                                public void edit(MethodCall m) throws CannotCompileException {
                                    m.replace("{ lockCustomAgent.lock(); try { $_ = $proceed($$); } finally { lockCustomAgent.unlock(); } }");
                                }
                            });
                        }
                        modifier = Modifier.clear(modifier, Modifier.SYNCHRONIZED);
                        method.setModifiers(modifier);
                    }
                }

                byteCode = ctClass.toBytecode();
                ctClass.detach();
                //log("[Agent] Class transformed: "+className);
            } catch (Exception e) {
                //log("[Agent] Failed to transform: "+className+ " Cause: "+e.getMessage());
            }
        }
        return byteCode;
    }
}
