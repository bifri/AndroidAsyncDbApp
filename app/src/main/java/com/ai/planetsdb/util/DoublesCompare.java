package com.ai.planetsdb.util;

public class DoublesCompare {

    public final static double EPSILON = 1E-6;


    /**
     * Returns true if two doubles are considered equal. Tests if the absolute
     * difference between two doubles has a difference less than EPSILON.
     *
     * @param a double to compare
     * @param b double to compare
     * @return true if two doubles are considered equal
     */
    public static boolean equals(double a, double b) {
        return equals(a, b, EPSILON);
    }


    /**
     * Returns true if two doubles are considered equal. Tests if the absolute
     * difference between the two doubles has a difference less than a given
     * double (epsilon).
     *
     * @param a       double to compare
     * @param b       double to compare
     * @param epsilon double which is compared to the absolute difference of two
     *                doubles to determine if they are equal
     * @return true if two doubles are considered equal
     */
    public static boolean equals(double a, double b, double epsilon) {
        return a == b || Math.abs(a - b) < epsilon;
    }


    /**
     * Returns true if the first double is considered greater than the second
     * double. Tests if the difference of first minus second is greater than
     * EPSILON.
     *
     * @param a first double
     * @param b second double
     * @return true if the first double is considered greater than the second
     * double
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean greaterThan(double a, double b) {
        return greaterThan(a, b, EPSILON);
    }


    /**
     * Returns true if the first double is considered greater than the second
     * double. Tests if the difference of first minus second is greater than
     * a given double (epsilon).
     *
     * @param a first double
     * @param b second double
     * @return true if the first double is considered greater than the second
     * double
     */
    public static boolean greaterThan(double a, double b, double epsilon) {
        return a - b > epsilon;
    }


    /**
     * Returns true if the first double is considered less than the second
     * double. Tests if the difference of second minus first is greater than
     * EPSILON.
     *
     * @param a first double
     * @param b second double
     * @return true if the first double is considered less than the second
     * double
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean lessThan(double a, double b) {
        return lessThan(a, b, EPSILON);
    }


    /**
     * Returns true if the first double is considered less than the second
     * double. Tests if the difference of second minus first is greater then
     * a given double (epsilon).
     *
     * @param a first double
     * @param b second double
     * @return true if the first double is considered less than the second
     * double
     */
    public static boolean lessThan(double a, double b, double epsilon) {
        return b - a > epsilon;
    }
}