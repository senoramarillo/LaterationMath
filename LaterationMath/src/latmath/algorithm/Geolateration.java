package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Circle;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.util.Triangle;

/**
 * Geolateration algorithm.
 * <p>
 * Copyright (c) 2011 Thomas Hillebrandt.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited unless expressly permitted.
 *
 * @version 1.0, 2011-08-09
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class Geolateration extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "GEO";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return trilaterate(anchors, ranges);
    }
    
    /**
     * Returns a string representation of this lateration algorithm.
     *
     * @return A string representation of this lateration algorithm.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     * <p>
     * If more than 3 anchor nodes are passed to this method, the first
     * three anchors will be taken for localization.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d trilaterate(Point2d[] anchors, double[] ranges) {
        if (anchors == null || ranges == null || anchors.length < 3 ||
                anchors.length != ranges.length) {
            return null;
        }
        return trilaterate(anchors[0], ranges[0], anchors[1], ranges[1],
                anchors[2], ranges[2]);
    }

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     *
     * @param p1 The first anchor/reference node.
     * @param r1 The measured distance to the first anchor/reference node.
     * @param p2 The second anchor/reference node.
     * @param r2 The measured distance to the second anchor/reference node.
     * @param p3 The third anchor/reference node.
     * @param r3 The measured distance to the third anchor/reference node.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d trilaterate(Point2d p1, double r1, Point2d p2, double r2, Point2d p3, double r3) {
        // step 1: calculate circle intersections
        Point2d[] points1 = Circle.getIntersection(p1, r1, p2, r2);
        if (points1 == null) {
            // no intersection => try to get approximated intersection
            Point2d pft = Circle.getIntersectionApprox(p1, r1, p2, r2);
            if (pft == null) return null;
            points1 = new Point2d[] {pft};
        }
        Point2d[] points2 = Circle.getIntersection(p1, r1, p3, r3);
        if (points2 == null) {
            // no intersection => try to get approximated intersection
            Point2d pft = Circle.getIntersectionApprox(p1, r1, p3, r3);
            if (pft == null) return null;
            points2 = new Point2d[] {pft};
        }
        Point2d[] points3 = Circle.getIntersection(p2, r2, p3, r3);
        if (points3 == null) {
            // no intersection => try to get approximated intersection
            Point2d pft = Circle.getIntersectionApprox(p2, r2, p3, r3);
            if (pft == null) return null;
            points3 = new Point2d[] {pft};
        }

        // step 2: copy intersections into one array
        Point2d[] points = new Point2d[points1.length + points2.length + points3.length];
        System.arraycopy(points1, 0, points, 0, points1.length);
        System.arraycopy(points2, 0, points, points1.length, points2.length);
        System.arraycopy(points3, 0, points, points1.length + points2.length, points3.length);

        // step 2b: if there are 3 points which are very close together
        //          => no ranging error, take one of them as result
        int closeCount = 3;
        for (int i = 0; i < points.length; i++) {
            int currentCloseCount = 1;
            for (int j = 0; j < points.length; j++) {
                if (i != j && points[i].distance(points[j]) < 0.1) {
                    currentCloseCount++;
                }
            }
            if (currentCloseCount >= closeCount) {
                return points[i];
            }
        }

        // step 3: calculate triangle with minimum perimeter, also check for
        //         minimum triangle which lies inside of all circles.
        double[] ranges = new double[] {r1, r2, r3};
        Point2d[] anchors = new Point2d[] {p1, p2, p3};
        int min1 = -1, min2 = -1, min3 = -1;
        int minIn1 = -1, minIn2 = -1, minIn3 = -1;
        double minV = Double.MAX_VALUE, minInV = Double.MAX_VALUE;

        for (int i = 0; i < points.length - 2; i++) {
            for (int j = i + 1; j < points.length - 1; j++) {
                for (int k = j + 1; k < points.length; k++) {
                    double tmp = Triangle.perimeter(points[i], points[j], points[k]);
                    // test if new minimum triangle found
                    if (tmp < minV) {
                        minV = tmp;
                        min1 = i;
                        min2 = j;
                        min3 = k;
                    }
                    // test for minimum triangle which lies in all circles
                    if (Circle.pointInCircles(anchors, ranges, points[i]) &&
                            Circle.pointInCircles(anchors, ranges, points[j]) &&
                            Circle.pointInCircles(anchors, ranges, points[k])) {
                        if (tmp < minInV) {
                            minInV = tmp;
                            minIn1 = i;
                            minIn2 = j;
                            minIn3 = k;
                        }
                    }
                }
            }
        }

        // REMARK: the first implementation of this algorithm stopped at this
        //         point and returned the center of gravity of the minimum
        //         triangle as result (without finding minimum triangle which
        //         lies in all circles).

        // step 4: (small optimization) Test if center of gravity of the min
        //         triangle lies in the center of the triangle formed by the
        //         anchor nodes. Therefore, calculate incircle radius of the
        //         triangle reduced by some factor.
        Point2d pCoM = Point2d.centerOfMass(new Point2d[] {points[min1],
            points[min2], points[min3]});
        Point2d pAnchorCoM = Point2d.centerOfMass(anchors);
        double icr = 2 * Triangle.area(p1, p2, p3) / Triangle.perimeter(p1, p2, p3);
        if (pAnchorCoM.distance(pCoM) <= icr/2) {
            return pCoM;
        }

        // step 5: test if there is a minimum triangle which lies in all
        //         circles. If it's not the "real" minimum triangle then
        //         do some further optimization.
        if (minIn1 != -1) {
            boolean same = (min1 == minIn1 && min2 == minIn2 && min3 == minIn3);
            if (!same) {
                // calculate the area of both triangles
                double ta1 = Triangle.area(points[min1], points[min2], points[min3]);
                double ta2 = Triangle.area(points[minIn1], points[minIn2], points[minIn3]);

                // if the area of the second triangle is roughly equal to the
                // area of the minimum triangle, use the second triangle.
                if (ta2 < 2.5 * ta1) {
                    min1 = minIn1;
                    min2 = minIn2;
                    min3 = minIn3;
                } else {
                    // build weights with triangle areas
                    ta1 = 1 / ta1;
                    ta2 = 1 / ta2;

                    // calculate weighted geometric median as result
                    return Point2d.geometricMedian(
                            new Point2d[] { points[min1],
                                            points[min2],
                                            points[min3],
                                            points[minIn1],
                                            points[minIn2],
                                            points[minIn3]},
                            new double[] { ta1, ta1, ta1, ta2, ta2, ta2});
                }
            }
        }

        // step 6: calculate geometric median of final triangle as result
        return Point2d.geometricMedian(new Point2d[] {points[min1],
            points[min2], points[min3]});
    }

}
