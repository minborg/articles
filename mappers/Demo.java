import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandles;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Demo {

    public static void main(String[] args) {
        var layout = MemoryLayout.structLayout(JAVA_INT.withName("x"), JAVA_INT.withName("y"));

        MethodHandles.publicLookup()

    }

}
