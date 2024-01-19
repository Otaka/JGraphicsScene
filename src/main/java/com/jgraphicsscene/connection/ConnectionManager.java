package com.jgraphicsscene.connection;

import com.jgraphicsscene.JGraphicsScene;
import com.jgraphicsscene.JGraphicsView;
import com.jgraphicsscene.events.ItemChangedType;
import com.jgraphicsscene.events.MouseEvent;
import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.node.JGraphicsSplineItem;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Simple class that icapsulates connection logic between nodes.
 * Any node can have connection targets. Node - it is some graph node and Target - place on node from where and to where you can drag connections.
 * One node can have several targets
 */
public class ConnectionManager {
    final JGraphicsView view;
    final JGraphicsScene scene;
    private final Map<JGraphicsItem, NodeConfig> node2config = new HashMap<>();
    private final Map<JGraphicsItem, TargetConfig> target2config = new HashMap<>();
    private final Map<JGraphicsSplineItem, ConnectionConfig> connection2config = new HashMap<>();
    private final ConnectionManagerConfiguration connectionManagerConfiguration;
    private JGraphicsSplineItem currentDraggingConnection;

    public ConnectionManager(JGraphicsView view, JGraphicsScene scene, ConnectionManagerConfiguration connectionManagerConfiguration) {
        this.view = view;
        this.scene = scene;
        this.connectionManagerConfiguration = connectionManagerConfiguration;
        view.addViewMouseHandler((item, event) -> mouseEventHandler(event));
    }

    public JGraphicsSplineItem createConnection(JGraphicsItem source, int sourceTargetId, JGraphicsItem destination, int destinationTargetId, Point2D... intermediatePoints) {
        NodeConfig sourceNodeConfig = getNode(source);
        NodeConfig destinationNodeConfig = getNode(destination);
        TargetConfig sourceTargetConfig = sourceNodeConfig.getTargetById(sourceTargetId);
        TargetConfig destinationTargetConfig = destinationNodeConfig.getTargetById(destinationTargetId);
        Objects.requireNonNull(sourceTargetConfig, "Source target with id '" + sourceTargetId + "' is not found");
        Objects.requireNonNull(destinationTargetConfig, "Destination target with id '" + destinationTargetId + "' is not found");
        JGraphicsSplineItem connection = connectionManagerConfiguration.createConnection(sourceTargetConfig);
        scene.addItem(connection);
        ConnectionConfig connectionConfig = registerConnectionItem(connection);

        connectionConfig.setStartNode(sourceNodeConfig);
        connectionConfig.setStartTarget(sourceTargetConfig);
        sourceTargetConfig.getOutConnections().add(connectionConfig);

        connectionConfig.setEndNode(destinationNodeConfig);
        connectionConfig.setEndTarget(destinationTargetConfig);
        destinationTargetConfig.getInConnections().add(connectionConfig);
        List<Point2D> points = new ArrayList<>();
        points.add(sourceTargetConfig.getItem().getGlobalPosition());
        Collections.addAll(points, intermediatePoints);
        points.add(destinationTargetConfig.getItem().getGlobalPosition());
        connection.setPoints(points);
        connection.refreshLine();
        return connection;
    }

    public ConnectionManagerConfiguration getConnectionFactory() {
        return connectionManagerConfiguration;
    }


    private void mouseEventHandler(MouseEvent event) {
        if (currentDraggingConnection != null && event.getType() == JGraphicsScene.MOUSE_EVENT_MOVE) {
            Point2D sceneCoords = view.mapViewToScene(new Point2D.Float(event.getX(), event.getY()));
            currentDraggingConnection.getEndPoint().setLocation(sceneCoords);
            currentDraggingConnection.refreshLine();
            scene.fireRedraw();
            TargetConfig target = getConnectionTargetCandidateAtCoords((float) sceneCoords.getX(), (float) sceneCoords.getY(), currentDraggingConnection);
            boolean allow = isAllowToConnect(connection2config.get(currentDraggingConnection), true, target);
            Cursor cursor = allow ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            view.setCursor(cursor);
        }
        if (currentDraggingConnection != null && event.getType() == JGraphicsScene.MOUSE_EVENT_RELEASE) {
            Point2D sceneCoords = view.mapViewToScene(new Point2D.Float(event.getX(), event.getY()));
            ConnectionConfig connectionConfig = connection2config.get(currentDraggingConnection);
            TargetConfig target = getConnectionTargetCandidateAtCoords((float) sceneCoords.getX(), (float) sceneCoords.getY(), currentDraggingConnection);
            if (isAllowToConnect(connectionConfig, true, target)) {
                connectionConfig.setEndTarget(target);
                connectionConfig.setEndNode(getNode(target.getOwnerNode()));
                target.getInConnections().add(connectionConfig);

                currentDraggingConnection.getEndPoint().setLocation(target.getItem().getGlobalPosition());
                currentDraggingConnection.refreshLine();
            }
            view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            scene.fireRedraw();
            currentDraggingConnection = null;
        }
    }

