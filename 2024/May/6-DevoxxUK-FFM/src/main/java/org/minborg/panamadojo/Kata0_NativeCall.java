package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class Kata0_NativeCall {

    static final Linker NATIVE_LINKER = Linker.nativeLinker();
    static final SymbolLookup LOOKUP = NATIVE_LINKER.defaultLookup();

    // stdlib
    // size_t strlen(const char *s);
    static final MethodHandle STR_LEN =
            NATIVE_LINKER.downcallHandle(
                    LOOKUP.find("strlen").orElseThrow(),
                    // Java 23: LOOKUP.findOrThrow("strlen")
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_LONG,
                            ValueLayout.ADDRESS));

    public static void main(String[] args) throws Throwable {

        String s = "ABC123";

        try (Arena offHeap = Arena.ofConfined()) {
            MemorySegment str = offHeap.allocateFrom(s);
            long len = (long) STR_LEN.invoke(str);
            System.out.println("strlen("+s+") = " + len); // 6
        }
    }

}
