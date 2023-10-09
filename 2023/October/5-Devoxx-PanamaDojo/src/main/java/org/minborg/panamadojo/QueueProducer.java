package org.minborg.panamadojo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.minborg.panamadojo.Kata5_AtomicOperations.*;

public interface QueueProducer<T extends Record> extends AutoCloseable {

    /**
     * Appends an element at the end of the queue.
     *
     * @param element to append
     */
    void append(T element);

    @Override
    void close();

    static <T extends Record> QueueProducer<T> of(RecordMapper<T> mapper,
                                                  Path path) {
        return new Impl<>(mapper, path);
    }

    final class Impl<T extends Record> implements QueueProducer<T> {

        private static final Set<OpenOption> OPEN_OPTIONS =
                Set.of(CREATE, SPARSE, READ, WRITE);

        private final RecordMapper<T> mapper;
        private final Arena arena;
        private final MemorySegment segment; // mmap:ed
        private long position;

        public Impl(RecordMapper<T> mapper, Path path) {
            this.mapper = mapper;
            this.arena = Arena.ofConfined();
            try (var fc = FileChannel.open(path, OPEN_OPTIONS)) {
                // Map 1 MiB
                this.segment = fc.map(READ_WRITE, 0, 1 << 20, arena);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void append(T element) {
            Header header = headerAtPosition();
            int index = header.index();
            // Seek to the next available slot
            while (header.isCompleted() || !header.acquire()) {
                position += HEADER.byteSize() + mapper.layout().byteSize();
                header = headerAtPosition();
                int i = header.index();
                if (i != 0) {
                    index = header.index();
                }
            }
            // Now we are alone
            MemorySegment payload =
                    segment.asSlice(position + HEADER.byteSize(), mapper.layout());

            mapper.set(payload, element);
            header.index(++index);
            header.complete(); // HB
        }

        @Override
        public void close() {
            arena.close();
        }

        private Header headerAtPosition() {
            return new Header(segment.asSlice(position, HEADER));
        }

    }

}
