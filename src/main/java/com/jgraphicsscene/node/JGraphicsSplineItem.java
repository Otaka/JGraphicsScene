package com.jgraphicsscene.node;

import com.jgraphicsscene.ManipulatorProcessor;
import com.jgraphicsscene.PaintContext;
import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.utils.BezierArrowDrawer;
import com.jgraphicsscene.utils.BezierCurve;
import com.jgraphicsscene.utils.BezierPolyline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ALL")
public class JGraphicsSplineItem extends JGraphicsItem {
    private static final Stroke selectionStroke = new BasicStroke(5);
    private final List<Point2D> points;
    private final BezierArrowDrawer bezierArrowDrawer = new BezierArrowDrawer();
    private Color subdivideManipulatorColor = new Color(70, 131, 246, 150);
    private boolean showManipulatorsOnEnds = true;

    public JGraphicsSplineItem(Point2D... points) {
        this.points = new ArrayList<>(Arrays.asList(points));
        bezierArrowDrawer.setPoints(points);
        bezierArrowDrawer.setArrowLength(8);
        bezierArrowDrawer.setArrowWidth(3);

        dirtyTransform();
    }


    public Color getSubdivideManipulatorColor() {
        return subdivideManipulatorColor;
    }

    public void setSubdivideManipulatorColor(Color subdivideManipulatorColor) {
        this.subdivideManipulatorColor = subdivideManipulatorColor;
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public JGraphicsSplineItem setPoints(Point2D... points) {
        bezierArrowDrawer.setPoints(points);
        this.points.clear();
        Collections.addAll(this.points, points);
        dirtyTransform();
        return this;
    }

    public JGraphicsSplineItem setPoints(List<Point2D> points) {
        this.points.clear();
        this.points.addAll(points);
        bezierArrowDrawer.setPoints(points);
        dirtyTransform();
        return this;
    }

    public void refreshLine() {
        bezierArrowDrawer.setPoints(points);
        dirtyTransform();
    }

    public Point2D getStartPoint() {
        return points.get(0);
    }

    public Point2D getEndPoint() {
        return points.get(points.size() - 1);
    }

    public float getCurveLength() {
        return bezierArrowDrawer.getBezierPolyline().getLength();
    }

    public float getDistanceBetweenEnds() {
        if (points.isEmpty()) return 0;
        return (float) points.get(0).distance(points.get(points.size() - 1));
    }

    public boolean isArrowOnStart() {
        return bezierArrowDrawer.isHasStartArrow();
    }

    public JGraphicsSplineItem setArrowOnStart(boolean hasStartArrow) {
        bezierArrowDrawer.setHasStartArrow(hasStartArrow);
        dirtyTransform();
        return this;
    }

    public boolean isArrowOnEnd() {
        return bezierArrowDrawer.isHasEndArrow();
    }

    public JGraphicsSplineItem setArrowOnEnd(boolean hasEndArrow) {
        bezierArrowDrawer.setHasEndArrow(hasEndArrow);
        dirtyTransform();
        return this;
    }

    public JGraphicsSplineItem setLineWidth(float lineWidth) {
        bezierArrowDrawer.setLineWidth(lineWidth);
        dirtyTransform();
        return this;
    }

    public float getArrowLength() {
        return bezierArrowDrawer.getArrowLength();
    }

    public JGraphicsSplineItem setArrowLength(float arrowLength) {
        bezierArrowDrawer.setArrowLength(arrowLength);
        dirtyTransform();
        return this;
    }

    public float getArrowWidth() {
        return bezierArrowDrawer.getArrowWidth();
    }

    public JGraphicsSplineItem setArrowWidth(float arrowWidth) {
        bezierArrowDrawer.setArrowWidth(arrowWidth);
        dirtyTransform();
        return this;
    }

    public JGraphicsSplineItem setColor(Color lineColor) {
        bezierArrowDrawer.setLineColor(lineColor);
        dirtyTransform();
        return this;
    }

    public Color getArrowStartColor() {
        return bezierArrowDrawer.getStartArrowColor();
    }

    public JGraphicsSplineItem setArrowStartColor(Color startArrowColor) {
        bezierArrowDrawer.setStartArrowColor(startArrowColor);
        dirtyTransform();
        return this;
    }

    public Color getArrowEndColor() {
        return bezierArrowDrawer.getEndArrowColor();
    }

    public JGraphicsSplineItem setArrowEndColor(Color endArrowColor) {
        bezierArrowDrawer.setEndArrowColor(endArrowColor);
        dirtyTransform();
        return this;
    }

    public boolean isShowManipulatorsOnEnds() {
        return showManipulatorsOnEnds;
    }

    public JGraphicsSplineItem setShowManipulatorsOnEnds(boolean showManipulatorsOnEnds) {
        this.showManipulatorsOnEnds = showManipulatorsOnEnds;
        return this;
    }

    @Override
    public Shape createBoundingShape() {
        GeneralPath shape = bezierArrowDrawer.getLineShape();
        return selectionStroke.createStrokedShape(shape);
    }

    @Override
    protected void paintItem(PaintContext p) {
        bezierArrowDrawer.draw(p.getGraphics());
    }

    @Override
    public List<JGraphicsItem> createManipulators() {
        List<JGraphicsItem> manipulators = new ArrayList<>();
        // create manipulators that needed for each point
        List<JGraphicsItem> movePointsManipulators = new ArrayList<>();
        int startIndex = 0;
        int endIndex = points.size() - 1;
        if (!showManipulatorsOnEnds) {
            startIndex++;
            endIndex--;
        }
        for (int i = startIndex; i <= endIndex; i++) {
            Point2D point = points.get(i);
            JGraphicsItem manipulator = scene.getManipulationHandlerFactory().createManipulationHandler(this);
            manipulator.addEvent((itemChangedType, item, arg) -> {
                //remove intermediate points on double click
                if (itemChangedType == ItemChangedType.DoubleClick) {
                    if (item != movePointsManipulators.get(0) && item != movePointsManipulators.get(movePointsManipulators.size() - 1)) {
                        int index = movePointsManipulators.indexOf(item);
                        points.remove(index);
                        bezierArrowDrawer.setPoints(points);
                        dirtyTransform();
                    }
                    return true;
                }
                return false;
            });
            ManipulatorProcessor processor = new ManipulatorProcessor(manipulator, this, () -> point, (manipulator1, dx, dy, processor1) -> {
                point.setLocation(point.getX() + dx, point.getY() + dy);
                bezierArrowDrawer.setPoints(points);
                dirtyTransform();
            }, i);
            processor.start();
            manipulators.add(manipulator);
            movePointsManipulators.add(manipulator);
        }
        // create manipulators that will subdivide the segment on two parts
        BezierPolyline polyline = bezierArrowDrawer.getBezierPolyline();
        for (int i = 0; i < polyline.getSegmentsCount(); i++) {
            int index = i;
            BezierCurve segment = polyline.getSegment(i);
            Point2D segmentCenterPoint = segment.calculatePointInT(0.5f);
            mapItemToSceneMut(segmentCenterPoint);
            JGraphicsEllipseItem manipulator = (JGraphicsEllipseItem) scene.getManipulationHandlerFactory().createManipulationHandler(this);
            manipulators.add(manipulator);
            manipulator.setFillColor(getSubdivideManipulatorColor());
            manipulator.setBorderColor(Color.WHITE);
            manipulator.setPosition((float) segmentCenterPoint.getX(), (float) segmentCenterPoint.getY(), false);
            manipulator.addEvent((itemChangedType, item, arg) -> {
                if (itemChangedType != ItemChangedType.Position) {
                    return false;
                }
                //disable this manipulator
                manipulator.deleteEvents();
                Point2D newCurvePoint = mapSceneToItemImmutable(segmentCenterPoint);
                //subdivide the curve
                points.add(index + 1, newCurvePoint);
                bezierArrowDrawer.setPoints(points);
                dirtyTransform();
                return true;
            });
        }
        return manipulators;
    }

    @Override
    protected float getWidth() {
        return 0;
    }

    @Override
    protected float getHeight() {
        return 0;
    }
}
