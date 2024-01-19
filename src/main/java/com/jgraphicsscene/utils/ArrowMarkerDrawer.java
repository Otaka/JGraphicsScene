package com.jgraphicsscene.utils;

import java.awt.Graphics2D;

public class ArrowMarkerDrawer {
    private final Polygon2D polygon;

    public ArrowMarkerDrawer(float x1, float y1, float x2, float y2, float arrowLength, float arrowWidth) {
        float dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - arrowLength, xn = xm, ym = arrowWidth, yn = -arrowWidth, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        float[] xpoints = new float[3];
        float[] ypoints = new float[3];
        xpoints[0] = x2;
        xpoints[1] = (float) xm;
        xpoints[2] = (float) xn;
        ypoints[0] = y2;
        ypoints[1] = (float) ym;
        ypoints[2] = (float) yn;
        polygon = new Polygon2D(xpoints, ypoints, 3);
    }

    public void drawArrow(Graphics2D g) {
        g.fill(polygon);
    }
}
