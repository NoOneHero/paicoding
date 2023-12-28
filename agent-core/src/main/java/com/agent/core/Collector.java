package com.agent.core;

import cn.hutool.core.util.StrUtil;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author : luweihao
 * @since : 2023/1/10 11:17 上午
 */
public abstract class Collector implements ClassFileTransformer {
    public void register(Instrumentation instrumentation) {
        instrumentation.addTransformer(this);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String targetClass = getTargetClass();
        if (!targetClass.replaceAll("\\.", "/").equals(className)) {
            return null;
        }

        String targetMethod = getTargetMethod();
        String desc = getTargetMethodDesc();
        Integer methodNum = getTargetMethodNum();
        String body = getMethodBody();
        try {
            ClassPool pool = new ClassPool();
            pool.importPackage("java.util");
            pool.appendSystemPath();
//            pool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = pool.get(targetClass);

            CtField field = CtField.make("HashSet set = new HashSet();", ctClass);
            ctClass.addField(field);

            CtMethod ctMethod;
            if (StrUtil.isNotBlank(desc)) {
                ctMethod = ctClass.getMethod(targetMethod, desc);
            } else {
                ctMethod = ctClass.getDeclaredMethods(targetMethod)[methodNum];
            }
            CtMethod newMethod = CtNewMethod.copy(ctMethod, ctClass, null);
            newMethod.setName(targetMethod + "$agent");
            ctClass.addMethod(newMethod);
            ctMethod.setBody(body);
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract String getTargetClass();

    public abstract String getTargetMethod();

    public abstract String getTargetMethodDesc();

    public abstract Integer getTargetMethodNum();

    public abstract String getMethodBody();
}
