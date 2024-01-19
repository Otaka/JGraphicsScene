package com.jgraphicsscene;

import com.jgraphicsscene.events.ItemChangedEvent;
import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.node.JGraphicsItem;

import java.awt.geom.Point2D;
import java.util.function.Supplier;

public class ManipulatorProcessor {
    private final JGraphicsItem manipulatorObject;
    private final JGraphicsItem manipulatorOwner;
    private final OnManipulatorMoved onManipulatorMoved;
    private final Supplier<Point2D> initPositionSupplier;
    private final Point2D oldManipulatorPosition = new Point2D.Float();
    private Integer manipulatorId;
    private ItemChangedEvent internalMovedEvent;

    public ManipulatorProcessor(JGraphicsItem manipulatorObject, JGraphicsItem manipulatorOwner, Supplier<Point2D> manipPositionSupplierItemSpace, OnManipulatorMoved onManipulatorMoved, Integer manipulatorId) {
        this.manipulatorId = manipulatorId;
        this.manipulatorObject = manipulatorObject;
        this.manipulatorOwner = manipulatorOwner;
        this.onManipulatorMoved = onManipulatorMoved;
        this.initPositionSupplier = manipPositionSupplierItemSpace;

        Point2D initialManipPositionOwnerSpace = manipPositionSupplierItemSpace.get();
        oldManipulatorPosition.setLocation((float) initialManipPositionOwnerSpace.getX(), (float) initialManipPositionOwnerSpace.getY());
    }

    public JGraphicsItem getManipulatorObject() {
        return manipulatorObject;
    }

    public JGraphicsItem getManipulatorOwner() {
        return manipulatorOwner;
    }

    public ManipulatorProcessor start() {
        Point2D initialManipPosition = updateManipulatorPosition();
        oldManipulatorPosition.setLocation(initialManipPosition);
        internalMovedEvent = (itemChangedType, item, arg) -> {
            if (itemChangedType == ItemChangedType.MousePress) {
                manipulatorOwner.fireEvent(ItemChangedType.ManipulatorPressed, manipulatorId);
            }
            if (itemChangedType == ItemChangedType.Position) {
                Point2D oldManipPositionItemSpace = manipulatorOwner.mapSceneToItemImmutable(oldManipulatorPosition);
                Point2D manipItemSpacePos = manipulatorOwner.mapSceneToItemImmutable(manipulatorObject.getGlobalPosition());
                float dx = (float) (manipItemSpacePos.getX() - oldManipPositionItemSpace.getX());
                float dy = (float) (manipItemSpacePos.getY() - oldManipPositionItemSpace.getY());
                oldManipulatorPosition.setLocation(manipulatorObject.getGlobalPosition());
                onManipulatorMoved.manipulatorMoved(manipulatorObject, dx, dy, this);
                manipulatorOwner.fireEvent(ItemChangedType.ManipulatorMoved, manipulatorId);
                manipulatorOwner.dirtyTransform();
            }
            if (itemChangedType == ItemChangedType.MouseRelease) {
                manipulatorOwner.fireEvent(ItemChangedType.ManipulatorReleased, manipulatorId);
            }

            return false;
        };
        manipulatorObject.addEvent(internalMovedEvent);
        return this;
    }

    public Point2D updateManipulatorPosition() {
        Point2D pos = initPositionSupplier.get();
        Point2D initialManipPosition = manipulatorOwner.mapItemToSceneImmutable(pos);
        manipulatorObject.setPosition((float) initialManipPosition.getX(), (float) initialManipPosition.getY(), false);
        manipulatorObject.dirtyTransform();
        return initialManipPosition;
    }
}