    private TargetConfig getConnectionTargetCandidateAtCoords(float sceneX, float sceneY, JGraphicsSplineItem connection) {
        List<JGraphicsItem> itemsUnderMouse = scene.getItemsAtPoint_scene(sceneX, sceneY);
        if (!itemsUnderMouse.isEmpty()) {
            for (int i = 0; i < itemsUnderMouse.size(); i++) {
                JGraphicsItem targetCandidate = itemsUnderMouse.get(i);
                if (target2config.containsKey(targetCandidate)) {
                    ConnectionConfig connectionConfig = connection2config.get(connection);
                    TargetConfig targetConfig = target2config.get(targetCandidate);
                    if (targetConfig == connectionConfig.getStartTarget()) {
                        return null;
                    }

                    return targetConfig;
                }
            }
        }
        return null;
    }

    public TargetConfig initConnectionTarget(int id, JGraphicsItem target, JGraphicsItem ownerNode) {
        if (target2config.containsKey(target)) {
            throw new IllegalArgumentException("Target already added as a connectionTarget");
        }
        if (!node2config.containsKey(ownerNode)) {
            ownerNode.addEvent((itemChangedType, item, arg) -> {
                if (itemChangedType == ItemChangedType.Position) {
                    nodeMoved(ownerNode);
                }
                return false;
            });
            node2config.put(ownerNode, new NodeConfig(ownerNode));
        }
        NodeConfig nodeConfig = node2config.get(ownerNode);
        TargetConfig targetConfig = new TargetConfig(id, target, ownerNode);
        nodeConfig.getTargets().add(targetConfig);
        target2config.put(target, targetConfig);
        return targetConfig;
    }

    public void enableCreatingConnectionByDraggingConnectionTarget(JGraphicsItem target, int mouseButton) {
        target.addEvent((itemChangedType, item, arg) -> {
            if (itemChangedType == ItemChangedType.MousePress && mouseButton == (Integer) arg) {
                startMakingConnection(target);
            }
            return false;
        });
    }

    public void startMakingConnection(JGraphicsItem target) {
        TargetConfig targetConfig = target2config.get(target);
        if (targetConfig == null) {
            throw new IllegalArgumentException("Please call initConnectionTarget first for this target");
        }

        JGraphicsSplineItem connection = getConnectionFactory().createConnection(targetConfig);
        if (connection == null)
            return;

        ConnectionConfig connectionConfig = registerConnectionItem(connection);
        connectionConfig.setStartNode(getNode(targetConfig.getOwnerNode()));
        connectionConfig.setStartTarget(targetConfig);

        currentDraggingConnection = connection;
        connection.getStartPoint().setLocation(target.getGlobalPosition());
        connection.getEndPoint().setLocation(target.getGlobalPosition());
        connection.refreshLine();
        scene.addItem(connection);
        targetConfig.getOutConnections().add(connectionConfig);
        scene.fireRedraw();
    }

    private boolean isAllowToConnect(ConnectionConfig connectionConfig, boolean dragByEndManipulator, TargetConfig target) {
        if (target == null) return false;
        return connectionManagerConfiguration.checkAllowToConnect(connectionConfig, dragByEndManipulator, target);
    }

