package com.github.pielena;

import java.util.ArrayList;

public class AppDemo {

//    -javaagent:"C:\Projects\PetProjects\asm-agent\agent\target\agent.jar"
    public static void main(String[] args) {
        System.out.println("Main method here");
        AppClass appClass = new AppClass();
        appClass.doSomething(12);
        appClass.doSomething1(12, "Hello", new int[0]);
        appClass.doSomething2(new ArrayList<>(), "Hello", new int[0]);
        appClass.doSomething3(12, "Hello");
        AppClassWithoutAnnotation classWithoutAnnotation = new AppClassWithoutAnnotation();
        classWithoutAnnotation.doSomething4("Hello", 1.0);
    }
}
