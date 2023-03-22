package org.minborg.goodies;

import java.lang.foreign.Arena;

public class Demo {

    public static void main(String[] args) {
        try (var arena = CleaningArena.of(Arena.ofConfined())) {
            var seg = arena.allocate(16);
        }
    }

}
