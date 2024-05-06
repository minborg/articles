package src;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.HexFormat;

public class Demo1 {

    // Working with flat memory
    public static void main(String[] args) {

        // Flat memory as arrays, back in the bad old days
        byte[] byteArray = new byte[1024];
        // Try writing an `int` into a byte array

        int[] intArray = new int[256];
        // Good luck writing a `byte` or a `long`...

        // Array Problems:
        // - Specific component type only
        // - Heap memory only
        // - Byte ordering always the native byte order
        // - Limited to ~2^32     =  ~2 GiB for byte arrays
        // - Limited to ~2^32 * 8 = ~16 GiB for long arrays
        // - ...

        // long[] maxMem = new long[Integer.MAX_VALUE - 8];


        // Flat memory as ByteBuffers

        // Heap Memory (e.g. array)
        ByteBuffer wrapped = ByteBuffer.wrap(byteArray);
        // Native Memory (e.g. malloc/mmap)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

        byteBuffer.putInt(0, 42);
        byteBuffer.putLong(4, 13);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // ByteBuffer Problems:
        // - Cannot be deterministically freed
        // - Limited to a byte size of ~2^31 = ~2 GiB
        // - Not thread safe
        // - No option for alignment checks
        // - No special/atomic operations (e.g. volatile or CAS operations)
        // - No support for structured access:

        //     # Original C struct
        //
        //     struct Point2D {
        //         double x;
        //         double y;
        //     } point = { 3.0, 4.0 }
        //


        // Enter FFM API (Memory Part)

        // Models a thread-safe, immutable, 64-bit continuous region of memory
        MemorySegment segment;

        try (Arena arena = Arena.ofConfined()) {
            segment = arena.allocate(16);
            // Do stuff ...
        } // Segment is deterministically freed here

        // segment.get(ValueLayout.JAVA_DOUBLE, 0L);
        //     -> IllegalStateException


        try (Arena arena = Arena.ofConfined()) {
            segment = arena.allocate(16);

            System.out.println(segment);

            segment.set(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN), 0, 3);

            HexFormat format = HexFormat.ofDelimiter(" ");
            byte[] asArray = segment.toArray(ValueLayout.JAVA_BYTE);
            String hex = format.formatHex(asArray);

            System.out.println(hex); // 03 00 00 00 ...
        }

        // Interoperability legacy -> FFM
        MemorySegment arrayView = MemorySegment.ofArray(byteArray);
        MemorySegment bufferView = MemorySegment.ofBuffer(byteBuffer);

        // Interoperability FFM -> legacy
        byte[] bytesCopy = bufferView.toArray(ValueLayout.JAVA_BYTE);
        double[] doublesCopy = bufferView.toArray(ValueLayout.JAVA_DOUBLE);
        ByteBuffer byteBufferView = bufferView.asByteBuffer();
    }

}
