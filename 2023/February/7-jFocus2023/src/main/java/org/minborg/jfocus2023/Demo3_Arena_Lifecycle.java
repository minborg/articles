package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Demo3_Arena_Lifecycle {

    public static void main(String[] args) {

        // MemorySegments are safe as opposed to memory from C's malloc/free/pointer
        try (Arena arena = Arena.openConfined()) {

            MemorySegment point = arena.allocate(8 * 2);

            point.set(ValueLayout.JAVA_DOUBLE, 0, 3d);
            point.set(ValueLayout.JAVA_DOUBLE, 8, 4d);

            System.out.println(Arrays.toString(
                    point.toArray(ValueLayout.JAVA_DOUBLE)
            ));
        } // free

        // point is not accessible here

        try (Arena arena = Arena.openShared()) {
            MemorySegment shared = MemorySegment.allocateNative(8 * 2, arena.scope());
            shared.set(ValueLayout.JAVA_DOUBLE, 0, 3d);
            shared.set(ValueLayout.JAVA_DOUBLE, 8, 4d);
            useInOtherThreads(shared);
        } // free safely via "handshaking"

    }

    static void useInOtherThreads(MemorySegment segment) {
        IntStream.range(0, 10_000)
                .parallel()
                .forEach(i -> segment.get(ValueLayout.JAVA_DOUBLE, 0));
    }

}
