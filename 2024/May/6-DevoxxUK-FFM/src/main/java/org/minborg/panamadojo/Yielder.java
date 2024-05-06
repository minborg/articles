package org.minborg.panamadojo;

import java.util.concurrent.locks.LockSupport;

@FunctionalInterface
public interface Yielder {

    void yield();

    static Yielder ofBusy() {
        return Thread::onSpinWait;
    }

    static Yielder ofYield() {
        return Thread::yield;
    }

    static Yielder ofSleep(long nanos) {
        return () -> LockSupport.parkNanos(nanos);
    }

}
