package org.minborg.panamadojo;

interface MarketInfo {
    long time();      // e.g. 23_10_06__11_15_23L
    int symbol();     // e.g. "ORCL" -> 4f 52 43 4c
    int high();       // e.g. 107
    int last();       // e.g. 106
    int low();        // e.g. 104
}
