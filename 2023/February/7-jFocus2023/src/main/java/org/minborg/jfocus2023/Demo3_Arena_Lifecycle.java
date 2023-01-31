package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;

public class Demo3_Arena_Lifecycle {

    public static void main(String[] args) {

        try (Arena arena = Arena.openConfined()) {

            MemorySegment point = MemorySegment.allocateNative(8 * 2, arena.scope());
            point.set(ValueLayout.JAVA_DOUBLE, 0, 3d);
            point.set(ValueLayout.JAVA_DOUBLE, 8, 4d);

            System.out.println(Arrays.toString(
                    point.toArray(ValueLayout.JAVA_DOUBLE)
            ));
        } // free

        // point is not accessible here
    }
}
