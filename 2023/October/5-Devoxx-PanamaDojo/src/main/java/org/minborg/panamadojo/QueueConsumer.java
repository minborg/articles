package org.minborg.panamadojo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.*;
import static org.minborg.panamadojo.Demo5_AtomicOperations.*;
import static org.minborg.panamadojo.Demo5_AtomicOperations.HEADER;

public interface QueueConsumer<T extends Record> extends AutoCloseable {

    Optional<T> next();

    @Override
    void close();

    static <T extends Record> QueueConsumer<T> of(RecordMapper<T> mapper, Path path) {
        return new Impl<>(mapper, path);
    }

    final class Impl<T extends Record> implements QueueConsumer<T> {

        private static final Set<OpenOption> OPEN_OPTIONS = Set.of(SPARSE, READ);

        private final RecordMapper<T> mapper;
        private final Arena arena;
        private final MemorySegment segment;
        private long position;

        public Impl(RecordMapper<T> mapper, Path path) {
            this.mapper = mapper;
            this.arena = Arena.ofConfined();
            try (var fc = FileChannel.open(path, OPEN_OPTIONS)) {
                // Map 1 MiB
                this.segment = fc.map(READ_ONLY, 0, 1 << 20, arena);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Optional<T> next() {
            Header header = headerAtPosition();
            if (!header.isCompleted()) return Optional.empty();

            MemorySegment payload =
                    segment.asSlice(position + HEADER.byteSize(), mapper.layout());
            position += HEADER.byteSize() + mapper.layout().byteSize();
            return Optional.of(mapper.get(payload));
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
