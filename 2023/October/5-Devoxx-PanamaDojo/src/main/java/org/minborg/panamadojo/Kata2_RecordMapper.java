package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Kata2_RecordMapper {

    public record MarketInfoRecord(long time, int symbol, int high, int last, int low)
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
                    (long) Util.TIME.get(s),
                    (int) Util.SYMBOL.get(s),
                    (int) Util.HIGH.get(s),
                    (int) Util.LAST.get(s),
                    (int) Util.LOW.get(s));
        }

        @Override
        public void set(MemorySegment s, MarketInfoRecord v) {
            Util.TIME.set(s, v.time());
            Util.SYMBOL.set(s, v.symbol());
            Util.HIGH.set(s, v.high());
            Util.LAST.set(s, v.last());
            Util.LOW.set(s, v.low());
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
                    23_10_06__11_15_23L,
                    Util.symbolAsInt("ORCL"),
                    107,
                    106,
                    104
            );

            System.out.println(mi);
            // MarketInfo{time = 231006111523, symbol = ORCL, high = 107, last = 106, low = 104}

            MARKET_INFO_RECORD_MAPPER.set(seg, mi);
            System.out.println(Util.toHex(seg));
            // 23 87 09 c9 35 00 00 00 4f 52 43 4c 6b 00 00 00 6a 00 00 00 68 00 00 00
            // | time=231016111523    | O  R  C  L| high=107  | last=106  | low=104  |

            // Read back from the segment
            var mi2 = MARKET_INFO_RECORD_MAPPER.get(seg);
            System.out.println(mi2);

            System.out.println("mi.equals(mi2) = " + mi.equals(mi2));
            // true

            // 🤩 Thread safety
            Thread.ofPlatform().start(() -> System.out.println(mi));
        }

        // 🤩 Holds no backing segment
        System.out.println(mi);
    }

}
