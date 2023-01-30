package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

public class Demo7HugeArrays {

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

        static Point2D create(Arena arena) {
            return create(MemorySegment.allocateNative(POINT_2D_LAYOUT, arena.scope()));
        }

        static Point2D create(MemorySegment segment) {
            return new SegmentPoint2D(segment);
        }

    }

    interface Point2DArray {
        double x(long index);

        double y(long index);

        void x(long index, double x);

        void y(long index, double y);

        Point2D get(long index);
    }


    static final class SegmentPoint2DArray implements Point2DArray {

        private static final MemoryLayout POINT_2D_ARRAY_LAYOUT = sequenceLayout(
                SegmentPoint2D.POINT_2D_LAYOUT
        ).withName("point2dArray");

        private static final VarHandle X_ARRAY_ACCESS = POINT_2D_ARRAY_LAYOUT.varHandle(sequenceElement(), groupElement("x"));
        private static final VarHandle Y_ARRAY_ACCESS = POINT_2D_ARRAY_LAYOUT.varHandle(sequenceElement(), groupElement("y"));
        private final MemorySegment segment;

        private SegmentPoint2DArray(Arena arena, long length) {
            long byteSize = SegmentPoint2D.POINT_2D_LAYOUT.byteSize() * length;
            this.segment = MemorySegment.allocateNative(byteSize, arena.scope());
        }

        @Override
        public double x(long index) {
            return (double) X_ARRAY_ACCESS.get(segment, index);
        }

        @Override
        public double y(long index) {
            return (double) Y_ARRAY_ACCESS.get(segment, index);
        }

        @Override
        public void x(long index, double x) {
            X_ARRAY_ACCESS.set(segment, index, x);
        }

        @Override
        public void y(long index, double y) {
            Y_ARRAY_ACCESS.set(segment, index, y);
        }

        @Override
        public Point2D get(long index) {
            return SegmentPoint2D.create(
                    segment.asSlice(
                            index * SegmentPoint2D.POINT_2D_LAYOUT.byteSize(), // offset
                            SegmentPoint2D.POINT_2D_LAYOUT.byteSize()));             // byteSize
        }

        public static Point2DArray create(Arena arena, long length) {
            return new SegmentPoint2DArray(arena, length);
        }

    }


    public static void main(String[] args) {

        try (Arena arena = Arena.openConfined()) {

            Point2DArray points = SegmentPoint2DArray.create(arena, 100_000);

            long index = 42;

            points.x(index, 3d);
            points.y(index, 4d);

            System.out.println(points.get(index));
        } // free

    }


}
