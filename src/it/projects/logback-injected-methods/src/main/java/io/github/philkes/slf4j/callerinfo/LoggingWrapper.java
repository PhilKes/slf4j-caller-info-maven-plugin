package io.github.philkes.slf4j.callerinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example class for custom injectedMethods parameter value
 */
public class LoggingWrapper {
    Logger logger = LoggerFactory.getLogger(getClass());

    public void customLogMethod(String msg) {
        logger.info("Super custom log statement with caller-information {}", msg);
    }

    public void customLogMethod2(String msg) {
        logger.info("Super custom log statement with caller-information {}", msg);
    }

    public void excludedMethod(String msg) {
        logger.info("Super custom log statement WITHOUT caller-information {}", msg);
    }
}
