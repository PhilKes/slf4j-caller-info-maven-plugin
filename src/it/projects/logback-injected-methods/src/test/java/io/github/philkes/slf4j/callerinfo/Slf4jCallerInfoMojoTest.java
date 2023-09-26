package io.github.philkes.slf4j.callerinfo;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.LoggerFactory;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Slf4jCallerInfoMojoTest {

    private MemoryAppender initMemoryAppender(Class<?> classWithlogger) {
        Logger logger = (Logger) LoggerFactory.getLogger(classWithlogger);
        MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(ch.qos.logback.classic.Level.TRACE);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
        return memoryAppender;
    }

    @org.junit.Test
    public void testMojoGoal() {
        Test test = new Test();
        LoggingWrapper loggingWrapper = test.getLoggingWrapper();
        MemoryAppender memoryAppender = initMemoryAppender(loggingWrapper.getClass());
        String msg = "This is a test message";
        test.log(msg);
        int infoLogLineNumber = 10; // see Test class for first loggingWrapper call line number
        String className = test.getClass().getSimpleName() + ".java";
        List<ILoggingEvent> logEvents = memoryAppender.search(msg, Level.INFO);
        assertEquals(3, logEvents.size());
        ILoggingEvent customLogMethodEvent = logEvents.get(0);
        assertEquals(String.format("%s:%s", className, infoLogLineNumber), customLogMethodEvent.getMDCPropertyMap().get("callerInformation"));
        ILoggingEvent excludedMethodEvent = logEvents.get(1);
        assertNull(excludedMethodEvent.getMDCPropertyMap().get("callerInformation"));
        ILoggingEvent customLogMethod2Event = logEvents.get(2);
        assertEquals(String.format("%s:%s", className, infoLogLineNumber + 2), customLogMethod2Event.getMDCPropertyMap().get("callerInformation"));
        memoryAppender.stop();
    }
}
