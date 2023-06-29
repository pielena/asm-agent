package com.github.pielena;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain");
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) {

                if (className.startsWith("com/github/pielena")) {
                    System.out.println(className);

                    return changeMethod(classfileBuffer);
                }

                return classfileBuffer;
            }
        });
    }

    private static byte[] changeMethod(byte[] originalClass) {
        ClassReader cr = new ClassReader(originalClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        ClassNode classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        for (MethodNode methodNode : classNode.methods) {
            System.out.println(methodNode.name);
            boolean hasAnnotation = false;
            System.out.println("methodNode.invisibleAnnotations: " + methodNode.invisibleAnnotations);
            if (methodNode.invisibleAnnotations != null) {
                for (AnnotationNode annotationNode : methodNode.invisibleAnnotations) {
                    System.out.println(annotationNode.desc);
                    if (annotationNode.desc.equals("Lcom/github/pielena/Log;")) {
                        hasAnnotation = true;
                        System.out.println("Hello, I've found the annotation!");
                        break;
                    }
                }
            }

            if (hasAnnotation) {

            }
        }
        return originalClass;
    }

}
