package com.philkes.plugins.slf4j.callerinfo;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import static org.junit.Assert.assertEquals;

public class Slf4jCallerInfoMojoTest {
    /**
     * Inject appender to fetch all logs from {@link LoggingTest#log(String)} in memory
     */
    private MemoryAppender memoryAppender;

    @Before
    public void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingTest.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(ch.qos.logback.classic.Level.TRACE);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void testMojoGoal() throws Exception {
        String msg = "This is a test message";
        new LoggingTest().log(msg);
        int infoLogLineNumber = 13; // see LoggingTest class log.info() call line number
        String methodName = "log";
        // Check if all log statements have the correct MDC properties
        for (Level level : new Level[]{Level.INFO, Level.WARN, Level.ERROR, Level.DEBUG, Level.TRACE}) {
            List<ILoggingEvent> logEvents = memoryAppender.search(msg, level);
            assertEquals(1, logEvents.size());
            ILoggingEvent logEvent = logEvents.get(0);
            Map<String, String> mdcPropertyMap = logEvent.getMDCPropertyMap();
            assertEquals(methodName, mdcPropertyMap.get("callerMethod"));
            assertEquals(infoLogLineNumber++,  Integer.parseInt(mdcPropertyMap.get("callerLine")));
        }

    }
}
