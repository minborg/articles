package org.minborg.jep442;


/**
 * A
 */
public class MarkWord {

/*
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    */
/**
     * A
     * @param args a
     *//*

    public static void main(String[] args) {

        Foo foo = new Foo();

        printMark(foo);
        synchronized (foo) {
            printMark(foo);
        }
        printMark(foo);
    }

    static void printMark(Object o) {
        int[] mark = new int[3];
        for (int i = 0; i < mark.length; i++) {
            mark[i] = UNSAFE.getInt(o, i * Integer.BYTES);
        }

        var hex = Arrays.stream(mark)
                .boxed()
                .map(i -> Integer.toHexString(i))
                .collect(Collectors.joining(", "));

        System.out.println(hex);
    }

    static final class Foo {
    }
*/

}
