package com.jgraphicsscene;

import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.utils.Transform;

import java.util.Comparator;
import java.util.List;

public abstract class JGraphicsContainer {
    boolean requiresSorting = false;

    abstract public List<JGraphicsItem> getItems();

    public List<JGraphicsItem> getItemsZOrdered() {
        List<JGraphicsItem> items = getItems();
        if (isRequiresSorting()) {
            items.sort(Comparator.comparingDouble(JGraphicsItem::getTotalZOrder));
            setRequiresSorting(false);
        }
        return items;
    }

    abstract public JGraphicsItem addItem(JGraphicsItem item);

    abstract public JGraphicsItem removeItem(JGraphicsItem item);

    boolean isRequiresSorting() {
        return requiresSorting;
    }

    public void setRequiresSorting(boolean requiresSorting) {
        this.requiresSorting = requiresSorting;
    }

    abstract public JGraphicsScene getScene();

    abstract public Transform getTransform();
}