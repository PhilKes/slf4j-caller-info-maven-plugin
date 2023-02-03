package com.philkes.plugins.slf4j.callerinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * ClassVisitor searching for {@code org/slf4j/Logger} log statements in every method of the class
 *  and injects the caller-information with {@code org/slf4j/MDC#put()} calls before every log statement
 */
public class AddCallerInfoToLogsAdapter extends ClassVisitor {
    /** Fully qualified name of {@code org.slf4j.Logger} */
    public static final String SLF4J_LOGGER_FQN = "org/slf4j/Logger";
    /** Fully qualified name of {@code org.slf4j.MDC} */
    public static final String SLF4J_MDC_FQN = "org/slf4j/MDC";
    /** Method name of {@code org.slf4j.MDC#put(String, String)} */
    public static final String SLF4J_MDC_PUT_METHOD_NAME = "put";
    /** Parameters descriptor of {@code org.slf4j.MDC#put(String, String)} */
    public static final String SLF4J_MDC_PUT_METHOD_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/String;)V";
    /** Method name of {@code org.slf4j.MDC#remove(String)} */
    public static final String SLF4J_MDC_REMOVE_METHOD_NAME = "remove";
    /** Parameters descriptor of {@code org.slf4j.MDC#remove(String)} */
    public static final String SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR = "(Ljava/lang/String;)V";

    private final String methodMdcParameter;
    private final String lineMdcParameter;

    private final boolean injectMethod;
    private final boolean injectLineNumber;

    /**
     * Keeping track of how many log statements have been found in the class for logging purposes
     */
    private int logStatementsCounter = 0;

    public AddCallerInfoToLogsAdapter(ClassVisitor cv, String methodMdcParameter, String lineMdcParameter, boolean injectMethod, boolean injectLineNumber) {
        super(ASM7, cv);
        this.methodMdcParameter = methodMdcParameter;
        this.lineMdcParameter = lineMdcParameter;
        this.injectMethod = injectMethod;
        this.injectLineNumber = injectLineNumber;
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
            }
            if (injectMethod && isSlf4jLogStatement) {
                super.visitLdcInsn(methodMdcParameter);
                super.visitLdcInsn(this.getName());
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_PUT_METHOD_NAME, SLF4J_MDC_PUT_METHOD_DESCRIPTOR, false);
                super.visitLdcInsn(lineMdcParameter);
                super.visitLdcInsn(currentLineNumber.toString());
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_PUT_METHOD_NAME, SLF4J_MDC_PUT_METHOD_DESCRIPTOR, false);
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (injectLineNumber && isSlf4jLogStatement) {
                super.visitLdcInsn(methodMdcParameter);
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_REMOVE_METHOD_NAME, SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR, false);
                super.visitLdcInsn(lineMdcParameter);
                super.visitMethodInsn(INVOKESTATIC, SLF4J_MDC_FQN, SLF4J_MDC_REMOVE_METHOD_NAME, SLF4J_MDC_REMOVE_METHOD_DESCRIPTOR, false);
            }
        }
    }

    public int getLogStatementsCounter() {
        return logStatementsCounter;
    }
}