    private void nodeMoved(JGraphicsItem node) {
        NodeConfig nodeConfig = node2config.get(node);
        for (TargetConfig targetConfig : nodeConfig.getTargets()) {
            Point2D targetPosition = targetConfig.getItem().getGlobalPosition();
            for (ConnectionConfig connectionConfig : targetConfig.getInConnections()) {
                connectionConfig.getItem().getEndPoint().setLocation(targetPosition);
                connectionConfig.getItem().refreshLine();
            }
            for (ConnectionConfig connectionConfig : targetConfig.getOutConnections()) {
                connectionConfig.getItem().getStartPoint().setLocation(targetPosition);
                connectionConfig.getItem().refreshLine();
            }
        }
    }

    public ConnectionConfig registerConnectionItem(JGraphicsSplineItem connectionItem) {
        ConnectionConfig connectionConfig = new ConnectionConfig(connectionItem);
        connection2config.put(connectionItem, connectionConfig);

        connectionItem.addEvent((itemChangedType, item, arg) -> {
            if (itemChangedType == ItemChangedType.ManipulatorPressed) {
                boolean dragByStart = arg.equals(0);
                if (dragByStart) {
                    //start
                    if (connectionConfig.getStartTarget() != null) {
                        TargetConfig startTargetConfig = connectionConfig.getStartTarget();
                        startTargetConfig.getOutConnections().remove(connectionConfig);
                        connectionConfig.setStartTarget(null);
                        connectionConfig.setStartNode(null);
                    }
                } else {
                    //end
                    if (connectionConfig.getEndTarget() != null) {
                        TargetConfig endTargetConfig = connectionConfig.getEndTarget();
                        endTargetConfig.getInConnections().remove(connectionConfig);
                        connectionConfig.setEndTarget(null);
                        connectionConfig.setEndNode(null);
                    }
                }
            }
            if (itemChangedType == ItemChangedType.ManipulatorReleased) {
                boolean dragByStart = arg.equals(0);
                if (dragByStart) {
                    //start
                    Point2D point = connectionItem.getStartPoint();
                    point = connectionItem.mapItemToSceneImmutable(point);
                    TargetConfig newTarget = getConnectionTargetCandidateAtCoords((float) point.getX(), (float) point.getY(), connectionItem);
                    if (isAllowToConnect(connectionConfig, false, newTarget)) {
                        bindConnectionStart(connectionConfig, newTarget);
                    }
                } else {
                    //end
                    Point2D point = connectionItem.getEndPoint();
                    point = connectionItem.mapItemToSceneImmutable(point);
                    TargetConfig newTarget = getConnectionTargetCandidateAtCoords((float) point.getX(), (float) point.getY(), connectionItem);
                    if (isAllowToConnect(connectionConfig, true, newTarget)) {
                        bindConnectionEnd(connectionConfig, newTarget);
                    }
                }
                view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            if (itemChangedType == ItemChangedType.ManipulatorMoved) {
                boolean dragByStart = arg.equals(0);
                Point2D point = dragByStart ? connectionItem.getStartPoint() : connectionItem.getEndPoint();
                point = connectionItem.mapItemToSceneImmutable(point);
                TargetConfig newTarget = getConnectionTargetCandidateAtCoords((float) point.getX(), (float) point.getY(), connectionItem);
                boolean allow = isAllowToConnect(connectionConfig, !dragByStart, newTarget);
                Cursor cursor = allow ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                view.setCursor(cursor);
            }
            return false;
        });
        return connectionConfig;
    }

    private void bindConnectionStart(ConnectionConfig connectionConfig, TargetConfig target) {
        connectionConfig.setStartNode(getNode(target.getOwnerNode()));
        connectionConfig.setStartTarget(target);
        target.getOutConnections().add(connectionConfig);
        connectionConfig.getItem().getStartPoint().setLocation(target.getItem().getGlobalPosition());
        connectionConfig.getItem().refreshLine();
    }

    private void bindConnectionEnd(ConnectionConfig connectionConfig, TargetConfig target) {
        connectionConfig.setEndNode(getNode(target.getOwnerNode()));
        connectionConfig.setEndTarget(target);
        target.getInConnections().add(connectionConfig);
        connectionConfig.getItem().getEndPoint().setLocation(target.getItem().getGlobalPosition());
        connectionConfig.getItem().refreshLine();
    }


    private void removeConnection(ConnectionConfig connectionConfig) {
        removeConnection(connectionConfig.getItem());
    }

    private void removeConnection(JGraphicsSplineItem item) {
        ConnectionConfig config = connection2config.remove(item);
        if (config.getStartTarget() != null) {
            config.getStartTarget().getOutConnections().remove(config);
        }
        if (config.getEndTarget() != null) {
            config.getEndTarget().getInConnections().remove(config);
        }
        removeItem(item, true);
    }

    public void remove(JGraphicsItem item) {
        if (node2config.containsKey(item)) {
            removeNode(item);
        } else if (item instanceof JGraphicsSplineItem && connection2config.containsKey(item)) {
            removeConnection((JGraphicsSplineItem) item);
        }
    }

    private void removeNode(JGraphicsItem item) {
        NodeConfig nodeConfig = node2config.get(item);
        for (TargetConfig targetConfig : nodeConfig.getTargets()) {
            List<ConnectionConfig> connectionsToRemove = new ArrayList<>();
            connectionsToRemove.addAll(targetConfig.getInConnections());
            connectionsToRemove.addAll(targetConfig.getOutConnections());
            for (ConnectionConfig connection : connectionsToRemove) {
                removeConnection(connection);
            }
        }
        for (TargetConfig targetConfig : nodeConfig.getTargets()) {
            targetConfig.getInConnections().clear();
            targetConfig.getOutConnections().clear();
            target2config.remove(targetConfig.getOwnerNode());
        }
        node2config.remove(item);
        removeItem(item, false);
    }

    private void removeItem(JGraphicsItem item, boolean isConnection) {
        connectionManagerConfiguration.onDelete(item, isConnection);
        item.getParent().removeItem(item);
    }

    public void clear() {
        for (NodeConfig node : new ArrayList<>(node2config.values())) {
            removeNode(node.getItem());
        }
        for (ConnectionConfig connection : new ArrayList<>(connection2config.values())) {
            removeConnection(connection);
        }

        currentDraggingConnection = null;
        connection2config.clear();
        target2config.clear();
        node2config.clear();
    }

    public List<NodeConfig> getNodes() {
        return new ArrayList<>(node2config.values());
    }

    public List<ConnectionConfig> getConnections() {
        return new ArrayList<>(connection2config.values());
    }

    public NodeConfig getNode(JGraphicsItem node) {
        NodeConfig nodeConfig = node2config.get(node);
        if (nodeConfig == null) {
            throw new IllegalArgumentException("JGraphicsItem is not initialized as a node");
        }
        return nodeConfig;
    }

    public List<ConnectionConfig> getInConnections(JGraphicsItem node) {
        NodeConfig nodeConfig = getNode(node);
        List<ConnectionConfig> result = new ArrayList<>();
        for (TargetConfig targetConfig : nodeConfig.getTargets()) {
            result.addAll(targetConfig.getInConnections());
        }
        return result;
    }

    public List<ConnectionConfig> getOutConnections(JGraphicsItem node) {
        NodeConfig nodeConfig = getNode(node);
        List<ConnectionConfig> result = new ArrayList<>();
        for (TargetConfig targetConfig : nodeConfig.getTargets()) {
            result.addAll(targetConfig.getOutConnections());
        }
        return result;
    }

    public List<TargetConfig> getNodeConnectionTargets(JGraphicsItem node) {
        return new ArrayList<>(getNode(node).getTargets());
    }

    public TargetConfig getNodeConnectionTarget(JGraphicsItem node, int id) {
        for (TargetConfig targetConfig : getNode(node).getTargets()) {
            if (targetConfig.getId() == id) {
                return targetConfig;
            }
        }
        return null;
    }

    public ConnectionConfig getConnectionConfig(JGraphicsSplineItem connection) {
        if (!connection2config.containsKey(connection)) {
            throw new IllegalArgumentException("connection is not initialized");
        }
        return connection2config.get(connection);
    }

    public ConnectionsManagerExportImport createSerializer() {
        return new ConnectionsManagerExportImport(this);
    }
}
