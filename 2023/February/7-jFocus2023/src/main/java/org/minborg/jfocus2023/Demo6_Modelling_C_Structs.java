package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

public class Demo6_Modelling_C_Structs {

    interface Point2D {
        double x();
        double y();
        void x(double x);
        void y(double y);
    }

    static final class HeapPoint2D implements Point2D {
        double x;
        double y;

        private HeapPoint2D() {}

        @Override
        public double x() {
            return x;
        }

        @Override
        public double y() {
            return y;
        }

        @Override
        public void x(double x) {
            this.x = x;
        }

        @Override
        public void y(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "[" + x() + ", " + y() + "]";
        }

        // Equals and hashcode not desirable for double fields

        static Point2D create() {
            return new HeapPoint2D();
        }

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

        static Point2D create(SegmentScope scope) {
            return new SegmentPoint2D(
                    MemorySegment.allocateNative(POINT_2D_LAYOUT, scope)
            );
        }

    }

    public static void main(String[] args) {

        try (Arena arena = Arena.openConfined()) {

            Point2D point = SegmentPoint2D.create(arena.scope());
            point.x(3d);
            point.y(4d);

            System.out.println(point);
        } // free

    }


}
