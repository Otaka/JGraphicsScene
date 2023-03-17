package com.jgraphicsscene.node;

import com.jgraphicsscene.Selection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class JGraphicsTextItem extends JGraphicsItem {
    private static Font DEFAULT_FONT = new JLabel().getFont();
    private final Rectangle boundingBox = new Rectangle();
    private float left, top, width, height;
    private Color color = Color.BLACK;
    private String text;
    private Font font;
    private List<String> cachedSplittedStrings;
    private int cachedStringsDistance;

    public JGraphicsTextItem(float x, float y, float left, float top, float width, float height, String text) {
        setPosition(x, y, false);
        setRect(left, top, width, height);
        this.text = text;
    }

    public static Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    public Font getFont() {
        if (font == null) {
            font = DEFAULT_FONT;
        }
        return font;
    }

    public JGraphicsTextItem setFont(Font font) {
        this.font = font;
        cachedSplittedStrings = null;
        return this;
    }

    public String getText() {
        return text;
    }

    public JGraphicsTextItem setText(String text) {
        this.text = text;
        cachedSplittedStrings = null;
        return this;
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

    public JGraphicsTextItem setRect(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public JGraphicsTextItem setColor(Color color) {
        this.color = color;
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
        g.setFont(getFont());
        g.setColor(getColor());
        if (cachedSplittedStrings == null) {
            preprocessText(g);
        }

        int lineY = (int) (top + cachedStringsDistance);
        for (String str : cachedSplittedStrings) {
            g.drawString(str, 0, lineY);
            lineY += cachedStringsDistance;
        }
    }

    private void preprocessText(Graphics2D g) {
        cachedSplittedStrings = new ArrayList<>();
        FontMetrics fontMetrics = g.getFontMetrics();
        cachedStringsDistance = fontMetrics.getHeight();
        String textToDraw = text;
        String[] arr = textToDraw.split(" ");
        int nIndex = 0;
        while (nIndex < arr.length) {
            StringBuilder line = new StringBuilder(arr[nIndex++]);
            while ((nIndex < arr.length) && (fontMetrics.stringWidth(line + " " + arr[nIndex]) < getWidth())) {
                line.append(" ").append(arr[nIndex]);
                nIndex++;
            }
            cachedSplittedStrings.add(line.toString());
        }
    }
}