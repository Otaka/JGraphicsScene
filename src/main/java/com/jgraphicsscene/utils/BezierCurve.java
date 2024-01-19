package com.jgraphicsscene.utils;

import java.awt.geom.Point2D;

public class BezierCurve {
    private final Point2D control1;
    private final Point2D control2;
    private Point2D start;
    private Point2D end;

    public BezierCurve(Point2D start, Point2D end, Point2D control1, Point2D control2) {
        this.start = start;
        this.end = end;
        this.control1 = control1;
        this.control2 = control2;
    }

    public float getApproximatedLength() {
        //let's split the Bezier curve into 20 parts and calculate the length of each part
        Point2D lastPoint = start;
        Point2D tempPoint1 = new Point2D.Float();
        Point2D tempPoint2 = new Point2D.Float();
        int stepsCount = 10;
        float step = (float) 1 / stepsCount;
        float totalLength = 0;
        for (int i = 1; i <= stepsCount; i++) {
            Point2D point = calculatePointInT(step * i, (i % 2 == 0) ? tempPoint1 : tempPoint2);
            float length = (float) lastPoint.distance(point);
            totalLength += length;
            lastPoint = point;
        }
        return totalLength;
    }

    public BezierCurve clone() {
        return new BezierCurve(copyPoint(start), copyPoint(end), copyPoint(control1), copyPoint(control2));
    }

    private Point2D copyPoint(Point2D point) {
        return new Point2D.Float((float) point.getX(), (float) point.getY());
    }

    public float linearLength() {
        return (float) start.distance(end);
    }

    /**
     * Turns start point to normal vector. Can be useful to properly align arrowhead to some surface/line
     */
    public void turnStartToNormal(Vector2 normalVector) {
        Vector2 v = normalVector.clone().normalize().setVectorLength(linearLength() / 2);
        control1.setLocation(start.getX() + v.getX(), start.getY() + v.getY());
    }

    public void turnEndToNormal(Vector2 normalVector) {
        Vector2 v = normalVector.clone().normalize().setVectorLength(linearLength() / 2);
        control2.setLocation(end.getX() + v.getX(), end.getY() + v.getY());
    }

    public Point2D getStart() {
        return start;
    }

    public void setStart(Point2D start) {
        this.start = start;
    }

    public Point2D getEnd() {
        return end;
    }

    public void setEnd(Point2D end) {
        this.end = end;
    }

    public Point2D getControl1() {
        return control1;
    }

    public Point2D getControl2() {
        return control2;
    }

    public Point2D calculatePointInT(float t) {
        return calculatePointInT(t, new Point2D.Float());
    }

    public Point2D calculatePointInT(float t, Point2D result) {
        double x = (Math.pow(1 - t, 3) * start.getX() + 3 * Math.pow(1 - t, 2) * t * control1.getX() + 3 * (1 - t) * Math.pow(t, 2) * control2.getX() + Math.pow(t, 3) * end.getX());
        double y = (Math.pow(1 - t, 3) * start.getY() + 3 * Math.pow(1 - t, 2) * t * control1.getY() + 3 * (1 - t) * Math.pow(t, 2) * control2.getY() + Math.pow(t, 3) * end.getY());
        result.setLocation(x, y);
        return result;
    }
}
