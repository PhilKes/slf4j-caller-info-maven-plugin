package io.github.philkes.slf4j.callerinfo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Mojo to scan the code for SLF4J Log statements and inject caller-information
 * into the log via the Mapped Diagnostic Context
 */
@Mojo(name = "inject", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class Slf4jCallerInfoMojo extends AbstractMojo {

    /**
     * Injected pattern, can include any conversion words ('%class','%line','%method')
     */
    @Parameter(defaultValue = "%class:%line")
    String injection;

    /**
     * Name of the used MDC parameter in the logging-pattern
     */
    @Parameter(defaultValue = "callerInformation")
    String injectionMdcParameter;

    /**
     * Whether or not to print the package-name of the class, if '%class' is present in 'injection' parameter
     */
    @Parameter(defaultValue = "false")
    boolean includePackageName;

    /**
     * Target directory which contains the compiled '.class' files, defaults to project class target dir ('target/classes')
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    File target;

    /**
     * Regex filters to only inject into specific Java classes
     */
    @Parameter
    ClassFilters filters = ClassFilters.DEFAULT_FILTERS;

    /**
     * Set of SLF4J log Levels to which should be injected to
     */
    @Parameter
    Set<Level> levels = new HashSet<>(Arrays.asList(Level.values()));


    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (injection.isEmpty()) {
            log.warn("'injection' is set to empty string, therefore execution is skipped.");
            return;
        } else {
            String testFormat = injection;
            for (String conversionWord : AddCallerInfoToLogsVisitor.CONVERSIONS) {
                testFormat = testFormat.replace(conversionWord, "");
            }
            if (testFormat.contains("%")) {
                log.warn("There is a `%` character in the 'injection' parameter," +
                        " without a valid conversion word afterwards, the '%' will be printed in the log statement.");
                log.warn(String.format("Available conversion words: %s, current 'injection': %s", String.join(", ", AddCallerInfoToLogsVisitor.CONVERSIONS), injection));
            }
        }
        log.info(String.format("Make sure to add the MDC parameter '%s' to your logging pattern, otherwise they wont be printed in your logs", injectionMdcParameter));
        try {
            new CallerInfoLogsClassWriter(target, filters, levels, injectionMdcParameter, injection, includePackageName, log)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
