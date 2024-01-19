package com.jgraphicsscene;

import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.events.MouseEventHandler;
import com.jgraphicsscene.events.SceneItemRemovedEvent;
import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.utils.Transform;
import com.martijncourteaux.multitouchgestures.GestureAdapter;
import com.martijncourteaux.multitouchgestures.MultiTouchGestureUtilities;
import com.martijncourteaux.multitouchgestures.event.MagnifyGestureEvent;
import com.martijncourteaux.multitouchgestures.event.ScrollGestureEvent;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class JGraphicsView extends JPanel {
    private final Color RUBBER_BAND_COLOR = new Color(127, 127, 255, 20);
    private final List<DragObject<JGraphicsItem>> draggedObjects = new ArrayList<>();
    private final Selection selection = new Selection();
    private final List<JGraphicsItem> manipulators = new ArrayList<>();
    private Transform viewTransform;
    private JGraphicsScene scene;
    private SceneItemRemovedEvent sceneItemRemovedEvent;
    private DragObject<?> panDrag = null;
    private boolean antialias = true;
    private int scrollX = 0;
    private int scrollY = 0;
    //we need this, because of the different scaling on different platforms. For example OsX with java 17 has scaling 2, but with java 8 - 1
    private float initialZoom = 1;
    private float zoom = 1;
    private boolean middleMousePanEnabled = true;
    private boolean wheelZoomEnabled = true;
    private boolean zoomToPointer = true;
    private DragMode dragMode = DragMode.NoDrag;
    private boolean draggedInLastPress = false;
    private boolean interactive = true;
    private boolean drawSceneBorders = true;
    private int sceneWidth = 1000;
    private int sceneHeight = 1000;
    private Color offSceneColor = Color.BLACK;
    private Cursor oldCursor;
    private long lastTouchPadAccessTime;
    private boolean globalAllowDragging = true;
    private List<MouseEventHandler> mouseEventHandlers = new ArrayList<>();
    private Timer redrawTimer;


    public JGraphicsView(JGraphicsScene scene) {
        this();
        setScene(scene);
    }

    public JGraphicsView() {
        setOpaque(true);
        setBackground(Color.WHITE);
        sceneItemRemovedEvent = this::sceneItemRemovedEvent;
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    zoom(initialZoom);
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseEvent(JGraphicsScene.MOUSE_EVENT_CLICK, e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseEvent(JGraphicsScene.MOUSE_EVENT_PRESS, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseEvent(JGraphicsScene.MOUSE_EVENT_RELEASE, e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseEvent(JGraphicsScene.MOUSE_EVENT_MOVE, e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseEvent(JGraphicsScene.MOUSE_EVENT_MOVE, e);
            }
        });
        addMouseWheelListener(e -> {
            if (wheelZoomEnabled) {
                //Quick hack, because Java do not know the difference between wheel scrolling and touchpad scrolling
                //that is why we need to check if the last touchpad access was more than 5 seconds ago
                if (System.currentTimeMillis() - lastTouchPadAccessTime < 5000) {
                    return;
                }
                handleZoom((float) (-0.1 * e.getPreciseWheelRotation()), e.getX(), e.getY());
            }
        });
        if (MultiTouchGestureUtilities.isSupported()) {
            MultiTouchGestureUtilities.addGestureListener(this, new GestureAdapter() {
                @Override
                public void magnify(MagnifyGestureEvent e) {
                    lastTouchPadAccessTime = System.currentTimeMillis();
                    Point2D mousePoint = JGraphicsScene.getTempPoint((float) e.getMouseX(), (float) e.getMouseY());
                    viewTransform.transformMut(mousePoint);
                    handleZoom((float) e.getMagnification(), (int) mousePoint.getX(), (int) mousePoint.getY());
                }

                @Override
                public void scroll(ScrollGestureEvent e) {
                    lastTouchPadAccessTime = System.currentTimeMillis();
                    scrollX += e.getDeltaX();
                    scrollY += e.getDeltaY();
                    updateViewTransform();
                    repaint();
                }
            });
        }

        Timer timer = new Timer(200, (e) -> repaint());
        timer.setRepeats(false);
        timer.start();
        redrawTimer = new Timer(500, (e) -> repaint());
        redrawTimer.setRepeats(true);
        redrawTimer.start();
    }

    public void dispose() {
        redrawTimer.stop();
        MultiTouchGestureUtilities.removeAllGestureListeners(this);
    }

    public boolean isGlobalAllowDragging() {
        return globalAllowDragging;
    }

    public void setGlobalAllowDragging(boolean globalAllowDragging) {
        this.globalAllowDragging = globalAllowDragging;
    }

    public void setScroll(float scrollX, float scrollY) {
        this.scrollX = (int) scrollX;
        this.scrollY = (int) scrollY;
        updateViewTransform();
        repaint();
    }

    private void handleZoom(float deltaZoom, int mouseX, int mouseY) {
        mouseX /= initialZoom;
        mouseY /= initialZoom;
        Point2D cursorLocationSceneSpace = JGraphicsScene.getTempPoint(mouseX, mouseY);
        viewTransform.inverseTransformPointMut(cursorLocationSceneSpace);
        zoom = (zoom + deltaZoom);
        zoom = Math.max(zoom, 0.1f);
        updateViewTransform();
        if (zoomToPointer) {
            cursorLocationSceneSpace = mapSceneToView(cursorLocationSceneSpace);
            scrollX += mouseX - cursorLocationSceneSpace.getX();
            scrollY += mouseY - cursorLocationSceneSpace.getY();
            updateViewTransform();
        }
        JGraphicsScene.disposeTempPoint(cursorLocationSceneSpace);
        repaint();
    }

    public Color getOffSceneColor() {
        return offSceneColor;
    }

    public JGraphicsView setOffSceneColor(Color offSceneColor) {
        this.offSceneColor = offSceneColor;
        return this;
    }

    public boolean isAntialias() {
        return antialias;
    }

    public JGraphicsView setAntialias(boolean antialias) {
        this.antialias = antialias;
        return this;
    }

    public boolean isDrawSceneBorders() {
        return drawSceneBorders;
    }

    public JGraphicsView setDrawSceneBorders(boolean drawSceneBorders) {
        this.drawSceneBorders = drawSceneBorders;
        return this;
    }

    public int getSceneWidth() {
        return sceneWidth;
    }

    public JGraphicsView setSceneWidth(int sceneWidth) {
        this.sceneWidth = sceneWidth;
        return this;
    }

    public int getSceneHeight() {
        return sceneHeight;
    }

    public JGraphicsView setSceneHeight(int sceneHeight) {
        this.sceneHeight = sceneHeight;
        return this;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public JGraphicsView setInteractive(boolean interactive) {
        this.interactive = interactive;
        return this;
    }

    public DragMode getDragMode() {
        return dragMode;
    }

    public JGraphicsView setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
        return this;
    }

    public boolean isMiddleMousePanEnabled() {
        return middleMousePanEnabled;
    }

    public JGraphicsView setMiddleMousePanEnabled(boolean middleMousePanEnabled) {
        this.middleMousePanEnabled = middleMousePanEnabled;
        return this;
    }

    public boolean isWheelZoomEnabled() {
        return wheelZoomEnabled;
    }

    public JGraphicsView setWheelZoomEnabled(boolean wheelZoomEnabled) {
        this.wheelZoomEnabled = wheelZoomEnabled;
        return this;
    }

    public boolean isZoomToPointer() {
        return zoomToPointer;
    }

    public JGraphicsView setZoomToPointer(boolean zoomToPointer) {
        this.zoomToPointer = zoomToPointer;
        return this;
    }

    public void addViewMouseHandler(MouseEventHandler handler) {
        mouseEventHandlers.add(handler);
    }

    public void removeViewMouseHandler(MouseEventHandler handler) {
        mouseEventHandlers.remove(handler);
    }

    public JGraphicsView scroll(int x, int y) {
        scrollX = x;
        scrollY = y;
        updateViewTransform();
        return this;
    }

    public JGraphicsView zoom(float zoom) {
        this.zoom = zoom;
        updateViewTransform();
        return this;
    }

    public float getInitialZoom() {
        return initialZoom;
    }

    public int getScrollX() {
        return scrollX;
    }

    public int getScrollY() {
        return scrollY;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        updateViewTransform();
        repaint();
    }

    private void updateViewTransform() {
        if (viewTransform == null) return;
        viewTransform.setDirty();
        viewTransform.setToIdentity();
        viewTransform.translate(scrollX, scrollY);
        viewTransform.scale(zoom, zoom);
        viewTransform.clearDirty();
    }

    public JGraphicsView setScene(JGraphicsScene scene) {
        if (this.scene != null) {
            this.scene.removeSceneItemRemovedEvent(sceneItemRemovedEvent);
            this.scene.setView(null);
        }
        this.scene = scene;
        if (scene != null) {
            scene.setView(this);
            scene.addSceneItemRemovedEvent(sceneItemRemovedEvent);
        }
        return this;
    }

    public void cancelRubberBandSelection() {
        selection.setRubberBandDragging(false);
    }


    private void sceneItemRemovedEvent(JGraphicsItem item) {
        selection.getSelectedItems().remove(item);
    }

    @Override
    protected void paintComponent(Graphics _g) {
        if (scene == null) {
            return;
        }

        Graphics2D g = (Graphics2D) _g;
        if (viewTransform == null) {
            //this is first paint. We should extract scaling from the view. On OsX it is 2.0, that is why everything is scaled in wrong way
            viewTransform = new Transform();
            AffineTransform originalViewTransform = g.getTransform();
            zoom = (float) originalViewTransform.getScaleX();
            initialZoom = zoom;
            updateViewTransform();
        }
        AffineTransform oldTransformBackup = g.getTransform();
        AffineTransform rawViewTransform = new AffineTransform(oldTransformBackup);
        rawViewTransform.scale(1 / initialZoom, 1 / initialZoom);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        if (drawSceneBorders) {
            g.setColor(getOffSceneColor());
            g.fillRect(0, 0, getSceneWidth(), getSceneHeight());
            g.setTransform(viewTransform.getAffineTransform());
            g.setColor(getBackground());
            g.fillRect(0, 0, getSceneWidth(), getSceneHeight());
        } else {
            g.setColor(getBackground());
            g.fillRect(-1, -1, getWidth() + 2, getHeight() + 2);
            g.setTransform(viewTransform.getAffineTransform());
        }

        PaintContext p = new PaintContext(g, rawViewTransform);
        scene.paint(p, selection);

        g.setTransform(rawViewTransform);
        g.setColor(RUBBER_BAND_COLOR);
        if (selection.isRubberBandDragging() && !(selection.getRubberBandRectangle().width == 1 && selection.getRubberBandRectangle().height == 1)) {
            Rectangle r = selection.getNormalizedRubberBandRectangle();
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setColor(Color.black);
            g.drawRect(r.x, r.y, r.width, r.height);
        }
        g.setColor(Color.WHITE);
        g.setTransform(oldTransformBackup);
    }

    private void mouseEvent(int mouseEventType, MouseEvent e) {
        if (scene == null || viewTransform == null) {
            return;
        }
        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_PRESS && !isFocusOwner() && isFocusable()) {
            requestFocusInWindow();
        }

        if (!mouseEventHandlers.isEmpty()) {
            com.jgraphicsscene.events.MouseEvent event = new com.jgraphicsscene.events.MouseEvent(mouseEventType, e.getX() * initialZoom, e.getY() * initialZoom, e.getButton(), e.getModifiersEx(), e.getClickCount());
            for (MouseEventHandler handler : mouseEventHandlers) {
                handler.handle(null, event);
                if (event.isStopPropagation()) return;
            }
        }
        transformMouseEvent(e);
        if (processPan(mouseEventType, e)) {
            return;
        }
        if (!interactive) {
            return;
        }
        Point2D point = mapViewToScene(JGraphicsScene.getTempPoint(e.getX(), e.getY()));
        float sceneMouseX = (float) point.getX();
        float sceneMouseY = (float) point.getY();
        JGraphicsScene.disposeTempPoint(point);
        float viewMouseX = (float) e.getX();
        float viewMouseY = (float) e.getY();

        com.jgraphicsscene.events.MouseEvent me = new com.jgraphicsscene.events.MouseEvent(mouseEventType, viewMouseX, viewMouseY, e.getButton(), e.getModifiersEx());

        //BEFORE SEARCH
        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_PRESS) {
            List<JGraphicsItem> itemsUnderMouse = scene.getItemsAtPoint_view(viewMouseX, viewMouseY);
            if (!itemsUnderMouse.isEmpty()) {
                JGraphicsItem item = getFirstMovableOrSelectableItem(itemsUnderMouse);
                if (item != null) {
                    item.fireEvent(ItemChangedType.MousePress, e.getButton());
                }
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
                draggedInLastPress = false;
                if (!itemsUnderMouse.isEmpty()) {
                    JGraphicsItem item = getFirstMovableOrSelectableItem(itemsUnderMouse);
                    if (item != null && e.getClickCount() == 2) {
                        item.fireEvent(ItemChangedType.DoubleClick, point);
                    }

                    //if this item only movable but not selectable
                    if (item != null && itemCanBeDragged(item) && !item.hasFlag(JGraphicsItem.ItemIsSelectable)) {
                        draggedObjects.add(createDragForGraphicsItem(item, (int) sceneMouseX, (int) sceneMouseY));
                        draggedInLastPress = true;
                        return;
                    } else {
                        hideManipulators();
                    }
                }
                if (pointInSelectedObject(viewMouseX, viewMouseY)) {
                    for (JGraphicsItem item : selection.getSelectedItems()) {
                        if (itemCanBeDragged(item)) {
                            draggedObjects.add(createDragForGraphicsItem(item, (int) sceneMouseX, (int) sceneMouseY));
                        }
                    }
                    return;
                } else {
                    selection.clearSelectedItems();
                    selection.getRubberBandRectangle().setLocation((int) viewMouseX, (int) viewMouseY);
                    selection.getRubberBandRectangle().setSize(1, 1);
                    selection.updateNormalizedRubberBandRectangle();
                }
                scene.fireRedraw();
            }
        }
        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_MOVE) {
            if (dragMode == DragMode.RubberBandDrag && selection.isRubberBandDragging()) {
                selection.clearSelectedItems();
                Rectangle r = selection.getRubberBandRectangle();
                r.setSize((int) (viewMouseX - r.x), (int) (viewMouseY - r.y));
                selection.updateNormalizedRubberBandRectangle();
                scene.fireRedraw();
            } else if (!draggedObjects.isEmpty()) {
                draggedInLastPress = true;
                for (DragObject<JGraphicsItem> dragObject : draggedObjects) {
                    float newX = dragObject.calculateNewX(sceneMouseX);
                    float newY = dragObject.calculateNewY(sceneMouseY);
                    JGraphicsItem item = dragObject.getUserObject();
                    item.setPosition(newX, newY, true);
                    item.dirtyTransform();
                    item.fireEvent(ItemChangedType.Position, new Point2D.Float(newX, newY));
                }
                scene.fireRedraw();
            }
        }

        //SEARCH
        if (mouseEventType != JGraphicsScene.MOUSE_EVENT_MOVE || (selection.isRubberBandDragging())) {
            scene.mouseEvent(me, selection);
        }

        //AFTER SEARCH
        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_PRESS && e.getButton() == MouseEvent.BUTTON1) {
            if (selection.getSelectedItems().isEmpty()) {
                selection.setRubberBandDragging(true);
            } else {
                //after press, we should not select as many items, as we could that lies directly under the cursor
                //we should select only top
                ensureOnlyOneItemSelected();
                JGraphicsItem item = selection.getSelectedItem();
                if (itemCanBeDragged(item)) {
                    draggedObjects.add(createDragForGraphicsItem(item, (int) sceneMouseX, (int) sceneMouseY));
                }
            }
        }

        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_RELEASE) {
            List<JGraphicsItem> itemsUnderMouse = scene.getItemsAtPoint_view(viewMouseX, viewMouseY);
            for (JGraphicsItem item : itemsUnderMouse) {
                item.fireEvent(ItemChangedType.MouseRelease, point);
            }

            if (!selection.isRubberBandDragging() && !draggedInLastPress) {
                if (!itemsUnderMouse.isEmpty()) {
                    JGraphicsItem itemUnderMouse = getFirstMovableOrSelectableItem(itemsUnderMouse);
                    selection.clearSelectedItems();
                    selection.addSelectedItem(itemUnderMouse);
                    selection.cancelRubberBandRectangle();
                    scene.fireRedraw();
                }
            }
            if (selection.isRubberBandDragging()) {
                selection.cancelRubberBandRectangle();
                scene.fireRedraw();
            }
            hideManipulators();
            if (selection.getSelectedItems().size() == 1) {
                showManipulators(selection.getSelectedItem());
            }
            draggedObjects.clear();
        }
    }

    private boolean itemCanBeDragged(JGraphicsItem item) {
        return globalAllowDragging && item.hasFlag(JGraphicsItem.ItemIsMovable);
    }

    private JGraphicsItem getFirstMovableOrSelectableItem(List<JGraphicsItem> items) {
        for (JGraphicsItem item : items) {
            if ((item.hasFlag(JGraphicsItem.ItemIsMovable) || item.hasFlag(JGraphicsItem.ItemIsSelectable))) {
                return item;
            }
        }
        return null;
    }

    public void hideManipulators() {
        if (!manipulators.isEmpty()) {
            for (JGraphicsItem manipulator : manipulators) {
                manipulator.getParent().removeItem(manipulator);
            }
            manipulators.clear();
        }
    }

    private void showManipulators(JGraphicsItem item) {
        if (item == null) return;
        if (!item.hasFlag(JGraphicsItem.ItemShowManipulationHandlers)) {
            return;
        }
        List<JGraphicsItem> _manipulators = item.createManipulators();
        manipulators.addAll(_manipulators);
        scene.fireRedraw();
    }

    /**
     * Java8 and Java>8(like java 17) on Mac has different default scaling of ui, we make it the same
     *
     * @param e
     */
    public void transformMouseEvent(MouseEvent e) {
        Point2D newPoint = new Point2D.Float(e.getX() * initialZoom, e.getY() * initialZoom);
        e.translatePoint((int) (newPoint.getX() - e.getX()), (int) (newPoint.getY() - e.getY()));
    }

    private boolean pointInSelectedObject(float x, float y) {
        for (JGraphicsItem item : selection.getSelectedItems()) {
            if (item.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private void ensureOnlyOneItemSelected() {
        if (selection.getSelectedItems().size() > 1) {
            JGraphicsItem item = selection.getSelectedItem();
            selection.clearSelectedItems();
            ;
            selection.addSelectedItem(item);
        }
    }

    public List<JGraphicsItem> getSelectedItems() {
        return new ArrayList<>(selection.getSelectedItems());
    }

    public JGraphicsItem getSelectedItem() {
        return selection.getSelectedItem();
    }

    public void clearSelection() {
        selection.clearSelectedItems();
        repaint();
    }

    private DragObject<JGraphicsItem> createDragForGraphicsItem(JGraphicsItem item, int mouseX, int mouseY) {
        DragObject<JGraphicsItem> dragObject = new DragObject<>(item);
        Point2D pos = item.getGlobalPosition();
        dragObject.initPosition((float) pos.getX(), (float) pos.getY(), (float) mouseX, (float) mouseY);
        return dragObject;
    }

    private boolean processPan(int mouseEventType, MouseEvent e) {
        boolean startPan = mouseEventType == JGraphicsScene.MOUSE_EVENT_PRESS && ((middleMousePanEnabled && e.getButton() == MouseEvent.BUTTON2));
        boolean releasePan = mouseEventType == JGraphicsScene.MOUSE_EVENT_RELEASE && panDrag != null;
        boolean movePan = mouseEventType == JGraphicsScene.MOUSE_EVENT_MOVE && panDrag != null;
        if (startPan) {
            panDrag = new DragObject<>();
            panDrag.initPosition(scrollX, scrollY, e.getX(), e.getY());
            oldCursor = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return true;
        }
        if (releasePan) {
            panDrag = null;
            setCursor(oldCursor);
            return true;
        }
        if (movePan) {
            scrollX = (int) panDrag.calculateNewX(e.getX());
            scrollY = (int) panDrag.calculateNewY(e.getY());
            updateViewTransform();
            repaint();
            return true;
        }
        return false;
    }

    public Transform getViewTransform() {
        return viewTransform;
    }

    public Point2D mapViewToScene(Point2D point) {
        return getViewTransform().inverseTransformPointMut(point);
    }

    public Point2D mapSceneToView(Point2D point) {
        return getViewTransform().transformMut(point);
    }
}