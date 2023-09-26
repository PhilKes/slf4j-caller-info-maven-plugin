package io.github.philkes.slf4j.callerinfo;

/**
 * Example Class using an SLF4J Logger on different Levels
 */
public class Test {
    private LoggingWrapper loggingWrapper = new LoggingWrapper();

    public void log(String msg) {
        loggingWrapper.customLogMethod(msg);
        loggingWrapper.excludedMethod(msg);
        loggingWrapper.customLogMethod2(msg);
    }

    public LoggingWrapper getLoggingWrapper() {
        return loggingWrapper;
    }
}
