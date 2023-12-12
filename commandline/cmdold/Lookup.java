package cmdold;

import java.util.stream.Stream;

public interface Lookup /*<C extends Cla>*/ {

    boolean contains(Class<?/* extends C*/> keyType);

    <T /*extends C*/> Stream<T> stream(Class<T> keyType);

    String get(Cla key);

}
