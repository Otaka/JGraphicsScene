package com.jgraphicsscene.events;

import com.jgraphicsscene.node.JGraphicsItem;

public interface ItemChangedEvent {
    boolean changed(ItemChangedType itemChangedType, JGraphicsItem item, Object arg);
}