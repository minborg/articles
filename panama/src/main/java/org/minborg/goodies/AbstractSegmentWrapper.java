package org.minborg.goodies;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractSegmentWrapper {

    private final MemorySegment segment;

    public AbstractSegmentWrapper(Arena arena) {
        this.segment = arena.allocate(layout());
    }

    abstract protected StructLayout layout();

    abstract protected List<VarHandle> varHandles();

    protected MemorySegment segment() {
        return segment;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + layout().memberLayouts()
                .stream()
                .map(l -> l.name().orElse(l.toString()) + "=" + l.varHandle().get(segment))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        AbstractSegmentWrapper that = (AbstractSegmentWrapper) o;
        return varHandles().stream()
                .allMatch(vh -> Objects.equals(vh.get(this.segment), vh.get(that.segment)));
    }

    @Override
    public int hashCode() {
        return varHandles().stream()
                .map(vh -> vh.get(segment))
                .mapToInt(v -> v == null ? 0 : v.hashCode())
                .reduce(1, (a, b) -> a * 31 + b);
    }

}
