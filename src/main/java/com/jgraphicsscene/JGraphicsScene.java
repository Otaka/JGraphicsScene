package com.jgraphicsscene;

import com.jgraphicsscene.events.MouseEvent;
import com.jgraphicsscene.events.SceneItemRemovedEvent;
import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.utils.Transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class JGraphicsScene extends JGraphicsContainer {
    public static final int MOUSE_EVENT_CLICK = 1 << 0;
    public static final int MOUSE_EVENT_PRESS = 1 << 1;
    public static final int MOUSE_EVENT_RELEASE = 1 << 2;
    public static final int MOUSE_EVENT_MOVE = 1 << 3;
    public static final int MOUSE_EVENT_WHEEL = 1 << 4;
    private final static List<Point2D> tempPoints = new ArrayList<>();
    private final static List<AffineTransform> tempTransform = new ArrayList<>();
    private final List<JGraphicsItem> items = new ArrayList<>();
    private final List<SceneItemRemovedEvent> itemRemovedEvents = new ArrayList<>();
    private final Transform transform = new Transform();
    private ManipulationHandlerFactory manipulationHandlerFactory;
    private JGraphicsView view;

    public JGraphicsScene() {
        this.manipulationHandlerFactory = new ManipulationHandlerFactory(this);
    }

    public static Point2D getTempPoint(float x, float y) {
        Point2D p = getTempPoint();
        p.setLocation(x, y);
        return p;
    }

    public static Point2D getTempPoint() {
        if (tempPoints.isEmpty()) {
            return new Point2D.Float();
        }
        return tempPoints.remove(tempPoints.size() - 1);
    }

    public static void disposeTempPoint(Point2D point) {
        tempPoints.add(point);
    }

    public static AffineTransform getTempTransform() {
        if (tempTransform.isEmpty()) {
            return new AffineTransform();
        }
        return tempTransform.remove(tempTransform.size() - 1);
    }

    public static void disposeTempTransform(AffineTransform transform) {
        tempTransform.add(transform);
    }

    @Override
    public JGraphicsScene getScene() {
        return this;
    }

    public JGraphicsView getView() {
        return view;
    }

    void setView(JGraphicsView view) {
        this.view = view;
    }

    public Transform getTransform() {
        return transform;
    }

    public ManipulationHandlerFactory getManipulationHandlerFactory() {
        return manipulationHandlerFactory;
    }

    public void setManipulationHandlerFactory(ManipulationHandlerFactory manipulationHandlerFactory) {
        this.manipulationHandlerFactory = manipulationHandlerFactory;
    }

    public List<JGraphicsItem> getItems() {
        return items;
    }

    public JGraphicsItem addItem(JGraphicsItem item) {
        item.setScene(this);
        item.setParent(this);
        items.add(item);
        setRequiresSorting(true);
        return item;
    }

    @Override
    public JGraphicsItem removeItem(JGraphicsItem item) {
        item.setScene(null);
        items.remove(item);
        fireItemRemoved(item);
        return item;
    }

    public void clearScene() {
        List<JGraphicsItem> tempItemsList = new ArrayList<>(items);
        for (JGraphicsItem item : tempItemsList) {
            removeItem(item);
        }
    }

    public void addSceneItemRemovedEvent(SceneItemRemovedEvent event) {
        itemRemovedEvents.add(event);
    }

    public void removeSceneItemRemovedEvent(SceneItemRemovedEvent event) {
        itemRemovedEvents.remove(event);
    }

    public void fireRedraw() {
        if (view != null) {
            view.repaint();
        }
    }

    public void fireItemRemoved(JGraphicsItem item) {
        for (int i = 0; i < itemRemovedEvents.size(); i++) {
            itemRemovedEvents.get(i).removedItem(item);
        }
    }

    public void paint(PaintContext p, Selection selection) {
        List<JGraphicsItem> items = getItemsZOrdered();
        for (int i = 0; i < items.size(); i++) {
            JGraphicsItem item = items.get(i);
            item.paint(p, selection);
        }
    }

    public void mouseEvent(MouseEvent e, Selection selection) {
        List<JGraphicsItem> items = getItemsZOrdered();
        for (int i = items.size() - 1; i >= 0; i--) {
            mouseEvent(e, items.get(i), selection);
        }
    }

    public void mouseEvent(MouseEvent e, JGraphicsItem item, Selection selection) {
        List<JGraphicsItem> childrenItems = item.getItemsZOrdered();
        for (int i = childrenItems.size() - 1; i >= 0; i--) {
            mouseEvent(e, childrenItems.get(i), selection);
        }

        if (item.hasFlag(JGraphicsItem.ItemIsSelectable)) {
            if (selection.getSelectedItems().isEmpty() || selection.isRubberBandDragging()) {
                if (item.isVisible() && item.intersect(selection.getNormalizedRubberBandRectangle())) {
                    selection.addSelectedItem(item);
                }
            }
        }
    }

    public List<JGraphicsItem> getItemsAtPoint_scene(float sceneMouseX, float sceneMouseY) {
        Point2D p = getTempPoint(sceneMouseX, sceneMouseY);
        view.mapSceneToView(p);
        disposeTempPoint(p);
        return getItemsAtPoint_view((float) p.getX(), (float) p.getY());
    }

    public List<JGraphicsItem> getItemsAtPoint_view(float viewMouseX, float viewMouseY) {
        List<JGraphicsItem> result = new ArrayList<>();
        List<JGraphicsItem> items = getItemsZOrdered();
        for (int i = items.size() - 1; i >= 0; i--) {
            getItemsAtPoint_view(viewMouseX, viewMouseY, items.get(i), result);
        }
        return result;
    }

    private void getItemsAtPoint_view(float viewMouseX, float viewMouseY, JGraphicsItem item, List<JGraphicsItem> result) {
        List<JGraphicsItem> childrenItems = item.getItemsZOrdered();
        for (int i = childrenItems.size() - 1; i >= 0; i--) {
            getItemsAtPoint_view(viewMouseX, viewMouseY, childrenItems.get(i), result);
        }

        if (item.contains(viewMouseX, viewMouseY)) {
            result.add(item);
        }
    }
}