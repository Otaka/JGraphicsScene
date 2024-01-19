package com.jgraphicsscene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Stack;

public class PaintContext {
    private final AffineTransform originalTransform;
    private final Stack<Color> xorModeColorStack = new Stack<Color>();
    private Graphics2D g;
    private boolean renderingEnabled;
    private Color xorModeColor;

    public PaintContext(Graphics2D g, AffineTransform originalTransform) {
        this.g = g;
        this.originalTransform = originalTransform;
        renderingEnabled = true;
    }

    public void setXORMode(Color color) {
        if (xorModeColor != null) {
            xorModeColorStack.push(xorModeColor);
        }
        g.setXORMode(color);
        xorModeColor = color;
    }

    public void restoreXORMode() {
        if (!xorModeColorStack.isEmpty()) {
            xorModeColor = xorModeColorStack.pop();
            g.setXORMode(xorModeColor);
        } else {
            g.setPaintMode();
            xorModeColor = null;
        }
    }

    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    public void setRenderingEnabled(boolean renderingEnabled) {
        this.renderingEnabled = renderingEnabled;
    }

    public AffineTransform getOriginalTransform() {
        return originalTransform;
    }

    public Graphics2D getGraphics() {
        return g;
    }

    public void setGraphics(Graphics2D g) {
        this.g = g;
    }
}
