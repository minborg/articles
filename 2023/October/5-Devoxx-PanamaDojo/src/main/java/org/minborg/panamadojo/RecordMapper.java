package org.minborg.panamadojo;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public interface RecordMapper<T extends Record> {

    T get(MemorySegment segment);

    void set(MemorySegment segment, T value);

    MemoryLayout layout();

    // To be JEPed...
    static <T extends Record> RecordMapper<T> of(Class<T> recordType, MemoryLayout layout) {
        throw new UnsupportedOperationException("Coming soon to a JEP near you!");
    }

}
