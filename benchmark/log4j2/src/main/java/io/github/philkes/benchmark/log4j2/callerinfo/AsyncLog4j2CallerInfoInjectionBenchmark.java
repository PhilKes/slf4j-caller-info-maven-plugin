/*
package io.github.philkes.benchmark.log4j2.callerinfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LifeCycle;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class AsyncLog4j2CallerInfoInjectionBenchmark {
    static final char[] CHARS = new char[500];
    static final String TEST;

    public AsyncLog4j2CallerInfoInjectionBenchmark() {
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public boolean throughputBaseline(NormalState e) {
        return e.logger.isInfoEnabled();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void throughput(NormalState e) {
        e.logger.info(TEST);
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public boolean latencyBaseline(NormalState e) {
        return e.logger.isInfoEnabled();
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void latency(NormalState e) {
        e.logger.info(TEST);
    }

    static {
        Arrays.fill(CHARS, 'a');
        TEST = new String(CHARS);
    }

    @State(Scope.Benchmark)
    public static class NormalState {
        Logger logger;

        public NormalState() {
        }

        @Setup(Level.Trial)
        public void up() {
            System.setProperty("log4j.configurationFile", "log4j2/callerinfo/perf-log4j2-caller-info-injection-async.xml");
            this.logger = LoggerFactory.getLogger(this.getClass());
        }

        @TearDown(Level.Trial)
        public void down() {
            ((LifeCycle) LogManager.getContext(false)).stop();
            (new File("perftest-injection.log")).delete();
        }
    }

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2/callerinfo/perf-log4j2-caller-info-injection-async.xml");
        org.slf4j.Logger log = LoggerFactory.getLogger(AsyncLog4j2CallerInfoPatternBenchmark.class);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            log.info("Test log ");
            log.info("Test log ");
            log.info("Test log ");
            log.info("Test log ");
            log.info("Test log ");
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        log.info("Elapsed: {}", timeElapsed);

    }
}
*/
