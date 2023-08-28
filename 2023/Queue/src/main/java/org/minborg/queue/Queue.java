package org.minborg.jep442;

import java.lang.foreign.Arena;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.util.List;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public final class Queue extends AbstractSegmentWrapper {

    // A mini framework for a persistent queue

    private static final long MAGIC = 0xDEAD_C00L;
    private static final int VERSION = 42;

    private static final MemoryLayout HEADER = MemoryLayout.structLayout(
            JAVA_LONG.withName("magic"),
            JAVA_INT.withName("version"),
            JAVA_INT.withName("length"),
            MemoryLayout.sequenceLayout(
                    16,
                    MemoryLayout.structLayout(JAVA_INT.withName("key"), JAVA_INT.withName("value"))).withName("config")
    );

    record KeyValue(int key, int value) {

        void serialize(MemorySegment segment) {
            segment.set(JAVA_INT, 0, key);
            segment.set(JAVA_INT, 4, value);
        }

        public static KeyValue of(MemorySegment segment) {
            return new KeyValue(segment.get(JAVA_INT, 0), segment.get(JAVA_INT, 4));
        }
    }

    record Header(long magic, int version, int length, KeyValue[] config){

        private static final VarHandle MAGIC_HANDLE = handle("magic");
        private static final VarHandle VERSION_HANDLE = handle("version");
        private static final VarHandle LENGTH_HANDLE = handle("length");
        private static final long CONFIG_OFFSET = HEADER.byteOffset(PathElement.groupElement("config"));

        public Header(KeyValue... keyValues) {
            this(MAGIC, VERSION, 4 * 2 * keyValues.length, keyValues.clone());
        }

        void serialize(MemorySegment segment) {
            MAGIC_HANDLE.set(segment, 0, magic);
            VERSION_HANDLE.set(segment, 0, version);
            LENGTH_HANDLE.set(segment, 0, length);
            IntStream.of(0, length / (4 * 2))
                    .forEach(i -> {
                        var slice = segment.asSlice(CONFIG_OFFSET + i * 4 * 2);
                        config[i].serialize(slice);
                    });
        }

        @Override
        public String toString() {
            return String.format("Header[magic=%X, version=%d, length=%d, config=%s]", magic, version, length, Arrays.toString(config));
        }

        public static Header of(MemorySegment segment) {
            int length = (int)LENGTH_HANDLE.get(segment);
            return new Header(
                    (long)MAGIC_HANDLE.get(segment, 0),
                    (int)VERSION_HANDLE.get(segment, 0),
                    length,
                    IntStream.of(0, length/(4 *2))
                            .mapToObj(i -> segment.asSlice(CONFIG_OFFSET + i * 4 * 2))
                            .map(KeyValue::of)
                            .toArray(KeyValue[]::new));
        }

        private static VarHandle handle(String name) {
            return HEADER.varHandle(PathElement.groupElement(name));
        }

    }

    private static final MemoryLayout DOCUMENT = MemoryLayout.structLayout(
            JAVA_INT.withName("header"), // Header contains locking and length info
            MemoryLayout.sequenceLayout(Integer.MAX_VALUE, JAVA_BYTE).withName("payload")
    );

    record Document(int header, byte[] payload){

        void serialize(MemorySegment segment) {
            segment.set(JAVA_INT, 0, key);
            segment.set(JAVA_INT, 4, value);
        }

        public static Document of(MemorySegment segment) {
            return null;
        }

        long append(byte[] payload) {
            throw new UnsupportedOperationException();
        }

        long next(long index) {
            throw new UnsupportedOperationException();
        }

    }

    static final class Cursor {
        private final AtomicLong index;
        private final MemorySegment segment;

        public Cursor(MemorySegment segment) {
            this(segment, 0L);
        }

        public Cursor(MemorySegment segment, long index) {
            this.segment = Objects.requireNonNull(segment);
            this.index = new AtomicLong(index);
        }

        Document acquire() {
            return Document.
        }

    }


    record Context(MemorySegment segment) implements AutoCloseable {

        private static final VarHandle HEADER_HANDLE = DOCUMENT.varHandle(PathElement.groupElement("header"));

        public void acquire() {
            HEADER_HANDLE.getAndBitwiseOr(segment, 0, 0x8000_0000);
        }


        @Override
        public void close() {
            HEADER_HANDLE.getAndBitwiseAnd(segment, 0, ~0x8000_0000);
        }

        Context of(MemorySegment segment) {
            return new Context(segment);
        }

    }


    @Test
    void initHeader() {
        Header header = new Header();
        System.out.println("header = " + header);


        throw new AssertionError();
    }

}
