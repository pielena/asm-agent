package com.github.pielena;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain");
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) {

                return changeMethod(classfileBuffer);
            }
        });
    }

    private static byte[] changeMethod(byte[] originalClass) {

        ClassReader cr = new ClassReader(originalClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        ClassNode classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean hasAnnotation = false;
        Map<String, String> annotatedMethodsMap = new HashMap<>();

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.invisibleAnnotations != null) {
                for (AnnotationNode annotationNode : methodNode.invisibleAnnotations) {
                    if (annotationNode.desc.equals("Lcom/github/pielena/Log;")) {
                        hasAnnotation = true;
                        System.out.println("Hello, I've found the annotation in method: " + methodNode.name);
                        //this map can't contain overload methods
                        annotatedMethodsMap.put(methodNode.name, methodNode.desc);
                        break;
                    }
                }
            }
        }

        if (!hasAnnotation) {
            return originalClass;
        }

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (annotatedMethodsMap.containsKey(name) && annotatedMethodsMap.get(name).equals(descriptor)) {
                    System.out.println("We here: " + name);
                    return new ChangeMethodVisitor(methodVisitor, access, name, descriptor);
                } else {
                    return methodVisitor;
                }
            }
        };
        cr.accept(cv, Opcodes.ASM9);

        return cw.toByteArray();
    }

    private static class ChangeMethodVisitor extends AdviceAdapter {

        ChangeMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(Opcodes.ASM9, methodVisitor, access, name, descriptor);
        }

        @Override
        protected void onMethodEnter() {

            System.out.println("So, what to do?...");

        }

        @Override
        public void visitLocalVariable(
                final String name,
                final String descriptor,
                final String signature,
                final Label start,
                final Label end,
                final int index) {
            System.out.println("visited name: " + name +
                    ", descriptor: " + descriptor +
                    ", signature: " + signature +
                    ", index: " + index
            );
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }
    }

//    static class LogInjector extends MethodVisitor {
//        static final String PS_T = "java/io/PrintStream", PS_S = "L" + PS_T + ";";
//        static final String PRINTF_DESC = "(Ljava/lang/String;[Ljava/lang/Object;)" + PS_S;
//        static final String MH_T = "java/lang/invoke/MethodHandle", MH_S = "L" + MH_T + ";";
//
//        private int firstUnusedVar;
//
//        public LogInjector(MethodVisitor mv, int acc, String desc) {
//            super(Opcodes.ASM9, mv);
//            int vars = Type.getArgumentsAndReturnSizes(desc) >> 2;
//            if ((acc & Opcodes.ACC_STATIC) != 0) vars--;
//            firstUnusedVar = vars;
//        }
//
//        @Override
//        public void visitFrame(int type,
//                               int numLocal, Object[] local, int numStack, Object[] stack) {
//            super.visitFrame(type, numLocal, local, numStack, stack);
//            firstUnusedVar = Math.max(firstUnusedVar, numLocal);
//        }
//
//        @Override
//        public void visitVarInsn(int opcode, int var) {
//            super.visitVarInsn(opcode, var);
//            if (opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE) var++;
//            if (var >= firstUnusedVar) firstUnusedVar = var + 1;
//        }
//
//        @Override
//        public void visitMethodInsn(int opcode,
//                                    String owner, String name, String descriptor, boolean isInterface) {
//
//
//            Type[] arg = Type.getArgumentTypes(descriptor);
//
//            int[] vars = storeArguments(arg, opcode, name, owner);
//
//            String reportDesc = getReportDescriptor(owner, descriptor, arg, vars);
//
//
//            mv.visitLdcInsn(new Handle(Opcodes.H_INVOKEVIRTUAL,
//                    PS_T, "printf", PRINTF_DESC, false));
//            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", PS_S);
//            bindTo();
//            mv.visitLdcInsn(messageFormat(opcode, owner, name, arg));
//            bindTo();
//            mv.visitLdcInsn(Type.getObjectType("[Ljava/lang/Object;"));
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH_T,
//                    "asVarargsCollector", "(Ljava/lang/Class;)" + MH_S, false);
//            mv.visitLdcInsn(Type.getMethodType(reportDesc));
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH_T,
//                    "asType", "(Ljava/lang/invoke/MethodType;)" + MH_S, false);
//            pushArguments(arg, vars);
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                    MH_T, "invokeExact", reportDesc, false);
//            mv.visitInsn(Opcodes.POP);
//
//            pushArguments(arg, vars);
//            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//        }
//
//        String getReportDescriptor(
//                String owner, String descriptor, Type[] arg, int[] vars) {
//            StringBuilder sb = new StringBuilder(owner.length() + descriptor.length() + 2);
//            sb.append('(');
//            if (arg.length != vars.length) {
//                if (owner.charAt(0) == '[') sb.append(owner);
//                else sb.append('L').append(owner).append(';');
//            }
//            sb.append(descriptor, 1, descriptor.lastIndexOf(')') + 1);
//            return sb.append(PS_S).toString();
//        }
//
//        int[] storeArguments(Type[] arg, int opcode, String name, String owner) {
//            int nArg = arg.length;
//            boolean withThis = opcode != Opcodes.INVOKESTATIC && !name.equals("<init>");
//            if (withThis) nArg++;
//            int[] vars = new int[nArg];
//            int slot = firstUnusedVar;
//            for (int varIx = nArg - 1, argIx = arg.length - 1; argIx >= 0; varIx--, argIx--) {
//                Type t = arg[argIx];
//                mv.visitVarInsn(t.getOpcode(Opcodes.ISTORE), vars[varIx] = slot);
//                slot += t.getSize();
//            }
//            if (withThis)
//                mv.visitVarInsn(Opcodes.ASTORE, vars[0] = slot);
//            return vars;
//        }
//
//        private void bindTo() {
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH_T,
//                    "bindTo", "(Ljava/lang/Object;)" + MH_S, false);
//        }
//
//        private void pushArguments(Type[] arg, int[] vars) {
//            int vIx = 0;
//            if (arg.length != vars.length)
//                mv.visitVarInsn(Opcodes.ALOAD, vars[vIx++]);
//            for (Type t : arg)
//                mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), vars[vIx++]);
//        }
//
//        private String messageFormat(int opcode, String owner, String name, Type[] arg) {
//            StringBuilder sb = new StringBuilder();
//            switch (opcode) {
//                case Opcodes.INVOKESPECIAL:
//                    if (name.equals("<init>")) {
//                        name = Type.getObjectType(owner).getClassName();
//                        break;
//                    }
//                    // else no break
//                case Opcodes.INVOKEINTERFACE: // no break
//                case Opcodes.INVOKEVIRTUAL:
//                    sb.append("[%s].");
//                    break;
//                case Opcodes.INVOKESTATIC:
//                    sb.append(Type.getObjectType(owner).getClassName()).append('.');
//                    break;
//            }
//            sb.append(name);
//            if (arg.length == 0) sb.append("()%n");
//            else {
//                sb.append('(');
//                for (int i = arg.length; i > 1; i--) sb.append("%s, ");
//                sb.append("%s)%n");
//            }
//            return sb.toString();
//        }
//    }

}
