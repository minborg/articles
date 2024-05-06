package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Kata2_RecordMapper {

    public record MarketInfoRecord(@Override long time,
                                   @Override int symbol,
                                   @Override int high,
                                   @Override int last,
                                   @Override int low)
            implements MarketInfo { // Does *not* implement MarketInfoUpdater!

        @Override
        public String toString() {
            return Util.toString(this);
        }
    }

    static final RecordMapper<MarketInfoRecord>
            MARKET_INFO_RECORD_MAPPER = new RecordMapper<>() {

        @Override
        public MarketInfoRecord get(MemorySegment s) {
            return new MarketInfoRecord(
                    (long) Util.TIME.get(s, 0L),
                    (int) Util.SYMBOL.get(s, 0L),
                    (int) Util.HIGH.get(s, 0L),
                    (int) Util.LAST.get(s, 0L),
                    (int) Util.LOW.get(s, 0L));
        }

        @Override
        public void set(MemorySegment s, MarketInfoRecord v) {
            Util.TIME.set(s, 0L, v.time());
            Util.SYMBOL.set(s, 0L, v.symbol());
            Util.HIGH.set(s, 0L, v.high());
            Util.LAST.set(s, 0L, v.last());
            Util.LOW.set(s, 0L, v.low());
        }

        @Override
        public MemoryLayout layout() {
            return Util.MARKET_INFO;
        }
    };

    public static void main(String[] args) {
        MarketInfoRecord mi;
        try (var arena = Arena.ofConfined()) {
            var seg = arena.allocate(Util.MARKET_INFO);

            mi = new MarketInfoRecord(
                    24_05_08__11_15_23L,
                    Util.symbolAsInt("ORCL"),
                    107,
                    106,
                    104
            );

            System.out.println(mi);
            // MarketInfo{time = 231006111523, symbol = ORCL, high = 107, last = 106, low = 104}

            MARKET_INFO_RECORD_MAPPER.set(seg, mi);
            System.out.println(Util.toHex(seg));
            // a3 8a 66 ff 37 00 00 00 4f 52 43 4c 6b 00 00 00 6a 00 00 00 68 00 00 00
            // | time=231006111523    | O  R  C  L| high=107  | last=106  | low=104  |

            // Read back from the segment
            var mi2 = MARKET_INFO_RECORD_MAPPER.get(seg);
            System.out.println(mi2);
            // Same as above

            System.out.println("mi.equals(mi2) = " + mi.equals(mi2));
            // true

            // 🤩 Thread safety
            Thread.ofPlatform().start(() -> System.out.println(mi));
        }

        // 🤩 Holds no backing segment
        System.out.println(mi);
    }

}