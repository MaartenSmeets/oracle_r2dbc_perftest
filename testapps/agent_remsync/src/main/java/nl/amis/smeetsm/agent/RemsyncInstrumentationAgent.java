package nl.amis.smeetsm.agent;

import java.lang.instrument.Instrumentation;

public class RemsyncInstrumentationAgent {

    private static void log(String line) {
        System.out.println(RemsyncInstrumentationAgent.class.getName().toString()+" : "+line);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        log("[Agent] In premain method");
        inst.addTransformer(new RemsyncTransformer());
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        log("[Agent] In agentmain method");
        inst.addTransformer(new RemsyncTransformer());
    }

}
