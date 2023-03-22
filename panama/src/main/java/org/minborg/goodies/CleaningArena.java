package org.minborg.goodies;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class CleaningArena
        extends ArenaDelegator
        implements Arena {

    private final byte cleanValue;

    CleaningArena(Arena arena,
                          byte cleanValue) {
        super(arena);
        this.cleanValue = cleanValue;
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        return cleaningSegment(super.allocate(byteSize, byteAlignment));
    }

    private MemorySegment cleaningSegment(MemorySegment segment) {
        return segment.reinterpret(delegate(), this::cleanup);
    }

    private void cleanup(MemorySegment segment) {
        //System.out.println("Setting " + segment + " to " + cleanValue);
        segment.fill(cleanValue);
    }

    public static Arena of(Arena arena) {
        return of(arena, (byte) 0);
    }

    public static Arena of(Arena arena, byte cleanValue) {
        return new CleaningArena(arena, cleanValue);
    }

}
