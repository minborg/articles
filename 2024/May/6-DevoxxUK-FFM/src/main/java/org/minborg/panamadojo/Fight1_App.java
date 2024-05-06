package org.minborg.panamadojo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.minborg.panamadojo.Kata2_RecordMapper.*;
import static org.minborg.panamadojo.Kata2_RecordMapper.MARKET_INFO_RECORD_MAPPER;

public class Fight1_App {

    private static final Path QUEUE_PATH =
            Paths.get("market-info", "20240508.queue");

    private static final int ORCL = Util.symbolAsInt("ORCL");
    private static final int AAPL = Util.symbolAsInt("AAPL");
    private static final int MSFT = Util.symbolAsInt("MSFT");

    public static void main(String[] args) {
        System.out.println("Starting");

        Thread.ofPlatform().name("producer").start(Fight1_App::producer);

        // Give the producer some time to start ...
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));

        Thread.ofVirtual().name("consumer one").start(Fight1_App::consumer);
        Thread.ofVirtual().name("consumer two").start(Fight1_App::consumer);
    }

    static void producer() {
        var events = List.of(
                new MarketInfoRecord(24_05_08__11_15_23L, ORCL, 107, 106, 104),
                new MarketInfoRecord(24_05_08__11_15_25L, AAPL, 172, 170, 169),
                new MarketInfoRecord(24_05_08__11_15_27L, MSFT, 313, 310, 311)
        );

        try (var producer = QueueProducer.of(MARKET_INFO_RECORD_MAPPER, QUEUE_PATH)) {
            for (var mi : events) {
                producer.append(mi);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(2));
            }
        }
    }

    static void consumer() {
        Yielder yielder = Yielder.ofSleep(TimeUnit.MICROSECONDS.toNanos(5)); // 5 us
        try (var consumer = QueueConsumer.of(MARKET_INFO_RECORD_MAPPER, QUEUE_PATH)) {
            for (;;) {
                consumer.next() // Optional<MarketInfoRecord>
                        .ifPresentOrElse(Fight1_App::onRead, yielder::yield);
            }
        }

    }

    static void onRead(MarketInfo event) {
        System.out.println(Thread.currentThread().getName() + " : " + event);

        // MAP.merge(event.symbol(), event, (e, n) -> n.time() > e.time() ? n : e);

        /*
        if (event.symbol() == Util.symbolAsInt("ORCL")) {
            outQueue.append(event);
        }
         */
    }

  /*
  Starting
  // 1 second (+ 2.5 us)
  consumer two : MarketInfo{time = 240508111523, symbol = ORCL, high = 107, last = 106, low = 104}
  consumer one : MarketInfo{time = 240508111523, symbol = ORCL, high = 107, last = 106, low = 104}
  // 2 seconds
  consumer one : MarketInfo{time = 240508111525, symbol = AAPL, high = 172, last = 170, low = 169}
  consumer two : MarketInfo{time = 240508111525, symbol = AAPL, high = 172, last = 170, low = 169}
  // 2 seconds
  consumer two : MarketInfo{time = 240508111527, symbol = MSFT, high = 313, last = 310, low = 311}
  consumer one : MarketInfo{time = 240508111527, symbol = MSFT, high = 313, last = 310, low = 311}


  hexdump -C market-info/20240508.queue

00000000  c0 00 00 00 01 00 00 00  a3 8a 66 ff 37 00 00 00  |..........f.7...|
00000010  4f 52 43 4c 6b 00 00 00  6a 00 00 00 68 00 00 00  |ORCLk...j...h...|
00000020  c0 00 00 00 02 00 00 00  a5 8a 66 ff 37 00 00 00  |..........f.7...|
00000030  41 41 50 4c ac 00 00 00  aa 00 00 00 a9 00 00 00  |AAPL............|
00000040  c0 00 00 00 03 00 00 00  a7 8a 66 ff 37 00 00 00  |..........f.7...|
00000050  4d 53 46 54 39 01 00 00  36 01 00 00 37 01 00 00  |MSFT9...6...7...|
00000060  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|

*
00100000

  */

}
