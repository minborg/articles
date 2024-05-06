package org.minborg.panamadojo;

interface MarketInfoUpdater { // AKA "setters"
    void time(long time);
    void symbol(int symbol);
    void high(int high);
    void last(int last);
    void low(int low);
}
