package com.philkes.plugins.slf4j.callerinfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import static com.philkes.plugins.slf4j.callerinfo.AddCallerInfoToLogsAdapter.SLF4J_LOGGER_FQN;

/**
 * Utilizing ASM {@link ClassReader} and {@link ClassWriter} to modify the compiled Java classes
 * to inject the caller-information to all SLF4J log invocations
 */
public class CallerInfoLogsClassWriter {

    private final File targetClassDir;
    private final String filterClasses;

    private final String methodMdcParameter;
    private final String lineMdcParameter;

    private final boolean injectMethod;
    private final boolean injectLineNumber;
    private final Log log;

    public CallerInfoLogsClassWriter(File target, String filterClasses, String methodMdcParameter, String lineMdcParameter, boolean injectMethod, boolean injectLineNumber, Log log) throws IOException {
        this.targetClassDir = target;
        this.filterClasses = filterClasses;
        this.injectMethod = injectMethod;
        this.injectLineNumber = injectLineNumber;
        this.methodMdcParameter = methodMdcParameter;
        this.lineMdcParameter = lineMdcParameter;
        this.log = log;
    }

    /**
     * Uses {@link AddCallerInfoToLogsAdapter} to inject caller-information via MDC to given class
     * @param classFile .class File in target directory
     * @throws IOException if {@code classFile} is not a valid .class File
     */
    private byte[] addCallerInfoToLogs(File classFile) throws IOException {
        log.debug(String.format("Searching for log statements in %s", classFile.toPath()));
        ClassReader reader = new ClassReader(new FileInputStream((classFile)));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        AddCallerInfoToLogsAdapter callerInfoLogAdapter = new AddCallerInfoToLogsAdapter(writer, methodMdcParameter, lineMdcParameter,
                injectMethod, injectLineNumber);
        reader.accept(callerInfoLogAdapter, ClassReader.EXPAND_FRAMES);
        int logStatementsFound = callerInfoLogAdapter.getLogStatementsCounter();
        if (logStatementsFound > 0) {
            log.info(String.format("%s - %d SLF4J log statements found", classFile.toPath(), logStatementsFound));
        }
        return writer.toByteArray();
    }

    /**
     * Loops through {@link #targetClassDir} directory recursively with the given {@link #filterClasses} filter and
     *  writes the injected class files back to the target directory
     * @throws IOException if {@link #targetClassDir} contains invalid {@code .class} files
     */
    public void execute() throws IOException {
        log.info(String.format("Searching for %s usages in all .class files in %s with filterClasses='%s'", SLF4J_LOGGER_FQN, targetClassDir.toPath(), filterClasses));
        if (!targetClassDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Path %s is not a valid target/classes directory!", targetClassDir.toPath()));
        }
        Collection<File> files = FileUtils.listFiles(targetClassDir,
                new AbstractFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String format = String.format(".*%s\\.class", filterClasses);
                        return file.toString().matches(format);
                    }
                }, DirectoryFileFilter.DIRECTORY);
        for (File classFile : files) {
            Files.write(classFile.toPath(), addCallerInfoToLogs(classFile), StandardOpenOption.WRITE);
        }
    }

}