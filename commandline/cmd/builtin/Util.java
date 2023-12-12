package cmd.builtin;

import java.util.Locale;

final class Util {

    private Util() {}


    static String longName(Class<?> key) {
        return camelToKebab(key.getSimpleName());
    }

    static String shortName(Class<?> key) {
        return key
                .getSimpleName()
                .substring(0, 1)
                .toLowerCase(Locale.ROOT);
    }

    private static String camelToKebab(String camel) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";

        return camel
                .replaceAll(regex, replacement)
                .toLowerCase(Locale.ROOT);

    }
}
