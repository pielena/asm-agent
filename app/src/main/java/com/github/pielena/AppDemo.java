package com.github.pielena;

import java.util.List;

public class AppDemo {

    //    -javaagent:"C:\Projects\PetProjects\asm-agent\agent\target\agent.jar"
    public static void main(String[] args) {

        int[] array = new int[]{1, 2, 3};
        List<String> list = List.of("One", "Two", "Three");

        System.out.println("Main method is here");
        AppClass appClass = new AppClass();
        appClass.doSomething(12);
        appClass.doSomething1(12, "Hello", array);
        AppClass.doSomething2(list, "Hello", array);
        appClass.doSomething3(12, "Hello");
        AppClassWithoutAnnotation classWithoutAnnotation = new AppClassWithoutAnnotation();
        classWithoutAnnotation.doSomething4("Hello", 1.0);
    }
}
