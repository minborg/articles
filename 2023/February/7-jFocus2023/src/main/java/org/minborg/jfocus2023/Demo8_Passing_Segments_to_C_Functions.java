package org.minborg.jfocus2023;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.ValueLayout.*;

public class Demo8_Passing_Segments_to_C_Functions {

    //
    // From the C standard library <time.h>
    //
    // struct tm {
    //    int tm_sec;         /* seconds,  range 0 to 59          */
    //    int tm_min;         /* minutes, range 0 to 59           */
    //    int tm_hour;        /* hours, range 0 to 23             */
    //    int tm_mday;        /* day of the month, range 1 to 31  */
    //    int tm_mon;         /* month, range 0 to 11             */
    //    int tm_year;        /* The number of years since 1900   */
    //    int tm_wday;        /* day of the week, range 0 to 6    */
    //    int tm_yday;        /* day in the year, range 0 to 365  */
    //    int tm_isdst;       /* daylight saving time             */
    // };
    //

    // True on macOS aarch64 and on other platforms
    private static final OfInt C_INT = JAVA_INT;

    private static final MemoryLayout TM = MemoryLayout.structLayout(
            C_INT.withName("tm_sec"),
            C_INT.withName("tm_min"),
            C_INT.withName("tm_hour"),
            C_INT.withName("tm_mday"),
            C_INT.withName("tm_mon"),
            C_INT.withName("tm_year"),
            C_INT.withName("tm_wday"),
            C_INT.withName("tm_yday"),
            C_INT.withName("tm_isdst")
    );

    public static void main(String[] args) throws Throwable {

        //
        // Also from <time.h>
        //
        // This function returns a C string containing the date and time information
        // in a human-readable format Www Mmm dd hh:mm:ss yyyy, where:
        //      Www is the weekday,
        //      Mmm the month in letters,
        //      dd the day of the month,
        //      hh:mm:ss the time,
        //      yyyy the year.
        //
        // char *asctime(const struct tm *timeptr)
        //

        MethodHandle asctime = nativeLookup(
                "asctime",
                FunctionDescriptor.of(
                        ADDRESS.asUnbounded(), // Return value of type "char*"
                        ADDRESS)               // First method parameter of type "struct tm*"
        );

        try (Arena arena = Arena.openConfined()) {

            // Initialize the struct
            MemorySegment tm = arena.allocate(TM);
            set(tm, "tm_sec", 30);
            set(tm, "tm_min", 45);
            set(tm, "tm_hour", 15);
            set(tm, "tm_mday", 7);
            set(tm, "tm_mon", 1);    // 0 = Jan, 1 = Feb, etc.
            set(tm, "tm_year", 123); // 1900 + 123 = 2023
            set(tm, "tm_wday", 2);   // 0 = Sun, 1 = Mon, 2 = Tue, etc.

            // Invoke the native method
            MemorySegment result = (MemorySegment) asctime.invokeExact(tm);

            // Extract the resulting string
            String asctimeString = result.getUtf8String(0);

            System.out.println(asctimeString);
        } // free

    }

    // Don't do this in production code!
    private static void set(MemorySegment segment,
                            String name,
                            int value) {
        TM.varHandle(groupElement(name)).set(segment, value);
    }

    private static MethodHandle nativeLookup(String name,
                                             FunctionDescriptor descriptor) {

        // A linker for the ABI associated with the underlying native platform.
        Linker linker = Linker.nativeLinker();

        // Lookup the native method
        return linker.downcallHandle(
                linker.defaultLookup().find(name).orElseThrow(),
                descriptor
        );
    }


}
