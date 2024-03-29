package org.minborg.panamadojo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public class Kata1_BeanWrapper {

    public static final class MarketInfoSegmentBean
            implements MarketInfo, MarketInfoUpdater {

        private final MemorySegment segment;

        public MarketInfoSegmentBean(MemorySegment segment) {
            this.segment = segment;
        }

        @Override
        public long time() {
            return (long) Util.TIME.get(segment);
        }

        @Override
        public int symbol() {
            return (int) Util.SYMBOL.get(segment);
        }

        @Override
        public int high() {
            return (int) Util.HIGH.get(segment);
        }

        @Override
        public int last() {
            return (int) Util.LAST.get(segment);
        }

        @Override
        public int low() {
            return (int) Util.LOW.get(segment);
        }

        @Override
        public void time(long time) {
            Util.TIME.set(segment, time);
        }

        @Override
        public void symbol(int symbol) {
            Util.SYMBOL.set(segment, symbol);
        }

        @Override
        public void high(int high) {
            Util.HIGH.set(segment, high);
        }

        @Override
        public void last(int last) {
            Util.LAST.set(segment, last);
        }

        @Override
        public void low(int low) {
            Util.LOW.set(segment, low);
        }

        @Override
        public String toString() {
            return Util.toString(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MarketInfo that &&
                    this.time() == that.time() &&
                    this.symbol() == that.symbol() &&
                    this.high() == that.high() &&
                    this.last() == that.last() &&
                    this.low() == that.low();
        }

        @Override
        public int hashCode() {
            return Objects.hash(time(), symbol(), high(), last(), low());
        }

    }

    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            var seg = arena.allocate(Util.MARKET_INFO);
            var mi = new MarketInfoSegmentBean(seg);

            System.out.println(mi);
            // MarketInfo{time = 0, symbol = 0000, high = 0, last = 0, low = 0}

            mi.time(23_10_06__11_15_23L);
            mi.symbol(Util.symbolAsInt("ORCL"));
            mi.high(107);
            mi.last(106);
            mi.low(104);

            System.out.println(mi);
            // MarketInfo{time = 231006111523, symbol = ORCL, high = 107, last = 106, low = 104}

            System.out.println(Util.toHex(seg));
            // 23 87 09 c9 35 00 00 00 4f 52 43 4c 6b 00 00 00 6a 00 00 00 68 00 00 00
            // | time=231006111523    | O  R  C  L| high=107  | last=106  | low=104  |

            // ☹️ Thread safety
            Thread.ofPlatform().start(() -> System.out.println(mi));
            // Exception in thread "Thread-0" java.lang.WrongThreadException

        }  // Free the segment

        // ☹️ Holds the backing segment
        // System.out.println(mi)
    }
}
