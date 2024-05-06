package src;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Demo3 {

    // Arenas
    public static void main(String[] args) {

        // Arena                Lifetime           Access
        Arena.global();      // Unbounded          multi-threaded
        Arena.ofAuto();      // Auto bounded (GC)  multi-threaded

        Arena.ofConfined();  // Explicit (close)   single-threaded
        Arena.ofShared();    // Explicit (close)   multi-threaded

        // When an arena is closed,
        //     all of its segments are atomically invalidated
        // Closing a shared arena triggers
        //     a thread-local handshake (JEP 312)

        // Anyone can implement a custom Arena

        // An arena that prints debug information to the console
        final class DebugArena implements Arena {

            private final Arena delegate;

            public DebugArena(Arena delegate) {
                this.delegate = delegate;
            }

            @Override
            public MemorySegment allocate(long byteSize,
                                          long byteAlignment) {
                System.out.println("Allocating " + byteSize + " bytes");
                return delegate.allocate(byteSize, byteAlignment);
            }

            @Override
            public MemorySegment.Scope scope() {
                return delegate.scope();
            }

            @Override
            public void close() {
                System.out.println("Closed!");
                delegate.close();
            }
        }

        try (var myArena = new DebugArena(Arena.ofConfined())) {
            var mySegment = myArena.allocate(Demo2.Point2D.LAYOUT);
            // "Allocating 16 bytes"

            // Do stuff ...

        } // "Closed!"
    }
}
