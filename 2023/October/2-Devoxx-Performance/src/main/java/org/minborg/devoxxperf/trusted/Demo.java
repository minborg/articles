package org.minborg.devoxxperf.trusted;

public class Demo {

    public static final Point ORIGIN = new RecordPoint(0, 0);

    public static void main(String[] args) {
        analyze(new RegularPoint(0, 0));
        analyze(new RegularPoint(1, 1));
    }

    public static void analyze(Point point) {
        System.out.format("The point %s is %s at the origin.%n",
                point, isOrigin(point) ? "" : "not");
    }

    public static boolean isOrigin(Point point) {
        return point.x() == ORIGIN.x() &&
                point.y() == ORIGIN.y();
    }

}
