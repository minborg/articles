package org.minborg.jfocus2023;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.*;

public class Demo7MemoryMapping {
    public static void main(String[] args) throws IOException {

        Set<OpenOption> sparse = Set.of(CREATE_NEW, SPARSE, READ, WRITE);

        try (var fc = FileChannel.open(Path.of("sparse"), sparse);
             var arena = Arena.openConfined()) {

            // Create a 64 TiB mapped memory segment
            MemorySegment mapped = fc.map(READ_WRITE, 0, 1L << 46, arena.scope());

            System.out.println(mapped);

        } // Resources allocated by "mapped" is released here via TwR

    }

}
