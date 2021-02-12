package nl.amis.smeetsm;


import javassist.*;

import java.util.Arrays;
import java.util.List;

public class AgentTest {
    static void getInfo() throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get(AgentTest.class.getCanonicalName());
        List<CtMethod> methods = Arrays.asList(cc.getMethods());
        for (CtMethod method : methods) {
            if (method.getLongName().contains(".hi")) {
                System.out.println(method.getLongName() + " : " + Modifier.toString(method.getModifiers()));
            }
        }
    }


    synchronized void hi1() {
        System.out.println("Hi1");
    }

    void hi2() {
        System.out.println("Hi2");
    }

    static void hi3() {
        System.out.println("Hi3");
    }

    static synchronized void hi4() {
        System.out.println("Hi4");
    }

    public static void main(String[] args) throws NotFoundException {
        var me = new AgentTest();
        me.hi1();
        me.hi2();
        AgentTest.hi3();
        AgentTest.hi4();
        AgentTest.getInfo();
    }

}
