package com.jgraphicsscene;

import com.jgraphicsscene.events.MouseEvent;
import com.jgraphicsscene.events.SceneRedrawEvent;
import com.jgraphicsscene.node.JGraphicsItem;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class JGraphicsScene implements JGraphicsContainer {
    public static final int MOUSE_EVENT_CLICK = 1 << 0;
    public static final int MOUSE_EVENT_PRESS = 1 << 1;
    public static final int MOUSE_EVENT_RELEASE = 1 << 2;
    public static final int MOUSE_EVENT_MOVE = 1 << 3;
    private final static List<Point2D> tempPoints = new ArrayList<>();
    private final static List<AffineTransform> tempTransform = new ArrayList<>();
    private final List<JGraphicsItem> items = new ArrayList<>();
    private final List<SceneRedrawEvent> redrawEvents = new ArrayList<>();

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

    public List<JGraphicsItem> getItems() {
        return items;
    }

    public JGraphicsItem addItem(JGraphicsItem item) {
        item.setScene(this);
        items.add(item);
        return item;
    }

    @Override
    public JGraphicsItem removeItem(JGraphicsItem item) {
        item.setScene(null);
        items.remove(item);
        return item;
    }

    public void addSceneRedrawEvent(SceneRedrawEvent event) {
        redrawEvents.add(event);
    }

    public void removeSceneRedrawEvent(SceneRedrawEvent event) {
        redrawEvents.remove(event);
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void fireRedraw() {
        for (int i = 0; i < redrawEvents.size(); i++) {
            redrawEvents.get(i).redraw(this);
        }
    }

    public void paint(Graphics2D g, Selection selection) {
        List<JGraphicsItem> items = getItems();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).paint(g, selection);
        }
    }

    public void mouseEvent(MouseEvent e, Selection selection) {
        List<JGraphicsItem> items = getItems();
        for (int i = items.size() - 1; i >= 0; i--) {
            mouseEvent(e, items.get(i), selection);
        }
    }

    public void mouseEvent(MouseEvent e, JGraphicsItem item, Selection selection) {
        List<JGraphicsItem> childrenItems = item.getItems();
        for (int i = childrenItems.size() - 1; i >= 0; i--) {
            mouseEvent(e, childrenItems.get(i), selection);
        }

        if ((item.getFlag() & JGraphicsItem.ItemIsSelectable) != 0) {
            if (selection.getSelectedItems().isEmpty() || selection.isRubberBandDragging()) {
                if (item.intersect(selection.getNormalizedRubberBandRectangle())) {
                    selection.getSelectedItems().add(item);
                }
            }
        }
    }
}