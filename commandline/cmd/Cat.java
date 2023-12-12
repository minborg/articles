package cmd;

import cmd.Cat.MyOption.Help;
import cmd.Cat.MyOption.InputFile;
import cmd.Cat.MyOption.Limit;
import cmd.Cat.MyOption.OutputFile;
import cmd.Cat.MyOption.Verbose;
import cmd.builtin.CommandLineParser;
import cmd.builtin.NonOption;
import cmd.builtin.Option;

import java.nio.file.Path;
import java.util.List;

import static cmd.Cat.MyOption.*;

// With just two static methods and two annotation classes, it is possible
// to roll a powerful command line parser using Algebraic Data Types and
// pattern matching in Java.

public class Cat {

    // Model a "cat"-like command tool
    sealed interface MyOption {
        @Option(required = true)
        record InputFile(Path path) implements MyOption {}

        @Option(required = true, unique = true)
        record OutputFile(Path path) implements MyOption {}

        record Limit(int maxLines) implements MyOption {}
        record Verbose() implements MyOption {}
        record Help() implements MyOption {}

        @Option(shortName = "b") // Begins with an H like Help
        record HyperbolicMode() implements MyOption {}

        @NonOption
        record Parameter(String text) implements MyOption {}
    }

    // java -jar cat.jar -i in.txt -verbose -input-file in2.txt -o out.txt foo bar
    //
    // Found input in.txt
    // Verbose mode on
    // Found input in2.txt
    // Output is out.txt
    // Parameter foo
    // Parameter bar

    void main(String[] args) {

        List<MyOption> parsedArgs = CommandLineParser.parse(MyOption.class, args);
        // InputFile[path=in.txt],
        // Verbose[],
        // InputFile[path=in2.txt],
        // OutputFile[path=out.txt],
        // Parameter[text=foo],
        // Parameter[text=bar]

        for (MyOption o : parsedArgs) {
            switch (o) {
                case InputFile(var path)  -> System.out.println("Found input " + path);
                case OutputFile(var path) -> System.out.println("Output is " + path);
                case Limit(int max)       -> System.out.println("Limit found " + max);
                case Verbose __           -> System.out.println("Verbose mode on");
                case Help __              -> System.out.println("Usage: ...");
                case HyperbolicMode __    -> System.out.println("Wroom!!");
                case Parameter(var text)  -> System.out.println("Parameter " + text);
            }
        }

        // Lookup
        if (parsedArgs.stream().anyMatch(InputFile.class::isInstance)) {
            System.out.println("As we already know, there is at least one input file");
        }

        // Looking for specific options
        parsedArgs.stream()
                .filter(InputFile.class::isInstance)
                .forEach(System.out::println);
        // InputFile[path=in.txt]
        // InputFile[path=in2.txt]

        // Help
        CommandLineParser.help(MyOption.class)
                .forEach(System.out::println);
        // Usage:
        //  -i, --input-file       path (one or more is required)
        //  -o, --output-file      path (exactly one is required)
        //  -l, --limit            max  (numeric)
        //  -v, --verbose
        //  -h, --help
        //  -b, --hyperbolic-mode
        //  parameter ...
    }
}
