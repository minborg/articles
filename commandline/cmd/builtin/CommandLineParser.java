package cmd.builtin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class CommandLineParser {

    private CommandLineParser() {}

    /**
     * {@return the parsed command line arguments into a list of instances of the
     *          provided sealed interface {@code type}}
     *
     * @implSpec The method will inspect all the {@linkplain Class#getPermittedSubclasses()}
     *           of the given {@code type} and derive default or explicit metadata after
     *           which the actual parsing will take place.
     *
     * @param type to parse the args into
     * @param args to parse
     * @param <T>  parser type into which to parse
     * @throws IllegalArgumentException if the provided {@code type}
     *         is not and interface, is a hidden interface or is
     *         not a sealed interface.
     * @throws IllegalArgumentException if unable to parse
     */
    public static <T> List<T> parse(Class<T> type, String[] args) {
        assertInterfaceInvariants(type);
        Arrays.stream(args)
                .forEach(Objects::requireNonNull);
        return List.of();
    }

    public static Stream<String> help(Class<?> type) {
        assertInterfaceInvariants(type);
        return Stream.empty();
    }





    private static void assertInterfaceInvariants(Class<?> type) {
        Objects.requireNonNull(type);
        if (!type.isInterface()) {
            throw newIae(type, "not an interface");
        }
        if (type.isHidden()) {
            throw newIae(type, "a hidden interface");
        }
        if (!type.isSealed()) {
            throw newIae(type, "not a sealed interface");
        }
    }

    private static IllegalArgumentException newIae(Class<?> type, String trailingInfo) {
        return new IllegalArgumentException(type.getName() + " is " + trailingInfo);
    }

}
