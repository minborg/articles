package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

public class Kata5_AtomicOperations {

    static final StructLayout HEADER = MemoryLayout.structLayout(
            // "flags" have "mutex" bits:
            // 7 6 5 4 3 2 1 0  15 14 13 12 11 10 9 8  16 17 ...
            // | |
            // | +-- Completed
            // +-- Acquired
            //
            // A) 0 -> Available,
            // B) 0x80 -> Acquired,
            // C) 0xC0 -> Acquired and Completed
            Util.JAVA_INT_LE.withName("flags"),
            Util.JAVA_INT_LE.withName("index")
    ); // We want this to be long-aligned

    static final class Header {

        static final int ACQUIRED = 1 << 7;  // 0x80
        static final int COMPLETED = 1 << 6; // 0x40

        static final VarHandle FLAGS =
                HEADER.varHandle(PathElement.groupElement("flags"));
        static final VarHandle INDEX =
                HEADER.varHandle(PathElement.groupElement("index"));

        private final MemorySegment segment;

        public Header(MemorySegment segment) {
            if (segment.byteSize() < HEADER.byteSize()) {
                throw new IllegalArgumentException("Segment is too small: " + segment);
            }
            this.segment = segment;
        }

        public boolean acquire() {
            return FLAGS.compareAndSet(segment, (int) 0, ACQUIRED);
        }

        public void complete() {
            if (!FLAGS.compareAndSet(segment, ACQUIRED, ACQUIRED | COMPLETED)) {
                throw new IllegalStateException("Panic!");
            }
            // MUTEX.getAndBitwiseOr(...)
        }

        public boolean isCompleted() {
            return (((int) FLAGS.getVolatile(segment)) & COMPLETED) != 0;
        }

        public int index() {
            return (int) INDEX.get(segment);
        }

        public void index(int index) {
            INDEX.set(segment, index);
        }

        @Override
        public String toString() {
            return "Header{" +
                    "mutex=0x" + Integer.toHexString((int) FLAGS.getVolatile(segment)) +
                    ", index=" + (int) INDEX.getVolatile(segment) +
                    "}";
        }
    }

    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {

            var seg = arena.allocate(HEADER);
            var header = new Header(seg);
            System.out.println("Initial: " + header);
            // Initial: Header{mutex=0x0, index=0}

            if (!header.acquire()) {
                throw new IllegalStateException("Failed to acquire");
            }
            System.out.println("After acquire: " + header);
            // After acquire: Header{mutex=0x80, index=0}

            header.index(42);  // Must be done before complete!
            header.complete(); // Establishes HB
            System.out.println("After complete: " + header);
            // After complete: Header{mutex=0xc0, index=42}

            System.out.println(Util.toHex(seg));
            // c0 00 00 00 2a 00 00 00
            // |  mutex   |  index   |

        }
    }

}
