package com.jgraphicsscene;

import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.events.SceneRedrawEvent;
import com.jgraphicsscene.node.JGraphicsItem;

import javax.swing.*;
import java.awt.*;
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
    private final AffineTransform sceneTransform = new AffineTransform();
    private final List<DragObject<JGraphicsItem>> draggedObjects = new ArrayList<>();
    private final Selection selection = new Selection();
    private JGraphicsScene scene;
    private SceneRedrawEvent sceneRedrawEvent;
    private DragObject<?> panDrag = null;
    private boolean antialias = true;
    private int scrollX = 0;
    private int scrollY = 0;
    private float zoom = 1;
    private boolean middleMousePanEnabled = true;
    private boolean wheelZoomEnabled = true;
    private boolean wheelZoomToPointer = true;
    private DragMode dragMode = DragMode.NoDrag;
    private boolean interactive = true;

    public JGraphicsView(JGraphicsScene scene) {
        super();
        this.scene = scene;
    }

    public JGraphicsView() {
        setOpaque(true);
        setBackground(Color.WHITE);
        sceneRedrawEvent = this::sceneRedrawEvent;
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
                Point2D cursorLocationSceneSpace = JGraphicsScene.getTempPoint(e.getX(), e.getY());
                Utils.inverseTransformPoint(cursorLocationSceneSpace, sceneTransform);
                zoom = (float) (zoom - (0.1 * e.getPreciseWheelRotation()));
                zoom = Math.max(zoom, 0.1f);
                updateSceneTransform();
                if (wheelZoomToPointer) {
                    cursorLocationSceneSpace = mapSceneToView(cursorLocationSceneSpace);
                    scrollX += e.getX() - cursorLocationSceneSpace.getX();
                    scrollY += e.getY() - cursorLocationSceneSpace.getY();
                    updateSceneTransform();
                }
                JGraphicsScene.disposeTempPoint(cursorLocationSceneSpace);
                repaint();
            }
        });
    }

    public boolean isAntialias() {
        return antialias;
    }

    public JGraphicsView setAntialias(boolean antialias) {
        this.antialias = antialias;
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

    public boolean isWheelZoomToPointer() {
        return wheelZoomToPointer;
    }

    public JGraphicsView setWheelZoomToPointer(boolean wheelZoomToPointer) {
        this.wheelZoomToPointer = wheelZoomToPointer;
        return this;
    }

    public JGraphicsView scroll(int x, int y) {
        scrollX = x;
        scrollY = y;
        updateSceneTransform();
        return this;
    }

    public JGraphicsView zoom(float zoom) {
        this.zoom = zoom;
        updateSceneTransform();
        return this;
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

    private void updateSceneTransform() {
        sceneTransform.setToIdentity();
        sceneTransform.translate(scrollX, scrollY);
        sceneTransform.scale(zoom, zoom);
    }

    public JGraphicsView setScene(JGraphicsScene scene) {
        if (this.scene != null) {
            this.scene.removeSceneRedrawEvent(sceneRedrawEvent);
        }
        this.scene = scene;
        if (scene != null) {
            scene.addSceneRedrawEvent(sceneRedrawEvent);
        }
        return this;
    }

    private void sceneRedrawEvent(JGraphicsScene scene) {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics _g) {
        if (scene == null) {
            return;
        }

        Graphics2D g = (Graphics2D) _g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setTransform(sceneTransform);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        scene.paint(g, selection);
        g.setTransform(sceneTransform);
        g.setColor(RUBBER_BAND_COLOR);
        if (selection.isRubberBandDragging() && !(selection.getRubberBandRectangle().width == 1 && selection.getRubberBandRectangle().height == 1)) {
            Rectangle r = selection.getNormalizedRubberBandRectangle();
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setColor(Color.black);
            g.drawRect(r.x, r.y, r.width, r.height);
        }
        g.setColor(Color.WHITE);
    }

    private void mouseEvent(int mouseEventType, MouseEvent e) {
        if (scene == null) {
            return;
        }

        if (processPan(mouseEventType, e)) {
            return;
        }
        if (!interactive) {
            return;
        }
        Point2D point = mapViewToScene(JGraphicsScene.getTempPoint(e.getX(), e.getY()));

        float transformedMouseX = (float) point.getX();
        float transformedMouseY = (float) point.getY();
        JGraphicsScene.disposeTempPoint(point);

        com.jgraphicsscene.events.MouseEvent me = new com.jgraphicsscene.events.MouseEvent(mouseEventType, transformedMouseX, transformedMouseY, e.getButton(), e.getModifiersEx());

        //BEFORE SEARCH
        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_PRESS && e.getButton() == MouseEvent.BUTTON1) {
            if (pointInSelectedObject(transformedMouseX, transformedMouseY)) {
                for (JGraphicsItem item : selection.getSelectedItems()) {
                    if ((item.getFlag() & JGraphicsItem.ItemIsMovable) != 0)
                        draggedObjects.add(createDragForGraphicsItem(item, (int) transformedMouseX, (int) transformedMouseY));
                }
                return;
            } else {
                selection.getSelectedItems().clear();
                selection.getRubberBandRectangle().setLocation((int) transformedMouseX, (int) transformedMouseY);
                selection.getRubberBandRectangle().setSize(1, 1);
                selection.updateNormalizedRubberBandRectangle();
            }
            scene.fireRedraw();
        }
        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_MOVE) {
            if (dragMode == DragMode.RubberBandDrag && selection.isRubberBandDragging()) {
                selection.getSelectedItems().clear();
                Rectangle r = selection.getRubberBandRectangle();
                r.setSize((int) (transformedMouseX - r.x), (int) (transformedMouseY - r.y));
                selection.updateNormalizedRubberBandRectangle();
                scene.fireRedraw();
            } else if (!draggedObjects.isEmpty()) {
                for (DragObject<JGraphicsItem> dragObject : draggedObjects) {
                    float newX = dragObject.calculateNewX(transformedMouseX);
                    float newY = dragObject.calculateNewY(transformedMouseY);
                    JGraphicsItem item = dragObject.getUserObject();
                    item.setPosition(newX, newY, true);
                    item.dirtyTransform();
                    if (item.hasFlag(JGraphicsItem.ItemSendsMoved)) {
                        item.fireEvent(ItemChangedType.Position);
                    }
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
                if ((item.getFlag() & JGraphicsItem.ItemIsMovable) != 0)
                    draggedObjects.add(createDragForGraphicsItem(item, (int) transformedMouseX, (int) transformedMouseY));
            }
        }

        if (mouseEventType == JGraphicsScene.MOUSE_EVENT_RELEASE) {
            if (selection.isRubberBandDragging()) {
                selection.setRubberBandDragging(false);
                selection.getRubberBandRectangle().setSize(0, 0);
                selection.updateNormalizedRubberBandRectangle();
                scene.fireRedraw();
            }
            draggedObjects.clear();
        }
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
            selection.getSelectedItems().clear();
            selection.getSelectedItems().add(item);
        }
    }

    public List<JGraphicsItem> selectedItems() {
        return new ArrayList<>(selection.getSelectedItems());
    }

    public JGraphicsItem selectedItem() {
        return selection.getSelectedItem();
    }

    private DragObject<JGraphicsItem> createDragForGraphicsItem(JGraphicsItem item, int mouseX, int mouseY) {
        DragObject<JGraphicsItem> dragObject = new DragObject<>(item);
        Point2D pos = item.getGlobalPosition();
        dragObject.initPosition((float) pos.getX(), (float) pos.getY(), (float) mouseX, (float) mouseY);
        return dragObject;
    }

    private boolean processPan(int mouseEventType, MouseEvent e) {
        boolean startPan = mouseEventType == JGraphicsScene.MOUSE_EVENT_PRESS && (
                (middleMousePanEnabled && e.getButton() == MouseEvent.BUTTON2)
        );
        boolean releasePan = mouseEventType == JGraphicsScene.MOUSE_EVENT_RELEASE && panDrag != null;
        boolean movePan = mouseEventType == JGraphicsScene.MOUSE_EVENT_MOVE && panDrag != null;
        if (startPan) {
            panDrag = new DragObject<>();
            panDrag.initPosition(scrollX, scrollY, e.getX(), e.getY());
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return true;
        }
        if (releasePan) {
            panDrag = null;
            setCursor(Cursor.getDefaultCursor());
            return true;
        }
        if (movePan) {
            scrollX = (int) panDrag.calculateNewX(e.getX());
            scrollY = (int) panDrag.calculateNewY(e.getY());
            updateSceneTransform();
            repaint();

            Point2D obj1GlobalPos = scene.getItems().get(0).getGlobalPosition();
            return true;
        }
        return false;
    }

    public AffineTransform getSceneTransform() {
        return sceneTransform;
    }

    public Point2D mapViewToScene(Point2D point) {
        return Utils.inverseTransformPoint(point, getSceneTransform());
    }

    public Point2D mapSceneToView(Point2D point) {
        return getSceneTransform().transform(point, point);
    }
}