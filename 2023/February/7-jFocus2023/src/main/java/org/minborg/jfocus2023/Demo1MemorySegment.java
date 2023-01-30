package org.minborg.jfocus2023;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;

// Make sure you set your JAVA_HOME to a JDK 20 build and provide "--enable-preview" when run
// In IntelliJ: Add the --enable-preview flag in the "compiler settings"
// For Maven add: export MAVEN_OPTS="--enable-preview"
public class Demo1MemorySegment {

    public static void main(String[] args) {

        // struct Point2D {
        //     double x;
        //     double y;
        // } point = { 3.0, 4.0 }

        MemorySegment point = MemorySegment.allocateNative(8 * 2, SegmentScope.auto());
        point.set(ValueLayout.JAVA_DOUBLE, 0, 3d);
        point.set(ValueLayout.JAVA_DOUBLE, 8, 4d);

        System.out.println(Arrays.toString(
                point.toArray(ValueLayout.JAVA_DOUBLE)
        ));

    }

}
