package org.minborg.jep442;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

// JDK 21 builds: https://jdk.java.net/
// Make sure you set your JAVA_HOME to a JDK 21 build and provide
// "--enable-preview" when run
// In IntelliJ: Add the --enable-preview flag in the "compiler settings"
// For Maven add: export MAVEN_OPTS="--enable-preview"
public class Demo {

    // With "import static" of PathElement.*, MemoryLayout.* and ValueLayout.*
    private static final MemoryLayout POINT = structLayout(
            JAVA_INT.withName("x"),
            JAVA_INT.withName("y")
    ).withName("point");

    // Accessor for x
    private static final VarHandle X = POINT.varHandle(groupElement("x"));
    // Accessor for y
    private static final VarHandle Y = POINT.varHandle(groupElement("y"));


    public static void main(String[] args) throws Throwable {

        {
            // ...
            MemorySegment segment = Arena.ofAuto().allocate(16);
            // ...
        } // Segment eligible for collection by the GC here.
        // Actual time of collection is unspecified.

        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(16);
        } // Segment is deterministically freed here

        try (var arena = Arena.ofConfined()) {
            MemorySegment point = arena.allocate(POINT);
            X.set(point, 3);
            Y.set(point, 4);
            System.out.println(
                    Arrays.toString(point.toArray(JAVA_INT))
            );
        } // Point is deterministically freed here

        try (var arena = Arena.ofConfined()) {
            var point = new Point(arena);
            point.x(3);
            point.y(4);
            System.out.println(point);
        } // Point is deterministically freed here

        Linker linker = Linker.nativeLinker();
        MethodHandle strlen = linker.downcallHandle(
                linker.defaultLookup().find("strlen").get(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS)
        );

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment str = arena.allocateUtf8String("Hello");
            long len = (long) strlen.invoke(str);
            System.out.println("The length is " + len);
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
            return "(" + x() + ", " + y() + ")";
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
