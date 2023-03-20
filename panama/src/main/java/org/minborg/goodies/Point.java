package org.minborg.goodies;

import java.lang.foreign.Arena;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.util.List;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Point extends AbstractSegmentWrapper {

        private static final StructLayout POINT = structLayout(
                JAVA_INT.withName("x"),
                JAVA_INT.withName("y")
        ).withName("point");

        private static final VarHandle X = POINT.varHandle(groupElement("x"))
                .withInvokeExactBehavior();
        private static final VarHandle Y = POINT.varHandle(groupElement("y"))
                .withInvokeExactBehavior();

        private static final List<VarHandle> VAR_HANDLES = List.of(X, Y);

        public Point(Arena arena) {
            super(arena);
        }

        @Override
        protected StructLayout layout() {
            return POINT;
        }

        @Override
        protected List<VarHandle> varHandles() {
            return VAR_HANDLES;
        }

        int x() {
            return (int) X.get(segment());
        }

        int y() {
            return (int) Y.get(segment());
        }

        void x(int x) {
            X.set(segment(), x);
        }

        void y(int y) {
            Y.set(segment(), y);
        }

    }