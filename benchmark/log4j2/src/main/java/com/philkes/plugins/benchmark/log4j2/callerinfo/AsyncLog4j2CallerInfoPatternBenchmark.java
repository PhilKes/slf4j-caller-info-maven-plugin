package com.philkes.plugins.benchmark.log4j2.callerinfo;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.LoggerFactory;

public class AsyncLog4j2CallerInfoPatternBenchmark {
    static final char[] CHARS = new char[500];
    static final String TEST;

    public AsyncLog4j2CallerInfoPatternBenchmark() {
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
            System.setProperty("log4j.configurationFile", "log4j2/callerinfo/perf-log4j2-caller-info-async.xml");
            this.logger = LogManager.getLogger(this.getClass());
        }

        @TearDown(Level.Trial)
        public void down() {
            ((LifeCycle)LogManager.getContext(false)).stop();
            (new File("perftest-pattern.log")).delete();
        }
    }

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2/callerinfo/perf-log4j2-caller-info-async.xml");

        Logger log =  LogManager.getLogger(AsyncLog4j2CallerInfoInjectionBenchmark.class);
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
