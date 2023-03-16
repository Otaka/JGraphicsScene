package com.jgraphicsscene.events;

import com.jgraphicsscene.node.JGraphicsItem;

public interface MouseEventHandler {
    void handle(JGraphicsItem item, MouseEvent event);
}