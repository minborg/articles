package org.minborg.panamadojo;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.WRITE;

public class Kata3_MemoryMapping {

    private static final Set<OpenOption> OPEN_OPTIONS =
            Set.of(CREATE, SPARSE, READ, WRITE);

    public static void main(String[] args) throws IOException {
        try (var fc = FileChannel.open(Path.of("market-info-data"), OPEN_OPTIONS);
                var arena = Arena.ofConfined()) {

            // Create a mapped memory segment managed by the arena that can hold a MarketInfo
            // The file can be shared across threads and/or processes
            MemorySegment mapped = fc.map(READ_WRITE, 0, Util.MARKET_INFO.byteSize(), arena);
            System.out.println("mapped = " + mapped);
            // mapped = MemorySegment{ heapBase: Optional.empty address:********** limit: 24 }
            System.out.println(mapped.isMapped());
            // true

            var mi = new Kata2_RecordMapper.MarketInfoRecord(
                    23_10_06__11_15_23L,
                    Util.symbolAsInt("ORCL"),
                    107,
                    106,
                    104
            );
            System.out.println(mi);
            // MarketInfo{time = 231006111523, symbol = ORCL, high = 107, last = 106, low = 104}

            Kata2_RecordMapper.MARKET_INFO_RECORD_MAPPER.set(mapped, mi);
            System.out.println(Util.toHex(mapped));
            // 23 87 09 c9 35 00 00 00 4f 52 43 4c 6b 00 00 00 6a 00 00 00 68 00 00 00
            // | time=231006111523    | O  R  C  L| high=107  | last=106  | low=104  |

        } // <- Segment is deterministically unmapped here
    }



    // % hexdump -C market-info-data
    // 00000000  23 87 09 c9 35 00 00 00  4f 52 43 4c 6b 00 00 00  |#...5...ORCLk...|
    // 00000010  6a 00 00 00 68 00 00 00                           |j...h...|
    // 00000018
}
