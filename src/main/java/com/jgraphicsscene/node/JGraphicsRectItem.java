package com.jgraphicsscene.node;

import com.jgraphicsscene.PaintContext;

import java.awt.Color;
import java.awt.Graphics2D;

public class JGraphicsRectItem extends JGraphicsAbstractRectItem {
    private Color borderColor = Color.BLACK;
    private Color fillColor = Color.GRAY;

    public JGraphicsRectItem(float x, float y, float width, float height) {
        setPosition(x, y, false);
        setWidth(width);
        setHeight(height);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public JGraphicsRectItem setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public JGraphicsRectItem setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    @Override
    protected void paintItem(PaintContext p) {
        Graphics2D g = p.getGraphics();
        g.setColor(getFillColor());
        g.fillRect((int) xOffset, (int) yOffset, (int) width, (int) height);
        g.setColor(getBorderColor());
        g.drawRect((int) xOffset, (int) yOffset, (int) width, (int) height);
    }
}