package com.jgraphicsscene;

import com.jgraphicsscene.node.JGraphicsEllipseItem;
import com.jgraphicsscene.node.JGraphicsItem;

import java.awt.Color;
import java.awt.geom.Point2D;

public class ManipulationHandlerFactory {
    private final JGraphicsScene scene;

    public ManipulationHandlerFactory(JGraphicsScene scene) {
        this.scene = scene;
    }

    public JGraphicsItem createManipulationHandler(JGraphicsItem parent) {
        return createManipulationHandler(parent, 0f, 0f);
    }

    public JGraphicsItem createManipulationHandler(JGraphicsItem parent, Point2D position) {
        return createManipulationHandler(parent, (float) position.getX(), (float) position.getY());
    }

    public JGraphicsItem createManipulationHandler(JGraphicsItem parent, float x, float y) {
        JGraphicsEllipseItem ellipseItem = new JGraphicsEllipseItem(x, y, 10, 10);
        ellipseItem.setXOffset(-5f).setYOffset(-5f);
        ellipseItem.setFillColor(Color.WHITE);
        ellipseItem.setBorderColor(Color.BLACK);
        ellipseItem.setZOrder(1000000);
        ellipseItem.setFlag(JGraphicsItem.ItemIsMovable | JGraphicsItem.ItemIgnoresParentScale
        );
        scene.addItem(ellipseItem);

        return ellipseItem;
    }
}
