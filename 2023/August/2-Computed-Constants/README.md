# Java: JEP Draft: "Computed Constants"

We finally made the draft [JEP "Computed Constants"](https://openjdk.org/jeps/8312611) public and I can't wait to tell you more about it! `ComputedConstant` objects are superfast immutable value holders that can be initialized independently of when they are created. As an added benefit, these objects may in the future be even more optimized via ["condensers"](https://openjdk.org/projects/leyden/notes/03-toward-condensers) that eventually might become available through project [Leyden](https://openjdk.org/projects/leyden/).

## Background

Oftentimes, we use `static` fields to hold objects that are only initialized once:

```java
// ordinary static initialization
private static final Logger LOGGER = Logger.getLogger("com.foo.Bar");
...
LOGGER.log(...);
```
The `LOGGER` variable will be unconditionally initialized as soon as the class where it is declared is loaded (loading occurs upon the class being first referenced).

The [class holder idiom](https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom) allows us to defer initialization until we actually need the variable:

```java
// Initialization-on-demand holder idiom
Logger logger() {
    class Holder {
         static final Logger LOGGER = Logger.getLogger("com.foo.Bar");
    }
    return Holder.LOGGER;
}
...
logger().log(...);
```

While this works well in theory, there are significant drawbacks:
 * Each constant that needs to be decoupled would need _its own_ holding class (adding static footprint overhead)
 * Only works if the decoupled constants are independent
 * Does only work for `static` variables and not for instance variables and objects

Another way is to use the  [double-checked locking idiom](https://en.wikipedia.org/wiki/Double-checked_locking) that can also be used for deferring initialization and, it works for both `static` variables, instance variables and objects:

```java
// Double-checked locking idiom
class Foo {
    
    private volatile Logger logger;
    
    public Logger logger() {
        Logger v = logger;
        if (v == null) {
            synchronized (this) {
                v = logger;
                if (v == null) {
                    logger = v = Logger.getLogger("com.foo.Bar");
                }
            }
        }
        return v;
    }
}
...
    foo.logger().log(...);
```
There is no way for the (current) JVM to determine that the `logger` is _monotonic_ in the sense that it can only change from `null` to a value _once_ and so, the JVM is unable to apply constant folding and other optimizations. Also, because `logger` needs to be declared `volatile` there is a small performance penalty paid for each access. 

The `ComputedConstant` class comes to the rescue here and offers the best of two worlds: Flexible initialization and good performance!

## Computed Constant

Here is how `ComputedConstant` can be used with the logger example:

```java
class Bar {
    // 1. Declare a computed constant value
    private static final ComputedConstant<Logger> LOGGER =
            ComputedConstant.of( () -> Logger.getLogger("com.foo.Bar") );

    static Logger logger() {
        // 2. Access the computed value 
        //    (evaluation made before the first access)
        return LOGGER.get();
    }
}
```

This is similar in spirit to the class-holder idiom, and offers the same performance, constant-folding, and thread-safety characteristics, but is simpler and incurs a lower static footprint since no additional class is required.

## Benchmarks

I've run some benchmarks on my Mac Pro M1 ARM-based machine and preliminary results indicates excellent performance for `ComputedConstant` static fields:

```text
Benchmark                                    Mode  Cnt  Score   Error  Units
ComputedConstantStatic.staticHolder          avgt   15  0.561 ? 0.002  ns/op
ComputedConstantStatic.doubleChecked         avgt   15  1.122 ? 0.003  ns/op
ComputedConstantStatic.constant              avgt   15  0.563 ? 0.002  ns/op // static ComputedConstant
```

As can be seen, a `ComputedConstant` has the same performance as the static holder (but with no extra class footprint) and much better performance than a double-checked locking variable. 

## Collections of ComputedConstant

So far so good. However, the hidden gem in the JEP is the ability to obtain Collections of `ComputedConstant` elements. This is achieved using a factory method that provides not a single `ComputedConstant` with its provider but a whole `List` of `ComputedConstant` elements that is handled by a single providing mapper that can initialize all the elements in the list. This allows a large number of variables to be handled via a _single_ list, thereby saving space compared to having many single constants and initialization lambdas (for example). 

Like a `ComputedConstant<V>` variable, a `List<ComputedConstant<V>>` variable is created by providing an element mapper - typically in the form of a lambda expression, which is used to compute the value associated with the i-th element of the `List` when the element value is first accessed:

```java
class Fibonacci {
    static final List<ComputedConstant<Integer>> FIBONACCI =
            ComputedConstant.of(1_000, Fibonacci::fib);

    static int fib(int n) {
        return (n < 2)
                ? n
                : FIBONACCI.get(n - 1) + FIBONACCI.get(n - 2);
    }


    int[] fibs = IntStream.range(0, 10)
            .map(Fibonacci::fib)
            .toArray(); // { 0, 1, 1, 2, 3, 5, 8, 13, 21, 34 }

}
```
Note how there's only one field of type `List<ComputedConstant<Integer>>` to initialize - every other computation is performed on-demand when the corresponding element of the List `FIBONACCI` is accessed. 

When a computation depends on more sub-computations, it induces a dependency graph, where each computation is a node in the graph, and has zero or more edges to each of the sub-computation nodes it depends on. For instance, the dependency graph associated with `fib(5)` is given below:

```text
               ___________fib(5)___________
              /                            \
        ____fib(4)____                ____fib(3)____
       /              \              /              \
     fib(3)         fib(2)         fib(2)          fib(1)
    /      \       /      \       /      \    
  fib(2)  fib(1) fib(1)  fib(0) fib(1)  fib(0)
```

The Computed Constant API allows modeling this cleanly, while still preserving good constant-folding guarantees and integrity of updates in the case of multi-threaded access.

## Benchmarks Collections

These benchmarks were run on the same platform as above and shows collections of `ComputedConstant` elements enjoys the same performance benefits as the single ones do:

```text
Benchmark                                 Mode  Cnt  Score   Error  Units
ComputedConstantStaticList.staticHolder   avgt   15  0.570 ? 0.005  ns/op // int[] in a holder class
ComputedConstantStaticList.doubleChecked  avgt   15  1.124 ? 0.044  ns/op
ComputedConstantStaticList.constant       avgt   15  0.562 ? 0.005  ns/op // List<ComputedConstant>
```

Again, the `ComputedConstant` clocks in at native static array speed while providing much better flexibility as to when initialized.

## Instance Performance

The performance for instance variables and objects is superior to holders using the double-checked idiom showed above as can be seen in the benchmarks below:

```text
Benchmark                               Mode  Cnt  Score   Error  Units
ComputedConstantInstance.doubleChecked  avgt   15  1.259 ? 0.023  ns/op
ComputedConstantInstance.constant       avgt   15  0.728 ? 0.022  ns/op // ComputedConstant
```

So, `ComputedConstant` is more than 40% faster than the double-checked holder class tested on my machine.

## Resources

[JEP draft: Computed Constants](https://openjdk.org/jeps/8312611)
[Proposed ComputedConstant API](https://cr.openjdk.org/~pminborg/computed-constant/api/java.base/java/lang/ComputedConstant.html)
[Benchmarks](https://github.com/openjdk/leyden/blob/computed-constants/test/micro/org/openjdk/bench/java/lang/ComputedConstantStatic.java)

## Acknowledgements

Parts of the text in this article were written by Maurizio Cimadamore


