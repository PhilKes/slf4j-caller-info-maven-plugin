package io.github.philkes.slf4j.callerinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example Class using an SLF4J Logger on different Levels
 */
public class IncludedAndExcluded implements ILogger{
    private final Logger log = LoggerFactory.getLogger(IncludedAndExcluded.class);

    public void log(String msg) {
        log.info(msg);
        log.warn(msg);
        log.error(msg);
        log.debug(msg);
        log.trace(msg);
    }
}
