package io.github.philkes.slf4j.callerinfo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.List;
import java.util.stream.Collectors;

/**
 * In memory slf4j appender<br/>
 * Convenient appender to be able to check slf4j invocations.<br/>
 * Source: <a href="https://github.com/eugenp/tutorials/blob/master/testing-modules/testing-assertions/src/test/java/com/baeldung/junit/log/MemoryAppender.java">github.com/eugenp</a>
 */
public class MemoryAppender extends ListAppender<ILoggingEvent> {
    public List<ILoggingEvent> search(String string, Level level) {
        return this.list.stream()
                .filter(event -> event.toString().contains(string)
                        && event.getLevel().equals(level))
                .collect(Collectors.toList());
    }
}
