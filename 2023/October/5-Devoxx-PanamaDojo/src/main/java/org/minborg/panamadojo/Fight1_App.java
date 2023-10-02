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
            Paths.get("market-info", "20231006.queue");

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
                new MarketInfoRecord(23_10_06__11_15_23L, ORCL, 107, 106, 104),
                new MarketInfoRecord(23_10_06__11_15_25L, AAPL, 172, 170, 169),
                new MarketInfoRecord(23_10_06__11_15_27L, MSFT, 313, 310, 311)
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
                        .map(mi -> Thread.currentThread().getName() + " : " + mi) // Optional<String>
                        .ifPresentOrElse(System.out::println, yielder::yield);
            }
        }

    }

  /*
  Starting
  // 1 second (+ 2.5 us)
  consumer one : MarketInfo{time = 231006111523, symbol = ORCL, high = 107, last = 106, low = 104}
  consumer two : MarketInfo{time = 231006111523, symbol = ORCL, high = 107, last = 106, low = 104}
  // 2 seconds
  consumer one : MarketInfo{time = 231006111525, symbol = AAPL, high = 172, last = 170, low = 169}
  consumer two : MarketInfo{time = 231006111525, symbol = AAPL, high = 172, last = 170, low = 169}
  // 2 seconds
  consumer two : MarketInfo{time = 231006111527, symbol = MSFT, high = 313, last = 310, low = 311}
  consumer one : MarketInfo{time = 231006111527, symbol = MSFT, high = 313, last = 310, low = 311}
  */

}
