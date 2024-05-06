package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

public class Kata5_AtomicOperations {
    // QUEUE:
    // HEADER, PAYLOAD, HEADER, PAYLOAD, ...

    static final StructLayout HEADER = MemoryLayout.structLayout(
            // "flags" have "mutex" bits:
            // 7 6 5 4 3 2 1 0  15 14 13 12 11 10 9 8  16 17 ...
            // ^ ^
            // | |
            // | +-- Completed
            // +-- Acquired
            //
            // A) 0 -> Free,
            // B) 0x80 -> Acquired,
            // C) 0xC0 -> Acquired and Completed
            Util.JAVA_INT_LE.withName("flags"),
            Util.JAVA_INT_LE.withName("index")
    ).withByteAlignment(JAVA_LONG.byteAlignment()); // We want this to be long-aligned

    static final class Header {

        static final int FREE = 0;           // 0x00
        static final int ACQUIRED = 1 << 7;  // 0x80
        static final int COMPLETED = 1 << 6; // 0x40

        // https://bugs.openjdk.org/browse/JDK-8331734

        static final VarHandle FLAGS =
                JAVA_INT.varHandle();
                // HEADER.varHandle(PathElement.groupElement("flags"));
        static final VarHandle INDEX =
                offset(JAVA_INT.varHandle(), HEADER.byteOffset(PathElement.groupElement("index")));
                //HEADER.varHandle(PathElement.groupElement("index"));

        private final MemorySegment segment;

        public Header(MemorySegment segment) {
            if (segment.byteSize() < HEADER.byteSize()) {
                throw new IllegalArgumentException("Segment is too small: " + segment);
            }
            this.segment = segment;
        }

        public boolean acquire() {
            // Works across all threads/JVMs on the same machine
            return FLAGS.compareAndSet(segment, 0L, FREE, ACQUIRED);
        }

        public void complete() {
            if (!FLAGS.compareAndSet(segment, 0L, ACQUIRED, ACQUIRED | COMPLETED)) {
                throw new IllegalStateException("Panic!");
            }
            // FLAGS.getAndBitwiseOr(...)
        }

        public boolean isCompleted() {
            return (((int) FLAGS.getVolatile(segment, 0L)) & COMPLETED) != 0;
        }

        public int index() {
            return (int) INDEX.get(segment, 0L);
        }

        public void index(int index) {
            INDEX.set(segment, 0L, index);
        }

        @Override
        public String toString() {
            return "Header{" +
                    "mutex=0x" + Integer.toHexString((int) FLAGS.getVolatile(segment, 0L)) +
                    ", index=" + (int) INDEX.getVolatile(segment, 0L) +
                    "}";
        }
    }

    public static void main(String[] args) {

        try (var arena = Arena.ofConfined()) {

            var seg = arena.allocate(HEADER);
            System.out.println("seg = " + seg);
            // MemorySegment{ address: 0xxxxxxxx(0/8), byteSize: 8 }

            var header = new Header(seg);
            System.out.println("Initial: " + header);
            // Initial: Header{mutex=0x0, index=0}

            if (!header.acquire()) {
                throw new IllegalStateException("Failed to acquire");
            }
            System.out.println("After acquire: " + header);
            // After acquire: Header{mutex=0x80, index=0}

            // Update payload here...

            header.index(42);  // Must be done before complete!
            header.complete(); // Establishes HB
            System.out.println("After complete: " + header);
            // After complete: Header{mutex=0xc0, index=42}

            System.out.println(Util.toHex(seg));
            // c0 00 00 00 2a 00 00 00
            // |  mutex   |  index   |

        }
    }

    // Work around:

    private static VarHandle offset(VarHandle target, long offset) {
        MethodHandle mh = MethodHandles.insertArguments(MH_ADD, 0, offset);
        return MethodHandles.collectCoordinates(target, 1, mh);
    }

    private static final MethodHandle MH_ADD;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MH_ADD = lookup.findStatic(Long.class, "sum",
                    MethodType.methodType(long.class, long.class, long.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }

    }


}
