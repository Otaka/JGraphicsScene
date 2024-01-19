package com.jgraphicsscene.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

public class BezierArrowDrawer {
    private final static Stroke arrowShapeStroke = new BasicStroke(1);
    private BezierPolyline bezierPolyline;
    private boolean hasStartArrow;
    private boolean hasEndArrow;
    private float arrowLength = 20;
    private float arrowWidth = 6;
    private GeneralPath lineShape;
    private GeneralPath startArrowShape;
    private GeneralPath endArrowShape;
    private Color lineColor = Color.BLACK;
    private Color startArrowColor = Color.RED;
    private Color endArrowColor = Color.GREEN;
    private Stroke lineStroke = new BasicStroke(1);
    private float lineWidth = 1f;

    public boolean isHasStartArrow() {
        return hasStartArrow;
    }

    public BezierArrowDrawer setHasStartArrow(boolean hasStartArrow) {
        this.hasStartArrow = hasStartArrow;
        startArrowShape = null;
        return this;
    }

    public boolean isHasEndArrow() {
        return hasEndArrow;
    }

    public BezierArrowDrawer setHasEndArrow(boolean hasEndArrow) {
        this.hasEndArrow = hasEndArrow;
        endArrowShape = null;
        return this;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public BezierArrowDrawer setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        return this;
    }

    public float getArrowLength() {
        return arrowLength;
    }

    public BezierArrowDrawer setArrowLength(float arrowLength) {
        this.arrowLength = arrowLength;
        startArrowShape = null;
        endArrowShape = null;
        return this;
    }

    public float getArrowWidth() {
        return arrowWidth;
    }

    public BezierArrowDrawer setArrowWidth(float arrowWidth) {
        this.arrowWidth = arrowWidth;
        startArrowShape = null;
        endArrowShape = null;
        return this;
    }

    public BezierArrowDrawer setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public Color getStartArrowColor() {
        return startArrowColor;
    }

    public BezierArrowDrawer setStartArrowColor(Color startArrowColor) {
        this.startArrowColor = startArrowColor;
        return this;
    }

    public Color getEndArrowColor() {
        return endArrowColor;
    }

    public BezierArrowDrawer setEndArrowColor(Color endArrowColor) {
        this.endArrowColor = endArrowColor;
        return this;
    }

    public BezierArrowDrawer setPoints(Point2D... points) {
        lineShape = null;
        startArrowShape = null;
        endArrowShape = null;
        this.bezierPolyline = BezierPolyline.createSmoothLineFromPoints(Arrays.asList(points));
        return this;
    }

    public BezierArrowDrawer setPoints(List<Point2D> points) {
        lineShape = null;
        startArrowShape = null;
        endArrowShape = null;
        this.bezierPolyline = BezierPolyline.createSmoothLineFromPoints(points);
        return this;
    }

    public GeneralPath getLineShape() {
        checkAndRebuild();
        return lineShape;
    }

    public BezierPolyline getBezierPolyline() {
        return bezierPolyline;
    }

    public BezierArrowDrawer setBezierPolyline(BezierPolyline bezierPolyline) {
        lineShape = null;
        startArrowShape = null;
        endArrowShape = null;
        this.bezierPolyline = bezierPolyline;
        return this;
    }

    private void checkAndRebuild() {
        if ((hasStartArrow && startArrowShape == null) || (hasEndArrow && endArrowShape == null) || (lineShape == null)) {
            rebuild();
        }
    }

    private void rebuild() {
        BezierPolyline poly = bezierPolyline.clone();
        Point2D lineStartPoint = poly.firstSegment().getStart();
        Point2D lineLastPoint = poly.lastSegment().getEnd();
        //If there is arrow at the end - make the last bezier segment smaller
        float lastSegmentLength = poly.lastSegment().getApproximatedLength();
        float firstSegmentLength = poly.firstSegment().getApproximatedLength();
        if (hasEndArrow) {
            Point2D backEndArrowPoint;
            if (lastSegmentLength >= arrowLength) {
                backEndArrowPoint = poly.lastSegment().calculatePointInT(1 - (arrowLength / lastSegmentLength));
                Vector2 v = new Vector2(lineLastPoint, backEndArrowPoint).setVectorLength(arrowLength);
                backEndArrowPoint = new Point2D.Float((float) (lineLastPoint.getX() + v.getX()), (float) (lineLastPoint.getY() + v.getY()));
            } else {
                Vector2 v = new Vector2(lineLastPoint, poly.lastSegment().getStart()).setVectorLength(arrowLength);
                backEndArrowPoint = new Point2D.Float((float) (lineLastPoint.getX() + v.getX()), (float) (lineLastPoint.getY() + v.getY()));
            }
            endArrowShape = createArrowHeadShape(backEndArrowPoint, lineLastPoint);
            poly.lastSegment().setEnd(backEndArrowPoint);
        }
        if (hasStartArrow) {
            Point2D backStartArrowPoint;
            if (firstSegmentLength >= arrowLength) {
                backStartArrowPoint = poly.firstSegment().calculatePointInT(arrowLength / firstSegmentLength);
                Vector2 v = new Vector2(lineStartPoint, backStartArrowPoint).setVectorLength(arrowLength);
                backStartArrowPoint = new Point2D.Float((float) (lineStartPoint.getX() + v.getX()), (float) (lineStartPoint.getY() + v.getY()));
            } else {
                Vector2 v = new Vector2(lineStartPoint, poly.firstSegment().getEnd()).setVectorLength(arrowLength);
                backStartArrowPoint = new Point2D.Float((float) (lineStartPoint.getX() + v.getX()), (float) (lineStartPoint.getY() + v.getY()));
            }
            startArrowShape = createArrowHeadShape(backStartArrowPoint, lineStartPoint);
            poly.firstSegment().setStart(backStartArrowPoint);
        }
        lineShape = poly.getGraphicsPath();
    }

    private GeneralPath createArrowHeadShape(Point2D startPoint, Point2D endPoint) {
        Vector2 arrowVector = new Vector2(endPoint, startPoint);
        arrowVector.setVectorLength(arrowLength);
        arrowVector.convertToNormal();
        arrowVector.setVectorLength(arrowWidth);
        Point2D arrowPoint1 = new Point2D.Float((float) (startPoint.getX() + arrowVector.getX()), (float) (startPoint.getY() + arrowVector.getY()));
        arrowVector.setVectorLength(-arrowWidth);
        Point2D arrowPoint2 = new Point2D.Float((float) (startPoint.getX() + arrowVector.getX()), (float) (startPoint.getY() + arrowVector.getY()));
        GeneralPath shape = new GeneralPath();
        shape.moveTo(endPoint.getX(), endPoint.getY());
        shape.lineTo(arrowPoint1.getX(), arrowPoint1.getY());
        shape.lineTo(arrowPoint2.getX(), arrowPoint2.getY());
        shape.closePath();
        return shape;
    }

    public BezierArrowDrawer draw(Graphics2D g) {
        checkAndRebuild();
        Stroke oldStroke = g.getStroke();
        g.setColor(lineColor);
        g.setStroke(lineStroke);
        g.draw(lineShape);
        if (startArrowShape != null) {
            g.setStroke(arrowShapeStroke);
            g.setColor(startArrowColor);
            g.fill(startArrowShape);
        }
        if (endArrowShape != null) {
            g.setStroke(arrowShapeStroke);
            g.setColor(endArrowColor);
            g.fill(endArrowShape);
        }
        g.setStroke(oldStroke);
        return this;
    }
}
