package com.jgraphicsscene.node;

import com.jgraphicsscene.JGraphicsContainer;
import com.jgraphicsscene.JGraphicsScene;
import com.jgraphicsscene.JGraphicsView;
import com.jgraphicsscene.PaintContext;
import com.jgraphicsscene.Selection;
import com.jgraphicsscene.effects.JGraphicsAbstractEffect;
import com.jgraphicsscene.events.ItemChangedEvent;
import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.utils.Transform;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class JGraphicsItem extends JGraphicsContainer {
    public static final int ItemIsMovable = 1 << 0;
    public static final int ItemIsSelectable = 1 << 1;
    public static final int ItemIsFocusable = 1 << 2;
    public static final int ItemIgnoresParentScale = 1 << 3;
    public static final int ItemShowManipulationHandlers = 1 << 4;
    public static final int ItemAppliesOwnEffectToChildren = 1 << 5;
    private static final Stroke SELECTION_STROKE = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0);
    protected final Transform transform = new Transform();
    protected final Transform cachedSceneViewTransform = new Transform();
    private final List<ItemChangedEvent> events = new ArrayList<>();
    protected Shape viewBoundingShape;
    protected int viewTransformVersion = -1;
    protected float x;
    protected float y;
    protected float rotation;
    protected JGraphicsScene scene;
    boolean visible = true;
    private float scale = 1;
    private List<JGraphicsItem> items;
    private JGraphicsContainer parent;
    private int flag;
    private float zOrder = 0;
    private float zOrderOffset = 0;
    private List<JGraphicsAbstractEffect> effects;

    public JGraphicsItem addEffect(JGraphicsAbstractEffect effect) {
        if (effects == null) {
            effects = new ArrayList<>();
        }
        effects.add(effect);
        return this;
    }

    public List<JGraphicsAbstractEffect> getEffects() {
        return effects == null ? Collections.emptyList() : effects;
    }

    public JGraphicsItem removeEffect(JGraphicsAbstractEffect effect) {
        getEffects().remove(effect);
        if (effects.isEmpty()) effects = null;
        return this;
    }


    public JGraphicsItem addEvent(ItemChangedEvent event) {
        events.add(event);
        return this;
    }

    public JGraphicsItem deleteEvent(ItemChangedEvent event) {
        events.remove(event);
        return this;
    }

    public JGraphicsItem deleteEvents() {
        events.clear();
        return this;
    }

    public void fireEvent(ItemChangedType type, Object arg) {
        for (int i = 0; i < events.size(); i++) {
            events.get(i).changed(type, this, arg);
        }
    }

    public float getTotalZOrder() {
        return zOrder + zOrderOffset;
    }

    public float getZOrder() {
        return zOrder;
    }

    public JGraphicsItem setZOrder(float zOrder) {
        this.zOrder = zOrder;
        if (parent != null) {
            parent.setRequiresSorting(true);
        }
        return this;
    }

    public float getZOrderOffset() {
        return zOrderOffset;
    }

    public JGraphicsItem setZOrderOffset(float zOrderOffset) {
        this.zOrderOffset = zOrderOffset;
        if (parent != null) {
            parent.setRequiresSorting(true);
        }
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getRotation() {
        return rotation;
    }

    public JGraphicsItem setRotation(float angleRad) {
        if (Float.isNaN(angleRad)) {
            throw new IllegalArgumentException("angle cannot be NaN");
        }
        this.rotation = angleRad;
        return this;
    }

    public float getX() {
        return x;
    }

    public float getXCenter() {
        return x + getWidth() / 2;
    }

    public float getY() {
        return y;
    }

    public float getYCenter() {
        return y + getHeight() / 2;
    }

    public Point2D getGlobalPosition() {
        Point2D p = new Point2D.Float(x, y);
        return getParentTransform().transformMut(p);
    }

    public Point2D getGlobalPositionCenter() {
        float zoom = getScene().getView().getZoom();
        Point2D p = new Point2D.Float(x + (getWidth() / 2), y + (getHeight() / 2));
        return getParentTransform().transformMut(p);
    }

    public Point2D getPositionInView(JGraphicsView view) {
        Point2D p = getGlobalPosition();
        return view.mapSceneToView(p);
    }

    public JGraphicsItem setPositionByCenter(float x, float y, boolean global) {
        if (hasFlag(ItemIgnoresParentScale)) {
            float zoom = getScene().getView().getZoom();
            x -= (getWidth() / 2);
            y -= (getHeight() / 2);
            return setPosition(x, y, global);
        } else {
            x -= (getWidth() / 2);
            y -= (getHeight() / 2);
            return setPosition(x, y, global);
        }
    }

    public JGraphicsItem setPositionByCenter(Point2D pos, boolean global) {
        return setPositionByCenter((float) pos.getX(), (float) pos.getY(), global);
    }

    public JGraphicsItem setPosition(float x, float y, boolean global) {
        if (Float.isNaN(x)) {
            throw new IllegalArgumentException("x is NaN");
        }
        if (Float.isNaN(y)) {
            throw new IllegalArgumentException("y is NaN");
        }
        if (!global) {
            this.x = x;
            this.y = y;
        } else {
            Point2D point = JGraphicsScene.getTempPoint(x, y);
            Point2D newPoint = getParentTransform().inverseTransformPoint(point, new Point2D.Float());
            this.x = (float) newPoint.getX();
            this.y = (float) newPoint.getY();
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
        if (scene == null) {
            scene = getParent().getScene();
        }
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
        child.setScene(scene);
        items.add(child);
        child.setParent(this);
        setRequiresSorting(true);
        return this;
    }

    public JGraphicsContainer getParent() {
        return parent;
    }

    public void setParent(JGraphicsContainer parent) {
        if (this.parent != null && this.parent.getItems() != null) {
            this.parent.getItems().remove(this);
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

    public boolean contains(float x, float y) {
        return getViewBoundingShape().contains(x, y);
    }


    public boolean intersect(Rectangle rectangle) {
        return getViewBoundingShape().intersects(rectangle);
    }

    public Transform getTransform() {
        if (transform.isDirty()) {
            if (parent != null) {
                transform.setFromTransform(parent.getTransform());
            }
            transform.translate(x, y);
            transform.rotate(rotation);
            transform.scale(scale, scale);
            transform.clearDirty();
            afterTransformRecalculation();
        }
        return transform;
    }

    public Transform getSceneViewTransform() {
        Transform itemTransform = getTransform();
        Transform viewTransform = getScene().getView().getViewTransform();
        int viewTransformVersion = viewTransform.getVersion();
        if (!cachedSceneViewTransform.isDirty() && cachedSceneViewTransform.getVersion() == viewTransformVersion) {
            return cachedSceneViewTransform;
        }

        if (!hasFlag(ItemIgnoresParentScale)) {
            cachedSceneViewTransform.setFromTransform(viewTransform);
            cachedSceneViewTransform.concatenate(itemTransform);
            cachedSceneViewTransform.setVersion(viewTransformVersion);
        } else {
            float initialViewZoom = getScene().getView().getInitialZoom();
            Point2D positionInView = getPositionInView(getScene().getView());
            cachedSceneViewTransform.setToIdentity();
            cachedSceneViewTransform.setToScale(initialViewZoom, initialViewZoom);
            cachedSceneViewTransform.translate(positionInView.getX() / initialViewZoom, positionInView.getY() / initialViewZoom);
            cachedSceneViewTransform.rotate(rotation);
            cachedSceneViewTransform.scale(scale, scale);
        }
        cachedSceneViewTransform.clearDirty();
        return cachedSceneViewTransform;
    }

    public final void paint(PaintContext p, Selection selection) {
        Graphics2D g = p.getGraphics();
        AffineTransform oldAffineTransform = g.getTransform();
        Transform sceneViewTransform = getSceneViewTransform();
        g.setTransform(sceneViewTransform.getAffineTransform());

        boolean effectsApplied = false;
        if (effects != null) {
            effectsApplied = true;
            for (int i = 0; i < effects.size(); i++) effects.get(i).beforeRender(p);
        }

        if (p.isRenderingEnabled() && isVisible()) {
            paintItem(p);
        }

        if (effectsApplied && !hasFlag(ItemAppliesOwnEffectToChildren)) {
            effectsApplied = false;
            if (effects != null) {
                for (int i = 0; i < effects.size(); i++) effects.get(i).afterRender(p);
            }
        }
        g.setTransform(oldAffineTransform);
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                JGraphicsItem item = items.get(i);
                item.paint(p, selection);
            }
        }

        if (effectsApplied) {
            for (int i = 0; i < effects.size(); i++) effects.get(i).afterRender(p);
        }

        if (p.isRenderingEnabled() && isVisible() && selection.getSelectedItems().contains(this)) {
            g.setTransform(sceneViewTransform.getAffineTransform());

            Shape viewBoundingShape = getViewBoundingShape();
            g.setTransform(p.getOriginalTransform());
            Stroke oldStroke = g.getStroke();
            setSelectionStroke(g);
            p.setXORMode(Color.WHITE);
            g.setColor(Color.black);
            g.draw(viewBoundingShape);
            g.setStroke(oldStroke);
            p.restoreXORMode();

            g.setTransform(oldAffineTransform);
        }
    }

    public Transform getParentTransform() {
        if (parent != null) {
            return parent.getTransform();
        }
        throw new IllegalStateException("Cannot get parent transform because the item is not in a container");
    }

    protected void afterTransformRecalculation() {
        //filled by subclasses
    }

    public JGraphicsItem dirtyTransform() {
        invalidateViewBoundingShape();
        cachedSceneViewTransform.setDirty();
        transform.setDirty();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).dirtyTransform();
            }
        }
        return this;
    }

    protected void setSelectionStroke(Graphics2D g) {
        g.setStroke(SELECTION_STROKE);
    }

    public Shape mapItemToSceneImmutable(Shape shape) {
        return getTransform().getAffineTransform().createTransformedShape(shape);
    }

    public Point2D mapItemToSceneMut(Point2D point) {
        return getTransform().transformMut(point);
    }

    public Point2D mapItemToSceneImmutable(Point2D point) {
        return getTransform().transform(point, new Point2D.Float());
    }

    public Point2D mapSceneToItemMut(Point2D point) {
        return getTransform().inverseTransformPointMut(point);
    }

    public Point2D mapSceneToItemImmutable(Point2D point) {
        return getTransform().inverseTransformPoint(point, new Point2D.Float());
    }

    public Shape getViewBoundingShape() {
        int actualViewTransformVersion = getScene().getView().getViewTransform().getVersion();
        if (viewBoundingShape != null && actualViewTransformVersion == viewTransformVersion) {
            return viewBoundingShape;
        }
        viewTransformVersion = actualViewTransformVersion;
        viewBoundingShape = getSceneViewTransform().transformImmutable(createBoundingShape());
        return viewBoundingShape;
    }

    public abstract Shape createBoundingShape();

    public void invalidateViewBoundingShape() {
        viewBoundingShape = null;
    }

    protected abstract void paintItem(PaintContext p);

    protected abstract float getWidth();

    protected abstract float getHeight();

    public void onSelect() {
    }

    public void onDeselect() {
    }

    public List<JGraphicsItem> createManipulators() {
        return Collections.emptyList();
    }
}