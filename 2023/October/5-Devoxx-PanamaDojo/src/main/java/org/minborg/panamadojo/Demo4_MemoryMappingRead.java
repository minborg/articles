package org.minborg.panamadojo;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.*;

public class Demo4_MemoryMappingRead {

    private static final Set<OpenOption> OPEN_OPTIONS = Set.of(SPARSE, READ); // Read only

    public static void main(String[] args) throws IOException {
        MarketInfo mi;

        try (var fc = FileChannel.open(Path.of("market-data"), OPEN_OPTIONS);
                var arena = Arena.ofConfined()) {

            MemorySegment mapped = fc.map(READ_ONLY, 0, Util.MARKET_INFO.byteSize(), arena);
            mi = Demo2_RecordMapper.RECORD_MAPPER.get(mapped);
        } // <- Segment is deterministically unmapped here

        System.out.println(mi);
    }

}
