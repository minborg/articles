package org.minborg.goodies;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

class ArenaDelegator implements Arena {

    private final Arena delegate;

    protected ArenaDelegator(Arena arena) {
        this.delegate = Objects.requireNonNull(arena);
    }

    @Override
    public MemorySegment.Scope scope() {
        return delegate.scope();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        return delegate.allocate(byteSize, byteAlignment);
    }

    protected Arena delegate() {
        return delegate;
    }

}
