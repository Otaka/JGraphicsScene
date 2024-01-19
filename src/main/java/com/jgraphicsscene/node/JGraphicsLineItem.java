package com.jgraphicsscene.node;

import com.jgraphicsscene.ManipulatorProcessor;
import com.jgraphicsscene.PaintContext;
import com.jgraphicsscene.Utils;
import com.jgraphicsscene.utils.ArrowMarkerDrawer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class JGraphicsLineItem extends JGraphicsItem {
    private float x1, y1, x2, y2;
    private Color color;
    private boolean arrowOnStart;
    private boolean arrowOnEnd;
    private Color arrowStartColor;
    private Color arrowEndColor;
    private Line2D lineShape;
    private ArrowMarkerDrawer arrowStartMarkerDrawer;
    private ArrowMarkerDrawer arrowEndMarkerDrawer;
    private int arrowWidth = 3;
    private int arrowLength = 8;

    public JGraphicsLineItem(float x1, float y1, float x2, float y2) {
        set(x1, y1, x2, y2);
        color = Color.BLACK;
    }

    public JGraphicsLineItem set(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        resetDrawShapeCache();
        return this;
    }

    public int getArrowWidth() {
        return arrowWidth;
    }

    public JGraphicsLineItem setArrowWidth(int arrowWidth) {
        this.arrowWidth = arrowWidth;
        resetDrawShapeCache();
        return this;
    }

    public int getArrowLength() {
        return arrowLength;
    }

    public JGraphicsLineItem setArrowLength(int arrowLength) {
        this.arrowLength = arrowLength;
        resetDrawShapeCache();
        return this;
    }

    public boolean isArrowOnStart() {
        return arrowOnStart;
    }

    public JGraphicsLineItem setArrowOnStart(boolean arrowOnStart) {
        this.arrowOnStart = arrowOnStart;
        resetDrawShapeCache();
        return this;
    }

    public boolean isArrowOnEnd() {
        return arrowOnEnd;
    }

    public JGraphicsLineItem setArrowOnEnd(boolean arrowOnEnd) {
        this.arrowOnEnd = arrowOnEnd;
        resetDrawShapeCache();
        return this;
    }

    public Color getArrowStartColor() {
        return arrowStartColor;
    }

    public JGraphicsLineItem setArrowStartColor(Color arrowStartColor) {
        this.arrowStartColor = arrowStartColor;
        return this;
    }

    public Color getArrowEndColor() {
        return arrowEndColor;
    }

    public JGraphicsLineItem setArrowEndColor(Color arrowEndColor) {
        this.arrowEndColor = arrowEndColor;
        return this;
    }

    public JGraphicsLineItem setStart(float x, float y) {
        this.x1 = x;
        this.y1 = y;
        resetDrawShapeCache();
        return this;
    }

    public JGraphicsLineItem setEnd(float x, float y) {
        this.x2 = x;
        this.y2 = y;
        resetDrawShapeCache();
        return this;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }

    public Color getColor() {
        return color;
    }

    public JGraphicsLineItem setColor(Color color) {
        this.color = color;
        return this;
    }

    private void resetDrawShapeCache() {
        arrowStartMarkerDrawer = null;
        arrowEndMarkerDrawer = null;
        lineShape = null;
    }

    @Override
    public Shape createBoundingShape() {
        return createLineBoundingShape(x1, y1, x2, y2);
    }

    protected Shape createLineBoundingShape(float x1, float y1, float x2, float y2) {
        float thickness = 5;
        float dX = x2 - x1;
        float dY = y2 - y1;
        // line length
        double lineLength = calculateLineLength();
        double scale = (double) (thickness) / (2 * lineLength);
        // The x,y increments from an endpoint needed to create a
        // rectangle...
        double ddx = -scale * (double) dY;
        double ddy = scale * (double) dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        // Now we can compute the corner points...
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];

        xPoints[0] = (int) (x1 + dx);
        yPoints[0] = (int) (y1 + dy);
        xPoints[1] = (int) (x1 - dx);
        yPoints[1] = (int) (y1 - dy);
        xPoints[2] = (int) (x2 - dx);
        yPoints[2] = (int) (y2 - dy);
        xPoints[3] = (int) (x2 + dx);
        yPoints[3] = (int) (y2 + dy);
        return new Polygon(xPoints, yPoints, 4);
    }

    @Override
    protected void paintItem(PaintContext p) {
        if (lineShape == null) {
            float tx1 = x1;
            float ty1 = y1;
            float tx2 = x2;
            float ty2 = y2;
            float lineLength = calculateLineLength();
            if (arrowOnStart) {
                float part = arrowLength / lineLength;
                tx1 = Utils.lerp(x1, x2, part);
                ty1 = Utils.lerp(y1, y2, part);
            }
            if (arrowOnEnd) {
                float part = arrowLength / lineLength;
                tx2 = Utils.lerp(x1, x2, 1 - part);
                ty2 = Utils.lerp(y1, y2, 1 - part);
            }
            lineShape = new Line2D.Float(tx1, ty1, tx2, ty2);
        }

        if (arrowOnStart && arrowStartMarkerDrawer == null)
            arrowStartMarkerDrawer = new ArrowMarkerDrawer(x2, y2, x1, y1, arrowLength, arrowWidth);
        if (arrowOnEnd && arrowEndMarkerDrawer == null)
            arrowEndMarkerDrawer = new ArrowMarkerDrawer(x1, y1, x2, y2, arrowLength, arrowWidth);


        Graphics2D g = p.getGraphics();
        g.setColor(color);
        g.draw(lineShape);
        if (arrowOnStart) {
            g.setColor((arrowStartColor != null) ? arrowStartColor : color);
            arrowStartMarkerDrawer.drawArrow(g);
        }
        if (arrowOnEnd) {
            g.setColor((arrowEndColor != null) ? arrowEndColor : color);
            arrowEndMarkerDrawer.drawArrow(g);
        }
    }

    @Override
    protected float getWidth() {
        return 0;
    }

    @Override
    protected float getHeight() {
        return 0;
    }

    public float calculateLineLength() {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    @Override
    public List<JGraphicsItem> createManipulators() {
        List<JGraphicsItem> manipulators = new ArrayList<>();
        JGraphicsItem startManip = scene.getManipulationHandlerFactory().createManipulationHandler(this);
        JGraphicsItem endManip = scene.getManipulationHandlerFactory().createManipulationHandler(this);
        new ManipulatorProcessor(startManip, this, () -> new Point2D.Float(getX1(), getY1()), (manipulator, dx, dy, processor) -> {
            setStart(getX1() + dx, getY1() + dy);
        }, 0).start();
        new ManipulatorProcessor(endManip, this, () -> new Point2D.Float(getX2(), getY2()), (manipulator, dx, dy, processor) -> {
            setEnd(getX2() + dx, getY2() + dy);
        }, 1).start();
        manipulators.add(startManip);
        manipulators.add(endManip);
        return manipulators;
    }
}