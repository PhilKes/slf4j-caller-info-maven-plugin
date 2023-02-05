/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.github.philkes.benchmark.logback.callerinfo;

import org.openjdk.jmh.annotations.Benchmark;


import ch.qos.logback.core.spi.LifeCycle;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncLogbackCallerInfoPatternBenchmark {
    static final char[] CHARS = new char[500];
    static final String TEST;

    public AsyncLogbackCallerInfoPatternBenchmark() {
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
            System.setProperty("logback.configurationFile", "logback/callerinfo/perf-logback-caller-info-async.xml");
            this.logger = LoggerFactory.getLogger(this.getClass());
        }

        @TearDown(Level.Trial)
        public void down() {
            ((LifeCycle) LoggerFactory.getILoggerFactory()).stop();
            (new File("perftest-pattern.log")).delete();
        }
    }
    public static void main(String[] args) {
        System.setProperty("logback.configurationFile", "logback/callerinfo/perf-logback-caller-info-async.xml");
        org.slf4j.Logger log = LoggerFactory.getLogger(AsyncLogbackCallerInfoInjectionBenchmark.class);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
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
