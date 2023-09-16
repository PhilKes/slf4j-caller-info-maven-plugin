package io.github.philkes.slf4j.callerinfo;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * ClassVisitor searching for {@link Logger} log statements in every method of the class
 * and injects the caller-information with {@link MDC#put(String, String)} calls before every log statement
 */
public class AddCallerInfoToLogsVisitor extends ClassVisitor {
    /**
     * Fully package path of {@link org.slf4j.Logger}
     */
    public static final String SLF4J_LOGGER_FQN = toPath(Logger.class);

    /**
     * Full package path of {@link org.slf4j.MDC}
     */
    public static final String SLF4J_MDC_FQN = toPath(MDC.class);
    /**
     * Method name of {@link org.slf4j.MDC#put(String, String)}
     */
    public static final String SLF4J_MDC_PUT_METHOD_NAME = "put";
    /**
     * Parameters descriptor of {@link org.slf4j.MDC#put(String, String)}
     */
    public static final String SLF4J_MDC_PUT_METHOD_DESCRIPTOR = getMethodDescriptor(MDC.class, SLF4J_MDC_PUT_METHOD_NAME, String.class, String.class);

    /**
     * Method name of {@link org.slf4j.MDC#remove(String)}
     */
    public static final String SLF4J_MDC_REMOVE_METHOD_NAME = "remove";
    /**
     * Parameters descriptor of {@link org.slf4j.MDC#remove(String)}
     */
    public static final String SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR = getMethodDescriptor(MDC.class, SLF4J_MDC_REMOVE_METHOD_NAME, String.class);

    public static final String CONVERSION_CLASS = "%class";
    public static final String CONVERSION_METHOD = "%method";
    public static final String CONVERSION_LINE = "%line";

    public static final Set<String> CONVERSIONS = new HashSet<>(Arrays.asList(CONVERSION_CLASS, CONVERSION_METHOD, CONVERSION_LINE));

    private final String className;
    private final Set<Level> levels;
    private final String injectionMdcParameter;
    private final String injection;
    private final Boolean includePackageName;

    /**
     * Keeping track of how many log statements have been found in the class for logging purposes
     */
    private int logStatementsCounter = 0;

    public AddCallerInfoToLogsVisitor(ClassVisitor cv, String className, Set<Level> levels,
                                      String injectionMdcParameter, String injection, Boolean includePackageName) {
        super(ASM7, cv);
        this.className = className;
        this.levels = levels;
        this.injectionMdcParameter = injectionMdcParameter;
        this.injection = injection;
        this.includePackageName = includePackageName;
        this.cv = cv;
    }

    @Override
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {
        MethodVisitor v = super.visitMethod(access, name, desc, signature, exceptions);
        return new AddCallerInfoToMdcAdapter(v, access, name, desc);
    }

    class AddCallerInfoToMdcAdapter extends GeneratorAdapter {
        /**
         * Keeping track of the last processed {@code LINENUMBER} command in the bytecode
         */
        private Integer currentLineNumber = -1;
        /**
         * Keeping track of the first String parameter passed to the current method
         */
        private String firstMethodStrArg;
        /**
         * Keeping track of the number of String parameters passed to the current method
         */
        private int strArgsCounter = 0;

        AddCallerInfoToMdcAdapter(MethodVisitor delegate, int access, String name, String desc) {
            super(Opcodes.ASM5, delegate, access, name, desc);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            currentLineNumber = line;
            super.visitLineNumber(line, start);
        }

        @Override
        public void visitLdcInsn(Object var1) {
            super.visitLdcInsn(var1);
            if (var1 != null && var1 instanceof String && (strArgsCounter == 0)) {
                firstMethodStrArg = (String) var1;
                strArgsCounter++;
            }
        }

        /**
         * Flag to check if the method call before any Logging calls already contains the {@link MDC#put(String, String)} call
         * with the {@link AddCallerInfoToLogsVisitor#injectionMdcParameter} as the key.
         */
        private boolean isLastMethodCallMDCPut = false;

        /**
         * Searches for {@link Logger} calls to specified log levels ({@link #levels}) and adds
         * the caller-location-information into the MDC context which can be used to output the caller information
         * of the log statement.
         *
         * @param opcode     Code for what type of invocation it is (static, dynamic, etc.)
         * @param owner      Java class name of the method invocation
         * @param name       Name of the method to be invoked
         * @param descriptor Description of the method's parameters
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            boolean isSlf4jLogOnSpecifiedLevel = Objects.equals(owner, SLF4J_LOGGER_FQN) && name.matches(levels.stream()
                    .map(level -> level.toString().toLowerCase())
                    .collect(Collectors.joining("|")));
            if (isSlf4jLogOnSpecifiedLevel && !isLastMethodCallMDCPut) {
                logStatementsCounter++;
                super.visitLdcInsn(injectionMdcParameter);
                super.visitLdcInsn(injection
                        .replace(CONVERSION_CLASS, (includePackageName ? className : withoutPackageName(className)) + ".java")
                        .replace(CONVERSION_METHOD, this.getName())
                        .replace(CONVERSION_LINE, String.valueOf(currentLineNumber))
                );
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_PUT_METHOD_NAME, SLF4J_MDC_PUT_METHOD_DESCRIPTOR, false);
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (isSlf4jLogOnSpecifiedLevel) {
                if (!isLastMethodCallMDCPut) {
                    super.visitLdcInsn(injectionMdcParameter);
                    super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_REMOVE_METHOD_NAME, SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR, false);
                } else {
                    isLastMethodCallMDCPut = false;
                }
            } else {
                isLastMethodCallMDCPut = Objects.equals(owner, SLF4J_MDC_FQN) && name.matches(SLF4J_MDC_PUT_METHOD_NAME)
                        && (injectionMdcParameter.equals(firstMethodStrArg));
            }
            strArgsCounter = 0;
        }
    }

    private static String withoutPackageName(String className) {
        return className.substring(className.lastIndexOf("/") + 1);
    }

    public int getLogStatementsCounter() {
        return logStatementsCounter;
    }

    /**
     * Helper to convert FQCN to path string
     */
    private static String toPath(Class<?> clazz) {
        return clazz.getName().replace(".", "/");
    }

    /**
     * Helper to get method descriptor string
     */
    private static String getMethodDescriptor(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            return Type.getMethodDescriptor(clazz.getMethod(name, paramTypes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}