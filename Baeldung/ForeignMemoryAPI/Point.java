import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public final class Point {

    private static final MemoryLayout POINT = structLayout(
            JAVA_INT.withName("x"),
            JAVA_INT.withName("y")
    ).withName("point");

    private static final VarHandle X = POINT.varHandle(groupElement("x"));
    private static final VarHandle Y = POINT.varHandle(groupElement("y"));

    private final MemorySegment segment;

    public Point(Arena arena) {
        this.segment = arena.allocate(POINT);
    }

    public Point(MemorySegment segment) {
        this.segment = Objects.requireNonNull(segment);
    }


    int x() {
        return (int) X.get(segment);
    }

    int y() {
        return (int) Y.get(segment);
    }

    void x(int x) {
        X.set(segment, x);
    }

    void y(int y) {
        Y.set(segment, y);
    }

    @Override
    public String toString() {
        return "Point[" + x() + ", " + y() + "]";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Point that &&
                this.x() == that.x() &&
                this.x() == that.y();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x(), y());
    }
}
