package com.philkes.plugins.slf4j.callerinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.File;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * ClassVisitor searching for {@code org/slf4j/Logger} log statements in every method of the class
 * and injects the caller-information with {@code org/slf4j/MDC#put()} calls before every log statement
 */
public class AddCallerInfoToLogsAdapter extends ClassVisitor {
    /**
     * Fully qualified name of {@code org.slf4j.Logger}
     */
    public static final String SLF4J_LOGGER_FQN = "org/slf4j/Logger";
    /**
     * Fully qualified name of {@code org.slf4j.MDC}
     */
    public static final String SLF4J_MDC_FQN = "org/slf4j/MDC";
    /**
     * Method name of {@code org.slf4j.MDC#put(String, String)}
     */
    public static final String SLF4J_MDC_PUT_METHOD_NAME = "put";
    /**
     * Parameters descriptor of {@code org.slf4j.MDC#put(String, String)}
     */
    public static final String SLF4J_MDC_PUT_METHOD_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/String;)V";
    /**
     * Method name of {@code org.slf4j.MDC#remove(String)}
     */
    public static final String SLF4J_MDC_REMOVE_METHOD_NAME = "remove";
    /**
     * Parameters descriptor of {@code org.slf4j.MDC#remove(String)}
     */
    public static final String SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR = "(Ljava/lang/String;)V";

    public static final String CONVERSION_CLASS = "%class";
    public static final String CONVERSION_METHOD = "%method";
    public static final String CONVERSION_LINE = "%line";

    public static final String[] CONVERSIONS = new String[]{CONVERSION_CLASS, CONVERSION_METHOD, CONVERSION_LINE};

    private final String classFileName;
    private final String injectionMdcParameter;
    private final String injection;


    /**
     * Keeping track of how many log statements have been found in the class for logging purposes
     */
    private int logStatementsCounter = 0;

    public AddCallerInfoToLogsAdapter(ClassVisitor cv, File classFile, String injectionMdcParameter, String injection) {
        super(ASM7, cv);
        this.classFileName = classFile.getName();
        this.injectionMdcParameter = injectionMdcParameter;
        this.injection = injection;
        this.cv = cv;
    }


    @Override
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {
        MethodVisitor v = super.visitMethod(access, name, desc, signature, exceptions);
        return new AddCallerInfoToMDCTransformer(v, access, name, desc);
    }

    class AddCallerInfoToMDCTransformer extends GeneratorAdapter {
        /**
         * Keeping track of the last processed {@code LINENUMBER} command in the bytecode
         */
        private Integer currentLineNumber = -1;

        AddCallerInfoToMDCTransformer(MethodVisitor delegate, int access, String name, String desc) {
            super(Opcodes.ASM5, delegate, access, name, desc);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            currentLineNumber = line;
            super.visitLineNumber(line, start);
        }

        /**
         * Searches for {@code org/slf4j/Logger} calls to {@code info(),warn(),error(),debug(),trace()} and adds
         * the current method + line number into the MDC context which can be used to output the caller information
         * of the log statement.
         *
         * @param opcode     Code for what type of invocation it is (static, dynamic, etc.)
         * @param owner      Java class name of the method invocation
         * @param name       Name of the method to be invoked
         * @param descriptor Description of the method's parameters
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            boolean isSlf4jLogStatement = Objects.equals(owner, SLF4J_LOGGER_FQN) && name.matches("info|warn|error|debug|trace");
            if (isSlf4jLogStatement) {
                logStatementsCounter++;
                super.visitLdcInsn(injectionMdcParameter);
                super.visitLdcInsn(injection
                        .replace(CONVERSION_CLASS, classFileName)
                        .replace(CONVERSION_METHOD, this.getName())
                        .replace(CONVERSION_LINE, String.valueOf(currentLineNumber))
                );
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_PUT_METHOD_NAME, SLF4J_MDC_PUT_METHOD_DESCRIPTOR, false);
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (isSlf4jLogStatement) {
                super.visitLdcInsn(injectionMdcParameter);
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_REMOVE_METHOD_NAME, SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR, false);

            }
        }
    }

    public int getLogStatementsCounter() {
        return logStatementsCounter;
    }
}