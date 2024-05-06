package org.minborg.panamadojo;

public class Fight2_AppReader {

    public static void main(String[] args) {
        Thread.ofPlatform().name("other-vm").start(Fight1_App::consumer);
    }

}
