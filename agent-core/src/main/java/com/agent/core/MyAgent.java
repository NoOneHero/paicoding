package com.agent.core;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class MyAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("premain");
        ServiceLoader<Collector> load = ServiceLoader.load(Collector.class);
        for (Collector collector : load) {
            collector.register(instrumentation);
        }
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        System.out.println("agentmain");
    }
}