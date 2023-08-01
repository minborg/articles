package org.minborg.jep442;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

// JDK 22 builds: https://jdk.java.net/
// Make sure you set your JAVA_HOME to a JDK 22 build and provide
// "--enable-preview" when run
// In IntelliJ: Add the --enable-preview flag in the "compiler settings"
// For Maven add: export MAVEN_OPTS="--enable-preview --enable-native-access=ALL-UNNAMED"
// java --enable-preview -cp target/jep442-1.0.0-SNAPSHOT.jar org.minborg.jep442.Demo
public class Demo {

    // With "import static" of PathElement.*, MemoryLayout.* and ValueLayout.*
    private static final MemoryLayout POINT = structLayout(
            JAVA_INT.withName("x"),
            JAVA_INT.withName("y")
    );

    // struct Point {
    //   int x;
    //   int y;
    // };

    // Accessor for x
    private static final VarHandle X = POINT.varHandle(groupElement("x"));
    // Accessor for y
    private static final VarHandle Y = POINT.varHandle(groupElement("y"));

    public static void main(String[] args) throws Throwable {

        {
            // ...
            MemorySegment segment = Arena.ofAuto().allocate(POINT);
            System.out.println("segment.byteSize() = " + segment.byteSize()); // segment.byteSize() = 8
            // ...
        } // Segment eligible for collection by the GC here.
        // Actual time of collection is unspecified.

        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(POINT);
        } // Segment is deterministically freed here

        try (var arena = Arena.ofConfined()) {
            MemorySegment point = arena.allocate(POINT);
            X.set(point, 3);
            Y.set(point, 4);
            System.out.println(
                    Arrays.toString(point.toArray(JAVA_INT)) // [3, 4]
            );
        } // Point is deterministically freed here

        try (var arena = Arena.ofConfined()) {
            var point = new Point(arena);
            point.x(3);
            point.y(4);
            System.out.println(point); // Point[3, 4]
        } // Point is deterministically freed here

        /* Might Preview in 22 ...

        record Point(int x, int y){}

        var segment = MemorySegment.ofArray(new int[]{3, 4, 10, 12});

        Mapper<Point> mapper = POINT.recordMapper(Point.class);

        Point point = mapper.get(segment); // Point[3, 4]

        List<Point> points = segment.elements(POINT)
                .map(mapper::get)
                .toList(); // [Point[3, 4], Point[10, 12]]

         record Foo(int y){}
         Foo foo = POINT.recordMapper(Foo.class).get(segment) // Foo(4)

         */

        Linker linker = Linker.nativeLinker();
        MethodHandle strlen = linker.downcallHandle(
                linker.defaultLookup().find("strlen").orElseThrow(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS)
        );

        try (Arena arena = Arena.ofConfined()) {
            var s = "Hello";
            MemorySegment sSegment = arena.allocateUtf8String(s);
            long len = (long) strlen.invokeExact(sSegment);
            System.out.println("The length of '" + s + "' is " + len); // The length of 'Hello' is 5
        }

    }

    static final class Point {

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

}
