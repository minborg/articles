package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;
import static org.minborg.panamadojo.Demo5_AtomicOperations.Header.FLAGS;

public class Demo5_AtomicOperations {

    static final StructLayout HEADER = MemoryLayout.structLayout(
            // The "mutex" bits in flags:
            // 7 6 5 4 3 2 1 0  15 14 13 12 11 10 9 8  ...
            // | |
            // | +-- Completed
            // +-- Acquired
            //
            // 0 -> Available, 0x80 -> Acquired, 0xC0 -> Acquired and Completed
            Util.JAVA_INT_LE.withName("flags"),
            Util.JAVA_INT_LE.withName("index")
    ); // We want this to be long-aligned

    static final class Header {

        static final int ACQUIRED = 1 << 7;
        static final int COMPLETED = 1 << 6;

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
            // MUTEX.getAndBitwiseOr(...)
            if (!FLAGS.compareAndSet(segment, ACQUIRED, ACQUIRED | COMPLETED)) {
                throw new IllegalStateException("Panic!");
            }
        }

        public boolean isCompleted() {
            return (((int) FLAGS.get(segment)) & COMPLETED) != 0;
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
                    "mutex=" + (int) FLAGS.getVolatile(segment) +
                    ", index=" + (int) INDEX.getVolatile(segment) +
                    "}";
        }
    }

    public static void main(String[] args) {
        try (var arena = Arena.ofShared()) { // Shared Arena

            var seg = arena.allocate(HEADER);
            var header = new Header(seg);
            System.out.println("Initial: " + header);

            if (!header.acquire()) {
                throw new IllegalStateException("Failed to acquire");
            }
            System.out.println("After acquire: " + header);
            header.index(42);
            header.complete();
            System.out.println("After complete: " + header);

            System.out.println(Util.toHex(seg));

        }
    }

}
