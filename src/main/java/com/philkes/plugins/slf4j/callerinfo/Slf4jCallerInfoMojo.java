package com.philkes.plugins.slf4j.callerinfo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mojo to scan the code for SLF4J Log statements and inject caller-information (Method name, Line number)
 * into the log via the Mapped Diagnostic Context
 */
@Mojo(name = "inject", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class Slf4jCallerInfoMojo extends AbstractMojo {

    @Parameter(defaultValue = "true")
    Boolean injectMethod;

    @Parameter(defaultValue = "true")
    Boolean injectLineNumber;

    @Parameter(defaultValue = "callerMethod")
    String methodMdcParameter;

    @Parameter(defaultValue = "callerLine")
    String lineMdcParameter;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    File target;

    @Parameter(defaultValue = ".*")
    String filterClasses;

    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (!injectMethod && !injectLineNumber) {
            log.warn("Both 'injectMethod' and 'injectLineNumber' is set to 'false', therefore execution is skipped.");
            return;
        }
        List<String> mandatoryLogPatternParameters = new ArrayList<>();
        if (injectMethod) {
            mandatoryLogPatternParameters.add(methodMdcParameter);
        }
        if (injectLineNumber) {
            mandatoryLogPatternParameters.add(lineMdcParameter);
        }
        log.info(String.format("Make sure to add the MDC parameters %s to your logging pattern, otherwise they wont be printed in your logs",
                mandatoryLogPatternParameters.stream()
                        .collect(Collectors.joining("\", \"", "\"", "\""))));

        try {
            new CallerInfoLogsClassWriter(target, filterClasses, methodMdcParameter, lineMdcParameter, injectMethod, injectLineNumber, log)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
