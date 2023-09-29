package org.minborg.panamadojo;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

final class Util {
    private Util() {}

    static final ValueLayout JAVA_INT_LE = JAVA_INT.withOrder(ByteOrder.LITTLE_ENDIAN);
    static final ValueLayout JAVA_LONG_LE = JAVA_LONG.withOrder(ByteOrder.LITTLE_ENDIAN);

    static final StructLayout MARKET_INFO = structLayout(
            JAVA_LONG_LE.withName("time"),
            JAVA_INT_LE.withName("symbol"),
            JAVA_INT_LE.withName("high"),
            JAVA_INT_LE.withName("last"),
            JAVA_INT_LE.withName("low")
    ).withName("MarketInfo");

    static final VarHandle TIME = varHandle("time");
    static final VarHandle SYMBOL = varHandle("symbol");
    static final VarHandle HIGH = varHandle("high");
    static final VarHandle LAST = varHandle("last");
    static final VarHandle LOW = varHandle("low");

    private static VarHandle varHandle(String fieldName) {
        return Util.MARKET_INFO.varHandle(PathElement.groupElement(fieldName));
    }

    public enum Symbol {
        ORCL, AAPL, IBM, MSFT, GOOG;
    }

    static int symbolAsInt(String symbol) {
        if (symbol.length() != 4) {
            throw new IllegalArgumentException("Unable to convert " + symbol + " to an int");
        }
        byte[] bytes = symbol.getBytes(StandardCharsets.US_ASCII);
        return (bytes[3] << 24) | (bytes[2] << 16) | (bytes[1] << 8) | (bytes[0] << 0);
    }

    static String symbolAsString(int symbol) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) ((symbol >>> 24) & 0xFF);
        bytes[2] = (byte) ((symbol >>> 16) & 0xFF);
        bytes[1] = (byte) ((symbol >>> 8) & 0xFF);
        bytes[0] = (byte) ((symbol >>> 0) & 0xFF);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    static String toString(MarketInfo mi) {
        return "MarketInfo{" +
                "time = " + mi.time() +
                ", symbol = " + symbolAsString(mi.symbol()) +
                ", high = " + mi.high() +
                ", last = " + mi.last() +
                ", low = " + mi.low() +
                "}";
    }

    static String toHex(MemorySegment seg) {
        var formatter = HexFormat.ofDelimiter(" ");
        return formatter.formatHex(seg.toArray(JAVA_BYTE));
    }

}



