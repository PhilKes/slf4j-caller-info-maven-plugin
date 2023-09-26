package io.github.philkes.slf4j.callerinfo;

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

    private void assertCallerInfoLogged(ILogger logger, boolean shouldBeLogged) {
        MemoryAppender memoryAppender = initMemoryAppender(logger.getClass());
        String msg = "This is a test message";
        logger.log(msg);
        int infoLogLineNumber = 13; // see LoggingTest class log.info() call line number
        String className = logger.getClass().getSimpleName() + ".java";
        for (Level level : new Level[]{Level.INFO, Level.WARN, Level.ERROR, Level.DEBUG, Level.TRACE}) {
            List<ILoggingEvent> logEvents = memoryAppender.search(msg, level);
            assertEquals(1, logEvents.size());
            ILoggingEvent logEvent = logEvents.get(0);
            Map<String, String> mdcPropertyMap = logEvent.getMDCPropertyMap();
            if (shouldBeLogged) {
                assertEquals(String.format("%s:%s", className, infoLogLineNumber++), mdcPropertyMap.get("callerInformation"));
            } else {
                assertNull(mdcPropertyMap.get("callerInformation"));
            }
            memoryAppender.stop();
        }
    }

    @Test
    public void testMojoGoal() throws Exception {
        assertCallerInfoLogged(new Included(), true);
        assertCallerInfoLogged(new Excluded(), false);
        assertCallerInfoLogged(new IncludedAndExcluded(), false);
    }
}
