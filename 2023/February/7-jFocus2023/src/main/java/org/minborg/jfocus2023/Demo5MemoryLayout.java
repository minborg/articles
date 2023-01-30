package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

public class Demo5MemoryLayout {

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

        try (Arena arena = Arena.openConfined()) {

            MemorySegment point = MemorySegment.allocateNative(POINT_2D_LAYOUT, arena.scope());
            X_ACCESS.set(point, 3d);
            Y_ACCESS.set(point, 4d);

            System.out.println(Arrays.toString(
                    point.toArray(ValueLayout.JAVA_DOUBLE)
            ));
        } // free

    }

}
