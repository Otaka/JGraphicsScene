package com.jgraphicsscene.node;

import com.jgraphicsscene.JGraphicsContainer;
import com.jgraphicsscene.JGraphicsScene;
import com.jgraphicsscene.Selection;
import com.jgraphicsscene.Utils;
import com.jgraphicsscene.events.ItemChangedEvent;
import com.jgraphicsscene.events.ItemChangedType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JGraphicsItem implements JGraphicsContainer {
    public static final int ItemIsMovable = 1 << 0;
    public static final int ItemIsSelectable = 1 << 1;
    public static final int ItemIsFocusable = 1 << 2;
    public static final int ItemIgnoresTransformations = 1 << 3;
    public static final int ItemSendsMoved = 1 << 4;
    private static Stroke SELECTION_STROKE = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0);
    private final AffineTransform transform = new AffineTransform();
    private final List<ItemChangedEvent> events = new ArrayList<>();
    protected JGraphicsScene scene;
    protected float x;
    protected float y;
    protected float rotation;
    private List<JGraphicsItem> items;
    private JGraphicsItem parent;
    private float scale = 1;
    private boolean dirtyTransform = true;
    private int flag;

    public JGraphicsItem addEvent(ItemChangedEvent event) {
        events.add(event);
        return this;
    }

    public JGraphicsItem deleteEvent(ItemChangedEvent event) {
        events.remove(event);
        return this;
    }

    public void fireEvent(ItemChangedType type) {
        for (int i = 0; i < events.size(); i++) {
            events.get(i).changed(type, this);
        }
    }

    public float getRotation() {
        return rotation;
    }

    public JGraphicsItem setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Point2D getGlobalPosition() {
        Point2D p = new Point2D.Float(x, y);
        getParentTransform().transform(p, p);
        return p;
    }

    public JGraphicsItem setPosition(float x, float y, boolean global) {
        if (!global) {
            this.x = x;
            this.y = y;
        } else {
            Point2D point = JGraphicsScene.getTempPoint(x, y);
            Utils.inverseTransformPoint(point, getParentTransform());
            this.x = (float) point.getX();
            this.y = (float) point.getY();
            JGraphicsScene.disposeTempPoint(point);
        }
        return this;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public JGraphicsScene getScene() {
        return scene;
    }

    public JGraphicsItem setScene(JGraphicsScene scene) {
        this.scene = scene;
        return this;
    }

    public int getFlag() {
        return flag;
    }

    public JGraphicsItem setFlag(int flag) {

        this.flag = flag;
        return this;
    }

    public boolean hasFlag(int flag) {
        return (this.flag & flag) != 0;
    }

    public List<JGraphicsItem> getItems() {
        return items == null ? Collections.emptyList() : items;
    }

    @Override
    public JGraphicsItem addItem(JGraphicsItem child) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(child);
        child.setParent(this);
        return this;
    }

    public void setParent(JGraphicsItem parent) {
        if (this.parent != null && this.parent.items != null) {
            this.parent.items.remove(this);
        }
        this.parent = parent;
    }

    @Override
    public JGraphicsItem removeItem(JGraphicsItem item) {
        if (items != null) {
            items.remove(item);
            if (items.isEmpty()) items = null;
        }
        return this;
    }

    public abstract boolean contains(float x, float y);

    public abstract boolean intersect(Rectangle rectangle);

    public final void paint(Graphics2D g, Selection selection) {
        AffineTransform oldAffineTransform = g.getTransform();
        AffineTransform tempTransform = JGraphicsScene.getTempTransform();
        tempTransform.setToIdentity();
        tempTransform.concatenate(oldAffineTransform);
        tempTransform.concatenate(getTransform());
        g.setTransform(tempTransform);
        paintItem(g, oldAffineTransform, selection);
        if (selection.getSelectedItems().contains(this)) {
            g.setTransform(new AffineTransform());
            Stroke oldStroke = g.getStroke();
            setSelectionStroke(g);
            g.setXORMode(Color.WHITE);
            g.setColor(Color.black);
            g.draw(oldAffineTransform.createTransformedShape(getBoundingBox()));
            g.setStroke(oldStroke);
            g.setPaintMode();
        }
        g.setTransform(oldAffineTransform);
        JGraphicsScene.disposeTempTransform(tempTransform);

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).paint(g, selection);
            }
        }
    }

    public AffineTransform getParentTransform() {
        if (parent != null) {
            return parent.getTransform();
        } else {
            AffineTransform transform = new AffineTransform();
            transform.setToIdentity();
            return transform;
        }
    }

    public AffineTransform getTransform() {
        if (dirtyTransform) {
            if (parent != null) {
                transform.setTransform(parent.getTransform());
            } else {
                transform.setToIdentity();
            }
            transform.translate(x, y);
            transform.rotate(rotation);
            transform.scale(scale, scale);
            dirtyTransform = false;
        }
        return transform;
    }

    public void dirtyTransform() {
        dirtyTransform = true;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).dirtyTransform();
            }
        }
    }

    protected void setSelectionStroke(Graphics2D g) {
        g.setStroke(SELECTION_STROKE);
    }

    public Shape mapItemToSceneImmutable(Shape shape) {
        return getTransform().createTransformedShape(shape);
    }

    public Point2D mapItemToScene(Point2D point) {
        return getTransform().transform(point, point);
    }

    public Point2D mapSceneToItem(Point2D point) {
        return Utils.inverseTransformPoint(point, getTransform());
    }

    public abstract Shape getBoundingBox();

    protected abstract void paintItem(Graphics2D g, AffineTransform oldAffineTransform, Selection selection);
}