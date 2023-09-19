package org.minborg.devoxxperf.trusted;

public final class RegularPoint implements Point {

    private final int x;
    private final int y;

    public RegularPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    // Implementations of toString(), hashCode() and equals() not shown

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegularPoint that = (RegularPoint) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "RegularPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
