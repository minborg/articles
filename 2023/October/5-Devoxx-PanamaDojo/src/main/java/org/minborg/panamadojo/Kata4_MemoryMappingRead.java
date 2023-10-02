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

public class Kata4_MemoryMappingRead {

    private static final Set<OpenOption> OPEN_OPTIONS =
            Set.of(SPARSE, READ); // Read only

    public static void main(String[] args) throws IOException {
        MarketInfo mi;

        try (var fc = FileChannel.open(Path.of("market-data"), OPEN_OPTIONS);
                var arena = Arena.ofConfined()) {

            MemorySegment mapped = fc.map(READ_ONLY, 0, Util.MARKET_INFO.byteSize(), arena);
            mi = Kata2_RecordMapper.MARKET_INFO_RECORD_MAPPER.get(mapped);
        } // <- Segment is deterministically unmapped here

        System.out.println(mi);
        // 23 87 09 c9 35 00 00 00 4f 52 43 4c 6b 00 00 00 6a 00 00 00 68 00 00 00
        // | time=231016111523    | O  R  C  L| high=107  | last=106  | low=104  |
    }

}
