package io.github.philkes.benchmark.compiletime.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class CallerInfoInjectionLog {
    static final char[] CHARS = new char[500];
    static final String TEST;
    static {
        Arrays.fill(CHARS, 'a');
        TEST = new String(CHARS);
    }
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void log(){
        logger.info(TEST);
        logger.warn(TEST);
        logger.error(TEST);
        logger.debug(TEST);
        logger.trace(TEST);
    }
}
