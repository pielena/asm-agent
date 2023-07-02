package com.github.pielena;

import java.util.List;

public class AppClass {

    @Log
    public void doSomething(int param) {
        System.out.println("Method doSomething is here");
    }

    @Log
    public void doSomething1(int param1, String param2, int[] param3) {
        System.out.println("Method doSomething1 is here");
    }

    @Log
    public static void doSomething2(List<String> param1, String param2, int[] param3) {
        System.out.println("Method doSomething2 is here");
    }

    public void doSomething3(int param1, String param2) {
        System.out.println("Method doSomething3 is here");
    }
}
