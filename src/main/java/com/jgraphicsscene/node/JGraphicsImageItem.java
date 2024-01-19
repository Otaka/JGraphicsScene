package com.jgraphicsscene.node;

import com.jgraphicsscene.PaintContext;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

public class JGraphicsImageItem extends JGraphicsAbstractRectItem {
    private BufferedImage image;

    public JGraphicsImageItem(float x, float y, BufferedImage image) {
        setPosition(x, y, false);
        setImage(image);
    }

    public BufferedImage getImage() {
        return image;
    }

    public JGraphicsImageItem setImage(BufferedImage image) {
        this.image = image;
        if (image != null) {
            width = image.getWidth();
            height = image.getHeight();
        } else {
            width = 0;
            height = 0;
        }
        return this;
    }

    @Override
    public Shape getViewBoundingShape() {
        if (getImage() == null) return new Rectangle(0, 0, 0, 0);
        return super.getViewBoundingShape();
    }

    @Override
    protected void paintItem(PaintContext p) {
        if (getImage() == null) return;
        p.getGraphics().drawImage(getImage(), (int) xOffset, (int) yOffset, (int) getWidth(), (int) getHeight(), null);
    }
}