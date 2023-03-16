package com.jgraphicsscene;

import com.jgraphicsscene.node.JGraphicsItem;

import java.util.List;

public interface JGraphicsContainer {
    List<JGraphicsItem> getItems();

    JGraphicsItem addItem(JGraphicsItem item);

    JGraphicsItem removeItem(JGraphicsItem item);
}