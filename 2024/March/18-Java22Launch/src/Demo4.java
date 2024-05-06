package src;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.*;

public class Demo4 {

    // Memory mapping
    public static void main(String[] args) throws IOException {

        Set<OpenOption> openOptions = Set.of(CREATE, SPARSE, READ, WRITE);

        try (var fc = FileChannel.open(Path.of("../sparse"), openOptions);
             var arena = Arena.ofConfined()) {

            // Create a 64 TiB mapped memory segment managed by the provided arena
            MemorySegment mapped = fc.map(READ_WRITE, 0, 1L << 45, arena);

            // Do stuff ...

            System.out.println(mapped);
        }

    }
}
