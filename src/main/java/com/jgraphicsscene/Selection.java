package com.jgraphicsscene;

import com.jgraphicsscene.node.JGraphicsItem;

import java.awt.Rectangle;
import java.util.LinkedHashSet;
import java.util.Set;

public class Selection {
    private final Rectangle rubberBandRectangle = new Rectangle();
    private final Rectangle normalizedRubberBandRectangle = new Rectangle();
    private final Set<JGraphicsItem> selectedItems = new LinkedHashSet<>();
    private boolean rubberBandDragging = false;

    public boolean isRubberBandDragging() {
        return rubberBandDragging;
    }

    public void setRubberBandDragging(boolean rubberBandDragging) {
        this.rubberBandDragging = rubberBandDragging;
    }

    public Rectangle getRubberBandRectangle() {
        return rubberBandRectangle;
    }

    public Set<JGraphicsItem> getSelectedItems() {
        return selectedItems;
    }

    public JGraphicsItem getSelectedItem() {
        for (JGraphicsItem item : selectedItems) {
            return item;
        }
        return null;
    }

    public Rectangle getNormalizedRubberBandRectangle() {
        return normalizedRubberBandRectangle;
    }

    public void updateNormalizedRubberBandRectangle() {
        normalizedRubberBandRectangle.setLocation(rubberBandRectangle.x, rubberBandRectangle.y);
        normalizedRubberBandRectangle.setSize(rubberBandRectangle.width, rubberBandRectangle.height);
        Utils.normalizeRect(normalizedRubberBandRectangle);
    }
}