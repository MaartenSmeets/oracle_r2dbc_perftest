package nl.amis.smeetsm.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class RemsyncInstrumentationAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemsyncInstrumentationAgent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        LOGGER.info("[Agent] In premain method");
        inst.addTransformer(new RemsyncTransformer());
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        LOGGER.info("[Agent] In agentmain method");
        inst.addTransformer(new RemsyncTransformer());
    }

}
