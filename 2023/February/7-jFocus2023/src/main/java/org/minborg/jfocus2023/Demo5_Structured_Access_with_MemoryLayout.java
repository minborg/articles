package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public class Demo5_Structured_Access_with_MemoryLayout {

    // struct point2d {
    //     double x;
    //     double y;
    // }
    private static final MemoryLayout POINT_2D_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName("x"),
            ValueLayout.JAVA_DOUBLE.withName("y")
    ).withName("point2d");

    private static final VarHandle X_ACCESS = POINT_2D_LAYOUT.varHandle(PathElement.groupElement("x"));
    private static final VarHandle Y_ACCESS = POINT_2D_LAYOUT.varHandle(PathElement.groupElement("y"));

    public static void main(String[] args) {

        System.out.println("POINT_2D_LAYOUT = " + POINT_2D_LAYOUT);
        System.out.println("POINT_2D_LAYOUT.byteSize() = " + POINT_2D_LAYOUT.byteSize());
        System.out.println();

        try (Arena arena = Arena.openConfined()) {

            MemorySegment point = arena.allocate(POINT_2D_LAYOUT);
            X_ACCESS.set(point, 3d);
            Y_ACCESS.set(point, 4d);

            // SegmentInspection is considered a candidate for Panama in JDK 21
            SegmentInspection.inspect(point, POINT_2D_LAYOUT)
                    .forEach(System.out::println);

            // CAS Operations and more...
            if (X_ACCESS.compareAndSet(point, 3d, 9d)) {
                System.out.println("The x value was updated from 3d to 9d");
            }

        } // free

    }

}
