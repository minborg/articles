package org.minborg.goodies;

import java.lang.foreign.Arena;

public final class PoolingArena
        extends ArenaDelegator
        implements Arena {

    public PoolingArena(Arena arena) {
        super(arena);
    }

    // Concurrent identity map

    // Clean in a separate thread

}
