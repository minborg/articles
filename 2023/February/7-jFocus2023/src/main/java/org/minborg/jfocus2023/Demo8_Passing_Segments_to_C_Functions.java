package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.MemorySegment.*;
import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

public class Demo8_Passing_Segments_to_C_Functions {

    interface Point2D {
        double x();

        double y();

        void x(double x);

        void y(double y);
    }

    static final class SegmentPoint2D implements Point2D {

        // struct point2d {
        //     double x;
        //     double y;
        // }
        private static final MemoryLayout POINT_2D_LAYOUT = structLayout(
                JAVA_DOUBLE.withName("x"),
                JAVA_DOUBLE.withName("y")
        ).withName("point2d");

        private static final VarHandle X_ACCESS = POINT_2D_LAYOUT.varHandle(groupElement("x"));
        private static final VarHandle Y_ACCESS = POINT_2D_LAYOUT.varHandle(groupElement("y"));

        private final MemorySegment segment;

        private SegmentPoint2D(MemorySegment segment) {
            this.segment = requireNonNull(segment);
        }

        @Override
        public double x() {
            return (double) X_ACCESS.get(segment);
        }

        @Override
        public double y() {
            return (double) Y_ACCESS.get(segment);
        }

        @Override
        public void x(double x) {
            X_ACCESS.set(segment, x);
        }

        @Override
        public void y(double y) {
            Y_ACCESS.set(segment, y);
        }

        @Override
        public String toString() {
            return "[" + x() + ", " + y() + "]";
        }

        public MemorySegment segment() {
            return segment;
        }

        static SegmentPoint2D create(Arena arena) {
            return create(allocateNative(POINT_2D_LAYOUT, arena.scope()));
        }

        static SegmentPoint2D create(MemorySegment segment) {
            return new SegmentPoint2D(segment);
        }

    }


    public static void main(String[] args) throws Throwable {

        // C code: extern double distance(struct Point2d p)

        // You *must* have a library that implements the above available or else the code will not run
        MemorySegment symbol = SymbolLookup.loaderLookup()
                .find("distance")
                .orElseThrow(() -> new IllegalStateException("Cannot find an implementation of 'distance'"));

        MethodHandle distanceHandle = Linker.nativeLinker().downcallHandle(
                symbol, FunctionDescriptor.of(JAVA_DOUBLE, SegmentPoint2D.POINT_2D_LAYOUT));

        try (Arena arena = Arena.openConfined()) {

            SegmentPoint2D point = SegmentPoint2D.create(arena);
            point.x(3.0);
            point.y(4.0);
            double dist = (double) distanceHandle.invoke(point.segment());
            System.out.println(dist); // -> 5d
        } // free

    }

}
