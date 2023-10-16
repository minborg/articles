import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Demo {

    public static void main(String[] args) {
        var layout = MemoryLayout.structLayout(JAVA_INT.withName("x"), JAVA_INT.withName("y"));
        record Point(long x, long y) {
        }

        var v = BigInteger.valueOf(1L << 40);

        System.out.println(v);

        var mapper = Builder.ofRecord(Point.class, layout)
                .mapping(long.class, BigInteger.class, BigInteger::valueOf)
                .toMapper();

    }

    interface Builder<T extends Record> {

        Builder<T> withLookup(MethodHandles.Lookup lookup);

        Builder<T> withWidening();

        Builder<T> withNarrowing();

        Builder<T> withBoxing();

        Builder<T> withUnboxing();

        Builder<T> withStringConversion();

        Builder<T> withAllConversions();

        Builder<T> withExactNarrowing();

        // General type mapping
        <S, R> Builder<T> mapping(Class<S> sourceType, Class<R> targetType, Function<? super S, ? extends R> mapper);

        <S, R> Builder<T> mapping(Class<S> sourceType, Class<R> targetType, MethodHandle mapper);

        // Name mapping
        <R> Builder<T> matching(MethodReference<T, R> recordComponent, MemoryLayout.PathElement valueLayout);

        <R> Builder<T> matching(MethodReference<T, R> recordComponent, SequencedCollection<MemoryLayout.PathElement> valueLayout);

        // Custom name and type mapping (`MethodHandle` overloads not shown for brevity)
        <R> Builder<T> mapping(MethodReference<T, R> recordComponentSource,
                               MemoryLayout.PathElement valueLayoutTarget,
                               Function<? super T, ? extends R> mapper);

        <R> Builder<T> mapping(MethodReference<T, R> recordComponentSource,
                               SequencedCollection<MemoryLayout.PathElement> valueLayoutTarget,
                               Function<? super T, ? extends R> mapper);

        <R> Builder<T> mapping(MemoryLayout.PathElement valueLayoutSource,
                               MethodReference<T, R> recordComponentTarget,
                               Function<?, ? extends R> mapper);

        <R> Builder<T> mapping(SequencedCollection<MemoryLayout.PathElement> valueLayoutSource,
                               MethodReference<T, R> recordComponentTarget,
                               Function<?, ? extends R> mapper);


        RecordMapper<T> toMapper();

        Function<MemorySegment, T> toUnmarshaller();

        BiConsumer<MemorySegment, T> toMarshaller();


        static <T extends Record> Builder<T> ofRecord(Class<T> type, GroupLayout layout) {
            Objects.requireNonNull(type);
            if (Record.class.equals(type)) {
                throw new IllegalArgumentException();
            }
            Objects.requireNonNull(layout);
            return new BuilderImpl<T>(type, layout) {
            };
        }
    }

    interface RecordMapper<T extends Record> {

    }

    private static abstract class BuilderImpl<T extends Record> implements Builder<T> {

        private final Class<T> type;
        private final GroupLayout layout;

        public BuilderImpl(Class<T> type, GroupLayout layout) {
            this.type = type;
            this.layout = layout;
        }

        @Override
        public <S, R> Builder<T> mapping(Class<S> sourceType, Class<R> targetType, Function<? super S, ? extends R> mapper) {
            return null;
        }

        @Override
        public RecordMapper<T> toMapper() {
            return null;
        }

        @Override
        public Builder<T> withLookup(MethodHandles.Lookup lookup) {
            return null;
        }

        @Override
        public Builder<T> withWidening() {
            return null;
        }

        @Override
        public Builder<T> withNarrowing() {
            return null;
        }

        @Override
        public Builder<T> withBoxing() {
            return null;
        }

        @Override
        public Builder<T> withUnboxing() {
            return null;
        }

        @Override
        public Builder<T> withStringConversion() {
            return null;
        }

        @Override
        public Builder<T> withAllConversions() {
            return null;
        }

        @Override
        public Builder<T> withExactNarrowing() {
            return null;
        }

        @Override
        public <S, R> Builder<T> mapping(Class<S> sourceType, Class<R> targetType, MethodHandle mapper) {
            return null;
        }

        @Override
        public <R> Builder<T> matching(MethodReference<T, R> recordComponent, MemoryLayout.PathElement valueLayout) {
            return null;
        }

        @Override
        public <R> Builder<T> matching(MethodReference<T, R> recordComponent, SequencedCollection<MemoryLayout.PathElement> valueLayout) {
            return null;
        }

        @Override
        public <R> Builder<T> mapping(MethodReference<T, R> recordComponentSource, MemoryLayout.PathElement valueLayoutTarget, Function<? super T, ? extends R> mapper) {
            return null;
        }

        @Override
        public <R> Builder<T> mapping(MethodReference<T, R> recordComponentSource, SequencedCollection<MemoryLayout.PathElement> valueLayoutTarget, Function<? super T, ? extends R> mapper) {
            return null;
        }

        @Override
        public <S, R> Builder<T> mapping(MemoryLayout.PathElement valueLayoutSource, MethodReference<T, R> recordComponentTarget, Function<?, ? extends R> mapper) {
            return null;
        }

        @Override
        public <S, R> Builder<T> mapping(SequencedCollection<MemoryLayout.PathElement> valueLayoutSource, MethodReference<T, R> recordComponentTarget, Function<?, ? extends R> mapper) {
            return null;
        }

        @Override
        public Function<MemorySegment, T> toUnmarshaller() {
            return null;
        }

        @Override
        public BiConsumer<MemorySegment, T> toMarshaller() {
            return null;
        }
    }

    interface MethodReference<T, R> extends Function<T, R> {
        Method method();
    }


}
