package com.gempukku.stccg.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class LoggingProxy {
    private static final Logger LOGGER = LogManager.getLogger(LoggingProxy.class);
    private static final long ERROR_LEVEL = 3000;
    private static final long WARN_LEVEL = 1000;
    private static final long INFO_LEVEL = 500;
    private static final long DEBUG_LEVEL = 100;

    @SuppressWarnings("unchecked")
    public static <T> T createLoggingProxy(Class<T> clazz, T delegate) {
        final String simpleName = clazz.getSimpleName();
        ClassLoader classLoader = LoggingProxy.class.getClassLoader();
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{clazz},
                (proxy, method, args) -> {
                    long start = System.currentTimeMillis();
                    try {
                        return method.invoke(delegate, args);
                    } catch (InvocationTargetException exp) {
                        throw exp.getTargetException();
                    } finally {
                        long time = System.currentTimeMillis() - start;
                        String name = method.getName();
                        if (time >= ERROR_LEVEL)
                            LOGGER.error(simpleName + "::" + name + "(...) " + time + "ms");
                        else if (time >= WARN_LEVEL)
                            LOGGER.warn(simpleName + "::" + name + "(...) " + time + "ms");
                        else if (time >= INFO_LEVEL)
                            LOGGER.info(simpleName + "::" + name + "(...) " + time + "ms");
                        else if (time >= DEBUG_LEVEL)
                            LOGGER.debug(simpleName + "::" + name + "(...) " + time + "ms");
                        else
                            LOGGER.trace(simpleName + "::" + name + "(...) " + time + "ms");
                    }
                });
    }
}