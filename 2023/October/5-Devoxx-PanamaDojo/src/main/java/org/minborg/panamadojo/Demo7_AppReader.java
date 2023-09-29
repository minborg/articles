package org.minborg.panamadojo;

public class Demo7_AppReader {

    public static void main(String[] args) {
        Thread.ofPlatform().name("other-vm").start(Demo6_App::consumer);
    }

}
