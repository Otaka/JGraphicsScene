package com.jgraphicsscene.node;

import com.jgraphicsscene.Selection;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class JGraphicsImageItem extends JGraphicsItem {
    private final BufferedImage image;
    private final Rectangle boundingBox = new Rectangle();
    private float left, top;

    public JGraphicsImageItem(float x, float y, BufferedImage image) {
        setPosition(x, y, false);
        this.image = image;
    }

    public float getWidth() {
        return image.getWidth();
    }

    public float getHeight() {
        return image.getHeight();
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
        boundingBox.setRect((int) left, (int) top, (int) getWidth(), (int) getHeight());
        return mapItemToSceneImmutable(boundingBox);
    }

    @Override
    protected void paintItem(Graphics2D g, AffineTransform oldAffineTransform, Selection selection) {
        g.drawImage(image, (int) left, (int) top, null);
    }
}