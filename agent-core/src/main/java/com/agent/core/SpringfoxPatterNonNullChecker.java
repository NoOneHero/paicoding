package com.agent.core;

import java.util.HashSet;
import java.util.Set;

/**
 * @author : luweihao
 * @since : 2023/1/5 5:55 下午
 */
public class SpringfoxPatterNonNullChecker extends Collector {
    private static final String TARGET_CLASS = "springfox.documentation.spring.web.WebMvcPatternsRequestConditionWrapper";
    private static final String TARGET_METHOD = "getPatterns";

    @Override
    public String getTargetClass() {
        return TARGET_CLASS;
    }

    @Override
    public String getTargetMethod() {
        return TARGET_METHOD;
    }

    @Override
    public String getTargetMethodDesc() {
        return null;
    }

    @Override
    public Integer getTargetMethodNum() {
        return 0;
    }

    @Override
    public String getMethodBody() {

        return "{\nif (this.condition == null) {\n" +
                "        return set;\n" +
                "        }\n" +
                "        return getPatterns$agent($$);}\n";
    }
}
