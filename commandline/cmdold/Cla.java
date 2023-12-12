package cmdold;

import java.util.Optional;

public interface Cla {

    String shortName();

    default Optional<String> name() {
        return Optional.empty();
    }

    default Optional<String> description() {
        return Optional.empty();
    }

    default boolean required() {
        return true;
    }

    default int maxCardinality() {
        return 1;
    }

/*    default int index() {
        return 0;
    }*/

/*    String value();*/

}
