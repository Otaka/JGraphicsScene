package com.jgraphicsscene.node;

import com.jgraphicsscene.Selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class JGraphicsEllipseItem extends JGraphicsItem {
    private final Rectangle boundingBox = new Rectangle();
    private float left, top, width, height;
    private Color borderColor = Color.BLACK;
    private Color fillColor = Color.GRAY;

    public JGraphicsEllipseItem(float x, float y, float left, float top, float width, float height) {
        setPosition(x, y, false);
        setRect(left, top, width, height);
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public JGraphicsEllipseItem setRect(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        return this;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public JGraphicsEllipseItem setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public JGraphicsEllipseItem setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    @Override
    public boolean contains(float x, float y) {
        return getBoundingBox().contains(x, y);
    }

    @Override
    public boolean intersect(Rectangle rectangle) {
        return getBoundingBox().intersects(rectangle);
    }

    public Shape getBoundingBox() {
        boundingBox.setRect((int) left, (int) top, (int) width, (int) height);
        return mapItemToSceneImmutable(boundingBox);
    }

    @Override
    protected void paintItem(Graphics2D g, AffineTransform oldAffineTransform, Selection selection) {
        g.setColor(fillColor);
        g.fillOval((int) left, (int) top, (int) width, (int) height);
        g.setColor(borderColor);
        g.drawOval((int) left, (int) top, (int) width, (int) height);
    }
}