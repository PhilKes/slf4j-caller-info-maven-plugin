package io.github.philkes.slf4j.callerinfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilizing ASM {@link ClassReader} and {@link ClassWriter} to modify the compiled Java classes
 * to inject the caller-information to all SLF4J log invocations
 */
public class CallerInfoLogsClassWriter {

    private final File targetClassDir;
    private final ClassFilters filters;

    private final String injectionMdcParameter;
    private final String injection;
    private final Boolean includePackageName;
    private final List<String> injectedMethods;

    private final Log log;

    public CallerInfoLogsClassWriter(File target, ClassFilters filters, String injectionMdcParameter,
                                     String injection, Boolean includePackageName, List<String> injectedMethods, Log log) throws IOException {
        this.targetClassDir = target;
        this.filters = filters;
        this.injectionMdcParameter = injectionMdcParameter;
        this.injection = injection;
        this.includePackageName = includePackageName;
        this.injectedMethods = injectedMethods;
        this.log = log;
    }

    /**
     * Uses {@link AddCallerInfoToLogsVisitor} to inject caller-information via MDC to given class
     *
     * @param classFile .class File in target directory
     * @throws IOException if {@code classFile} is not a valid .class File
     */
    private byte[] addCallerInfoToLogs(File classFile) throws IOException {
        log.debug(String.format("Searching for log statements in %s", classFile.toPath()));
        ClassReader reader = new ClassReader(new FileInputStream((classFile)));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        AddCallerInfoToLogsVisitor callerInfoLogAdapter = new AddCallerInfoToLogsVisitor(writer, reader.getClassName(),
                injectionMdcParameter, injection, includePackageName, injectedMethods);
        reader.accept(callerInfoLogAdapter, ClassReader.EXPAND_FRAMES);
        int logStatementsFound = callerInfoLogAdapter.getLogStatementsCounter();
        if (logStatementsFound > 0) {
            log.info(String.format("%s - %d SLF4J log statements found", classFile.toPath(), logStatementsFound));
        }
        return writer.toByteArray();
    }

    /**
     * Loops through {@link #targetClassDir} directory recursively with the given {@link #filters} filter and
     * writes the injected class files back to the target directory
     *
     * @throws IOException if {@link #targetClassDir} contains invalid {@code .class} files
     */
    public void execute() throws IOException {
        log.info(String.format("Searching for %s usages in all .class files in %s with filterClasses='%s', injection='%s', includePackageName='%s'",
                injectedMethods.stream().collect(Collectors.joining(",", "[", "]")),
                targetClassDir.toPath(), filters, injection, includePackageName));
        if (!targetClassDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Path %s is not a valid target/classes directory!", targetClassDir.toPath()));
        }
        Collection<File> files = FileUtils.listFiles(targetClassDir, new ClassFileFilter(filters), DirectoryFileFilter.DIRECTORY);
        for (File classFile : files) {
            Files.write(classFile.toPath(), addCallerInfoToLogs(classFile), StandardOpenOption.WRITE);
        }
    }

}