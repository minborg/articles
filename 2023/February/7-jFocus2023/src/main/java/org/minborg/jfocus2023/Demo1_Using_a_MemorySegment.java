package org.minborg.jfocus2023;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.Arrays;

// Make sure you set your JAVA_HOME to a JDK 20 build and provide "--enable-preview" when run
// In IntelliJ: Add the --enable-preview flag in the "compiler settings"
// For Maven add: export MAVEN_OPTS="--enable-preview"
public class Demo1_Using_a_MemorySegment {

    public static void main(String[] args) {

        // struct Point2D {
        //     double x;
        //     double y;
        // } point = { 3.0, 4.0 }

        MemorySegment point = MemorySegment.allocateNative(8 * 2, SegmentScope.auto());
        point.set(ValueLayout.JAVA_DOUBLE, 0, 3d);
        point.set(ValueLayout.JAVA_DOUBLE, 8, 4d);

        byte[] bytesCopy = point.toArray(ValueLayout.JAVA_BYTE);
        double[] doublesCopy = point.toArray(ValueLayout.JAVA_DOUBLE);
        ByteBuffer byteBufferView = point.asByteBuffer();

        // Interop FFM -> legacy
        System.out.println(Arrays.toString(bytesCopy));
        System.out.println(Arrays.toString(doublesCopy));
        System.out.println(byteBufferView);

        // Interop legacy -> FFM
        MemorySegment arrayView = MemorySegment.ofArray(new byte[]{1, 2, 3, 4, 5});
        MemorySegment segmentView = MemorySegment.ofBuffer(ByteBuffer.allocateDirect(100));
    }

}
