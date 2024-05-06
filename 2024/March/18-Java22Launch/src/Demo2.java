package src;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement;

public class Demo2 {

    // Structured MemorySegment access with MemoryLayout and VarHandles

    public interface Point2D {

        //  struct Point2D {
        //     double x;
        //     double y;
        // }
        MemoryLayout LAYOUT = MemoryLayout.structLayout(
                ValueLayout.JAVA_DOUBLE.withName("x"),
                ValueLayout.JAVA_DOUBLE.withName("y")
        );

        MemorySegment segment(); // "Pointer" to a backing segment
        long offset();           // Offset into the backing segment

        // Accessors
        double x();
        double y();
        void x(double x);
        void y(double y);
    }

    // Structured Access
    public static void main(String[] args) {

        // - No option for alignment checks
        // - No atomic operations (e.g. volatile or CAS operations)
        // - No support for structured access:

        //     # Original C struct
        //
        //     struct Point2D {
        //         double x;
        //         double y;
        //     } point = { 3.0, 4.0 }
        //

        // Instead of this ...
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment point = arena.allocate(8 * 2);
            point.set(ValueLayout.JAVA_DOUBLE, 0, 3.0d);
            point.set(ValueLayout.JAVA_DOUBLE, 8, 4.0d);
        }


        // We encapsulate a segment/offset pair into a regula Java class
        final class Point2DImpl implements Point2D {

            private static final VarHandle X_HANDLE =
                    LAYOUT.varHandle(PathElement.groupElement("x"));

            private static final VarHandle Y_HANDLE =
                    LAYOUT.varHandle(PathElement.groupElement("y"));

            private final MemorySegment segment;
            private final long offset;

            public Point2DImpl(MemorySegment segment, long offset) {
                this.segment = segment;
                this.offset = offset;
            }

            @Override
            public MemorySegment segment() {
                return segment;
            }

            @Override
            public long offset() {
                return offset;
            }

            @Override
            public double x() {
                return (double) X_HANDLE.get(segment, offset);
            }

            @Override
            public double y() {
                return (double) Y_HANDLE.get(segment, offset);
            }

            @Override
            public void x(double x) {
                X_HANDLE.set(segment, offset, x);
            }

            @Override
            public void y(double y) {
                Y_HANDLE.set(segment, offset, y);
            }

            @Override
            public String toString() {
                return Point2D.class.getSimpleName() + "["
                        + x() + ", " +
                        y() + "]";
            }
        }

        try (var arena = Arena.ofConfined()) {
            MemorySegment pointSegment = arena.allocate(Point2D.LAYOUT);

            Point2D point2D = new Point2DImpl(pointSegment, 0);
            point2D.x(3.0d);
            point2D.y(4.0d);
            System.out.println(point2D); // Point2D[3.0, 4.0]
        }

    }

}
