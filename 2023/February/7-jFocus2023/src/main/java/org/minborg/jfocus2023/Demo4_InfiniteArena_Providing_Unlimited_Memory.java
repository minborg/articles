package org.minborg.jfocus2023;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.requireNonNull;

// Memory mapping and Arena combined.
public final class Demo4_InfiniteArena_Providing_Unlimited_Memory {

    private static final class InfiniteArea implements Arena {
        private static final Set<OpenOption> OPTS =
                Set.of(CREATE_NEW, SPARSE, READ, WRITE);

        private final String fileName;
        private final AtomicLong cnt;
        private final Arena delegate;

        public InfiniteArea() {
            this.fileName = "InfiniteArena";
            this.cnt = new AtomicLong();
            this.delegate = Arena.openShared();
        }

        @Override
        public MemorySegment allocate(long byteSize, long byteAlignment) {
            try {
                try (var fc = FileChannel.open(
                        Path.of(fileName + "-" + cnt.getAndIncrement()), OPTS)) {
                    return fc.map(READ_WRITE, 0, byteSize, delegate.scope());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SegmentScope scope() {
            return delegate.scope();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public boolean isCloseableBy(Thread thread) {
            return delegate.isCloseableBy(thread);
        }
    }

    public static void main(String[] args) {
        try (Arena arena = new InfiniteArea()) {

            // Allocate 1 TiB mapped to the file "InfiniteArea-0"
            MemorySegment s0 = arena.allocate(1L << 40);
            // Do nothing with s0

            // Allocate another 1 TiB mapped to the file "InfiniteArea-1"
            MemorySegment s1 = arena.allocate(1L << 40);
            // Fill the region [1024 to 1024+256) with the value 2
            s1.asSlice(1024, 256)
                    .fill((byte) 2);

            // 16 bytes in "InfiniteArea-2"
            MemorySegment s2 = arena.allocate(16);

            // Write a String to the segment
            s2.setUtf8String(0, "Hello World");
        }
    }
}


