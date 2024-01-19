package com.jgraphicsscene.utils;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BezierPolyline {
    private final List<BezierCurve> segments;

    public BezierPolyline() {
        segments = new ArrayList<>();
    }

    public BezierPolyline(List<BezierCurve> segments) {
        this.segments = segments;
    }

    public static BezierPolyline createSmoothLineFromPoints(List<Point2D> points) {
        if (points.size() <= 1) {
            return new BezierPolyline();
        }
        if (points.size() == 2) {
            Point2D p1 = points.get(0);
            Point2D p2 = points.get(1);
            Point2D c1 = new Point2D.Float((float) p2.getX(), (float) p2.getY());
            Point2D c2 = new Point2D.Float((float) p1.getX(), (float) p1.getY());
            BezierCurve bezierCurve = new BezierCurve(p1, p2, c1, c2);
            return new BezierPolyline(Collections.singletonList(bezierCurve));
        }

        //we will process all points except the first and last
        Point2D[] controlPoints = new Point2D[(points.size() - 2) * 2 + 2];//each point has 2 control points, except the first and last, they have only 1
        int controlPointIndex = 1;
        for (int i = 1; i < points.size() - 1; i++) {
            Point2D p = points.get(i);
            Point2D prevPoint = points.get(i - 1);
            Point2D nextPoint = points.get(i + 1);
            Vector2 firstLine = new Vector2((float) (p.getX() - prevPoint.getX()), (float) (p.getY() - prevPoint.getY()));
            Vector2 secondLine = new Vector2((float) (nextPoint.getX() - p.getX()), (float) (nextPoint.getY() - p.getY()));
            float firstLineLength = firstLine.length();
            float secondLineLength = secondLine.length();
            firstLine.normalize();
            secondLine.normalize();
            secondLine.multiple(-1);
            Vector2 normalVector = firstLine.clone().add(secondLine);
            if (Math.abs(normalVector.getX()) < 0.0001 && Math.abs(normalVector.getY()) < 0.0001) {
                normalVector.set(0, 1);
            }
            normalVector.normalize();
            Vector2 tangentVector = normalVector.clone().convertToNormal();
            float angleBetweenVectors = (float) Math.toDegrees(firstLine.angleBetweenVectors(secondLine));
            if (angleBetweenVectors < 0) {
                tangentVector.multiple(-1);
            }
            Vector2 leftControlPoint = tangentVector.clone().multiple(firstLineLength / 3);
            Vector2 rightControlPoint = tangentVector.clone().multiple(-secondLineLength / 3);
            controlPoints[controlPointIndex] = new Point2D.Float((float) (p.getX() + leftControlPoint.getX()), (float) (p.getY() + leftControlPoint.getY()));
            controlPointIndex++;
            controlPoints[controlPointIndex] = new Point2D.Float((float) (p.getX() + rightControlPoint.getX()), (float) (p.getY() + rightControlPoint.getY()));
            controlPointIndex++;
        }
        //calculate control point for first dot
        Vector2 line = new Vector2((float) (controlPoints[1].getX() - points.get(0).getX()), (float) (controlPoints[1].getY() - points.get(0).getY()));
        float lineLength = line.length();
        Vector2 control = line.normalize().multiple(lineLength / 3);
        controlPoints[0] = new Point2D.Float((float) (points.get(0).getX() + control.getX()), (float) (points.get(0).getY() + control.getY()));

        //calculate control point for last dot
        Point2D beforeLastControlPoint = controlPoints[controlPoints.length - 2];
        line.set((float) (points.get(points.size() - 1).getX() - beforeLastControlPoint.getX()), (float) (points.get(points.size() - 1).getY() - beforeLastControlPoint.getY()));
        lineLength = line.length();
        control = line.normalize().multiple(-lineLength / 3);
        controlPoints[controlPoints.length - 1] = new Point2D.Float((float) (points.get(points.size() - 1).getX() + control.getX()), (float) (points.get(points.size() - 1).getY() + control.getY()));
        BezierPolyline polyline = new BezierPolyline(new ArrayList<>());
        for (int i = 1; i < points.size(); i++) {
            Point2D start = points.get(i - 1);
            Point2D end = points.get(i);
            Point2D c1 = controlPoints[(i - 1) * 2];
            Point2D c2 = controlPoints[(i - 1) * 2 + 1];
            BezierCurve segment = new BezierCurve(start, end, c1, c2);
            polyline.getSegments().add(segment);
        }
        return polyline;
    }

    public List<BezierCurve> getSegments() {
        return segments;
    }

    /**
     * Get bezier segment in polyline. If index is negative, it will be counted from the end of the polyline.
     */
    public BezierCurve getSegment(int index) {
        if (index < 0) {
            return segments.get(segments.size() + index);
        } else {
            return segments.get(index);
        }
    }

    public int getSegmentsCount() {
        return segments.size();
    }

    public BezierCurve lastSegment() {
        return getSegment(-1);
    }

    public BezierCurve firstSegment() {
        return getSegment(0);
    }

    public GeneralPath getGraphicsPath() {
        GeneralPath path = new GeneralPath();
        Point2D lastPoint = null;
        for (BezierCurve segment : segments) {
            Point2D start = segment.getStart();
            Point2D end = segment.getEnd();
            Point2D control1 = segment.getControl1();
            Point2D control2 = segment.getControl2();
            if (start != lastPoint) {
                path.moveTo(start.getX(), start.getY());
            }
            path.curveTo(control1.getX(), control1.getY(), control2.getX(), control2.getY(), end.getX(), end.getY());
            lastPoint = end;
        }
        return path;
    }

    public BezierPolyline clone() {
        return new BezierPolyline(segments.stream()
                .map(BezierCurve::clone)
                .collect(Collectors.toList())
        );
    }

    public float getLength() {
        float length = 0;
        for (BezierCurve segment : segments) {
            length += segment.getApproximatedLength();
        }
        return length;
    }
}
