package src;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Demo5 {

    // Future community efforts ...
    public static void main(String[] args) {

        record Point2D(double x, double y){}

        var points = MemorySegment.ofArray(new double[]{3.0, 4.0, 5.0, 6.0});

        RecordMapper<Point2D> mapper =
                RecordMapper.of(Point2D.class, Demo2.Point2D.LAYOUT);

        Point2D firstPoint =
                mapper.get(points, 0); // Point2D[3.0, 4.0]

        Point2D secondPoint =
                mapper.getAtIndex(points, 1); // Point2D[5.0, 6.0]
    }














    interface RecordMapper<T extends Record> {

        T get(MemorySegment segment, long offset);

        T getAtIndex(MemorySegment segment, long index);

        static <T extends Record> RecordMapper<T> of(Class<T> type, MemoryLayout layout) {
            return new RecordMapper<T>() {
                @Override
                public T get(MemorySegment segment, long offset) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public T getAtIndex(MemorySegment segment, long index) {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }

}
