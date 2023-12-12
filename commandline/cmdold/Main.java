package cmdold;

import java.util.Optional;

public class Main {

    // This is done under the covers
    public static void main(String[] args) {
        // Parse the args
        main(null, new Alpha(), new Config(), new Config());
    }

    // Exposed main met
    void main(Lookup lookup, MyCla... args) {
        boolean verbose = false;
        for (MyCla cla : args) {
            switch (cla) {
                case Alpha __ -> System.out.println("Alpha activated");
                case Config c -> System.out.println("Config found " + lookup.get(c));
                case Verbose __ -> verbose = true;
                case Help __ -> System.out.print("HELP");
            }
        }

        if (lookup.contains(Config.class)) {
            System.out.println("Looks like there is at least one Config arg");
        }

        // All the config parameter values
        lookup.stream(Config.class)
                .map(lookup::get)
                .forEach(System.out::println);

    }


    public sealed interface MyCla extends Cla {
    }

    public record Help(String shortName, Optional<String> name) implements MyCla {
        public Help() {
            this("h", Optional.of("help"));
        }
    }

    public static final class Verbose implements MyCla {
        @Override
        public String shortName() {
            return "v";
        }
    }

    public static final class Alpha extends AbstractCla implements MyCla {
        public Alpha() {
            super("a", "alpha", "Activate feature alpha");
        }
    }

    public static final class Config extends AbstractCla implements MyCla {
        public Config() {
            super("c", "config", "Set config file",
                    true, 10);
        }
    }

}
