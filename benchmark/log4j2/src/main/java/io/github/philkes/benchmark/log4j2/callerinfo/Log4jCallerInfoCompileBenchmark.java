package io.github.philkes.benchmark.log4j2.callerinfo;

import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Log4jCallerInfoCompileBenchmark {
    public static final String SLF_4_J_CALLER_INFO_INJECT = "slf4j-caller-info:inject";
    public static final String TEST_LOG_TEMPLATE_CLASS = "io/github/philkes/benchmark/log4j2/template/Log.java";
    public static final String TEST_LOG_CLASS_PATTERN = "io/github/philkes/benchmark/log4j2/test/Log%d.java";
    public static final String MAIN_JAVA_PACKAGE_PATH = "./log4j2-compiletime/src/main/java/";
    private static final Runtime rt = Runtime.getRuntime();

    public static void copyClass(File sourceClassFile, File destinationClassFile, int idx) {
        try {
            FileUtils.copyFile(sourceClassFile, destinationClassFile);
            String str = FileUtils.readFileToString(destinationClassFile, StandardCharsets.UTF_8);
            str = str.replace("class Log", "class Log" + idx);
            str = str.replace("log4j2.template", "log4j2.test");
            FileUtils.writeStringToFile(destinationClassFile, str, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteClass(File classFile) {
        classFile.delete();
    }

    public static int executeCli(String command) {
        try {
            Process pr = rt.exec(command);
            synchronized (pr) {
                return pr.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static int executeCliWithLog(String command) {
        try {
            Process pr = rt.exec(command);
            String line;
            InputStreamReader isr = new InputStreamReader(pr.getInputStream());
            BufferedReader rdr = new BufferedReader(isr);
            while ((line = rdr.readLine()) != null) {
                System.out.println(line);
            }

            isr = new InputStreamReader(pr.getErrorStream());
            rdr = new BufferedReader(isr);
            while ((line = rdr.readLine()) != null) {
                System.err.println(line);
            }
            synchronized (pr) {
                return pr.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Log4jCallerInfoCompileBenchmark() {
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void average1Class(State1Class e) {
        executeCli(String.format("mvn -f %s/pom.xml %s", e.projectPath, SLF_4_J_CALLER_INFO_INJECT));
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void average10Class(State10Class e) {
        executeCli(String.format("mvn -f %s/pom.xml %s", e.projectPath, SLF_4_J_CALLER_INFO_INJECT));
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void average1000Class(State1000Class e) {
        executeCli(String.format("mvn -f %s/pom.xml %s", e.projectPath, SLF_4_J_CALLER_INFO_INJECT));
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void average10000Class(State10000Class e) {
        executeCli(String.format("mvn -f %s/pom.xml %s", e.projectPath, SLF_4_J_CALLER_INFO_INJECT));
    }


    public static void setup(int nClasses, String projectPath) {
        for (int i = 0; i < nClasses; i++) {
            File destClassFile = new File(MAIN_JAVA_PACKAGE_PATH + String.format(TEST_LOG_CLASS_PATTERN, i));
            copyClass(new File(MAIN_JAVA_PACKAGE_PATH + TEST_LOG_TEMPLATE_CLASS), destClassFile, i);
        }
        int result = executeCli(String.format("mvn -f %s/pom.xml clean compile", projectPath));
        if (result != 0) {
            throw new RuntimeException("Setup 'mvn clean compile' failed!");
        }
    }

    public static void tearDown(int nClasses) {
        for (int i = 0; i < nClasses; i++) {
            deleteClass(new File(String.format(MAIN_JAVA_PACKAGE_PATH + TEST_LOG_CLASS_PATTERN, i)));
        }
    }

    @State(Scope.Benchmark)
    public static class StateNClass {

        protected final String projectPath;
        protected final int nClasses;

        public StateNClass(int nClasses) {
            this.nClasses = nClasses;
            try {
                projectPath = new File("./log4j2-compiletime").getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        @Setup(Level.Invocation)
        public void up() {
            setup(nClasses,projectPath);
        }

        @TearDown(Level.Invocation)
        public void down() {
            tearDown(nClasses);
        }

    }

    public static class State1Class extends StateNClass {

        public State1Class() {
            super(1);
        }
    }

    public static class State10Class extends StateNClass {

        public State10Class() {
            super(10);
        }
    }

    public static class State1000Class extends StateNClass {

        public State1000Class() {
            super(1000);
        }
    }

    public static class State10000Class extends StateNClass {

        public State10000Class() {
            super(10000);
        }
    }


    public static void main(String[] args) throws IOException {
        int nClasses = 100;
        String projectPath = new File("./log4j2-compiletime").getCanonicalPath();
        setup(nClasses, projectPath);
        for (int i = 0; i < nClasses; i++) {
            File destClassFile = new File(String.format("./log4j2-compiletime/src/main/java/%s", String.format(TEST_LOG_CLASS_PATTERN, i)));
            copyClass(new File(String.format("./log4j2-compiletime/src/main/java/%s", TEST_LOG_TEMPLATE_CLASS)), destClassFile, i);
        }

        executeCliWithLog(String.format("mvn -f %s/pom.xml %s", projectPath, SLF_4_J_CALLER_INFO_INJECT));
        for (int i = 0; i < nClasses; i++) {
            deleteClass(new File(String.format(MAIN_JAVA_PACKAGE_PATH + TEST_LOG_CLASS_PATTERN, i)));
        }
        tearDown(nClasses);
    }
}
