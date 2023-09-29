package org.minborg.panamadojo;

interface MarketInfo {
    long time();
    int symbol();
    int high();
    int last();
    int low();
}
