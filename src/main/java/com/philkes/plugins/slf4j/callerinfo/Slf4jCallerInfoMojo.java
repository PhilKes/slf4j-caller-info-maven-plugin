package com.philkes.plugins.slf4j.callerinfo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

import static com.philkes.plugins.slf4j.callerinfo.AddCallerInfoToLogsAdapter.*;

/**
 * Mojo to scan the code for SLF4J Log statements and inject caller-information
 * into the log via the Mapped Diagnostic Context
 */
@Mojo(name = "inject", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class Slf4jCallerInfoMojo extends AbstractMojo {

    @Parameter(defaultValue = "%class:%line")
    String injection;

    @Parameter(defaultValue = "callerInformation")
    String injectionMdcParameter;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    File target;

    @Parameter(defaultValue = ".*")
    String filterClasses;

    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (injection.isEmpty()) {
            log.warn("'injection' is set to empty string, therefore execution is skipped.");
            return;
        } else {
            String testFormat = injection;
            for (String conversionWord : CONVERSIONS) {
                testFormat = testFormat.replace(conversionWord, "");
            }
            if (testFormat.contains("%")) {
                log.warn("There is a `%` character in the 'injection' parameter," +
                        " without a valid conversion word afterwards, the '%' will be printed in the log statement.");
                log.warn(String.format("Available conversion words: %s, current 'injection': %s", String.join(", ", CONVERSIONS), injection));
            }
        }
        log.info(String.format("Make sure to add the MDC parameter '%s' to your logging pattern, otherwise they wont be printed in your logs", injectionMdcParameter));
        try {
            new CallerInfoLogsClassWriter(target, filterClasses, injectionMdcParameter, injection, log)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
