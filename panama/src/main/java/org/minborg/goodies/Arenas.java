package org.minborg.goodies;

import java.lang.foreign.Arena;
import java.util.Objects;

public final class Arenas {

    private Arenas() {}

    public static Arena ofCleaning(Arena arena) {
        return ofCleaning(arena, (byte) 0);
    }

    public static Arena ofCleaning(Arena arena, byte cleanValue) {
        Objects.requireNonNull(arena);
        return new CleaningArena(arena, cleanValue);
    }

}
