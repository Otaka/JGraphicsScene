package com.jgraphicsscene.connection;

import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.node.JGraphicsSplineItem;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class ConnectionsManagerExportImport {
    private final ConnectionManager connectionManager;
    private final AtomicInteger idCounter = new AtomicInteger();
    private ExportCustomizer exportCustomizer;
    private ImportCustomizer importCustomizer;


    ConnectionsManagerExportImport(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionsManagerExportImport setExportCustomizer(ExportCustomizer exportCustomizer) {
        this.exportCustomizer = exportCustomizer;
        return this;
    }

    public ConnectionsManagerExportImport setImportCustomizer(ImportCustomizer importCustomizer) {
        this.importCustomizer = importCustomizer;
        return this;
    }

    public ExportData exportData() {
        BiDirMap<String, NodeConfig> id2Node = new BiDirMap<>();
        BiDirMap<String, ConnectionConfig> id2Connection = new BiDirMap<>();

        for (NodeConfig node : connectionManager.getNodes()) {
            id2Node.put(getNextId(), node);
        }

        if (idCounter.get() < 100_000) { //make Ids of connections start from 100_000
            idCounter.set(100_000);
        }

        for (ConnectionConfig connection : connectionManager.getConnections()) {
            id2Connection.put(getNextId(), connection);
        }

        List<ExportData.Node> nodes = new ArrayList<>();
        List<ExportData.Connection> connections = new ArrayList<>();
        List<ExportData.LinkData> linksData = new ArrayList<>();

        ExportData exportData = new ExportData(nodes, connections, linksData);
        for (NodeConfig node : connectionManager.getNodes()) {
            ExportData.Node nodeData = new ExportData.Node(id2Node.getReverse(node), node.getItem().getX(),  node.getItem().getY(), node.getItem().getRotation(), node.getItem().getScale());
            if (exportCustomizer != null) {
                nodeData.extra = exportCustomizer.createExtraNodeData(node);
                exportCustomizer.fillNodeData(node, nodeData.extra);
            }

            exportData.nodes.add(nodeData);
        }

        for (ConnectionConfig connectionConfig : connectionManager.getConnections()) {
            ExportData.Connection connection = new ExportData.Connection();
            exportData.connections.add(connection);
            connection.id = id2Connection.getReverse(connectionConfig);
            connection.pointsCount = connectionConfig.getItem().getPoints().size();
            for (Point2D point : connectionConfig.getItem().getPoints()) {
                connection.pointsCoords.add((float) point.getX());
                connection.pointsCoords.add((float) point.getY());
            }

            if (exportCustomizer != null) {
                Object extraConnectionData = exportCustomizer.createExtraConnectionData(connectionConfig);
                if (extraConnectionData != null) {
                    exportCustomizer.fillConnectionData(connectionConfig, extraConnectionData);
                    connection.extra = extraConnectionData;
                }
            }
        }

        for (ConnectionConfig connection : connectionManager.getConnections()) {
            ExportData.LinkData linkData = new ExportData.LinkData();
            exportData.linkData.add(linkData);
            linkData.connectionId = id2Connection.getReverse(connection);
            if (connection.getStartNode() != null) {
                linkData.startNodeId = id2Node.getReverse(connection.getStartNode());
                linkData.startNodeTargetId = connection.getStartTarget().getId();
            } else {
                linkData.startNodeId = null;
                linkData.startNodeTargetId = -1;
            }
            if (connection.getEndNode() != null) {
                linkData.endNodeId = id2Node.getReverse(connection.getEndNode());
                linkData.endNodeTargetId = connection.getEndTarget().getId();
            } else {
                linkData.endNodeId = null;
                linkData.endNodeTargetId = -1;
            }
        }

        return exportData;
    }

    public void importData(ExportData data) {
        if (importCustomizer == null) throw new IllegalArgumentException("ImportCustomizer is not set");
        BiDirMap<String, NodeConfig> id2node = new BiDirMap<>();
        for (ExportData.Node node : data.nodes) {
            JGraphicsItem nodeItem = importCustomizer.createNode(node.extra);
            nodeItem.setPosition(node.x, node.y, false);
            nodeItem.setRotation(node.rotation);
            nodeItem.setScale(node.scale);

            id2node.put(node.id, connectionManager.getNode(nodeItem));
            connectionManager.scene.addItem(nodeItem);
        }

        BiDirMap<String, ConnectionConfig> id2connection = new BiDirMap<>();
        for (ExportData.Connection connection : data.connections) {
            JGraphicsSplineItem connectionItem = importCustomizer.createConnection(connection.extra);
            connectionManager.scene.addItem(connectionItem);
            Point2D[] points = new Point2D[connection.pointsCount];
            for (int i = 0; i < connection.pointsCount; i++) {
                points[i] = new Point2D.Float(connection.pointsCoords.get(i * 2), connection.pointsCoords.get(i * 2 + 1));
            }

            connectionItem.setPoints(points);

            ConnectionConfig connectionConfig = connectionManager.registerConnectionItem(connectionItem);
            id2connection.put(connection.id, connectionConfig);
            connectionManager.scene.addItem(connectionItem);
        }

        for (ExportData.LinkData linkData : data.linkData) {
            ConnectionConfig connection = id2connection.get(linkData.connectionId);
            if (linkData.startNodeId != null) {
                NodeConfig node = id2node.get(linkData.startNodeId);
                connection.setStartNode(node);
                if (linkData.startNodeTargetId != -1) {
                    TargetConfig startTargetConfig = connectionManager.getNodeConnectionTarget(node.getItem(), linkData.startNodeTargetId);
                    connection.setStartTarget(startTargetConfig);
                    startTargetConfig.getOutConnections().add(connection);
                }
            }
            if (linkData.endNodeId != null) {
                NodeConfig node = id2node.get(linkData.endNodeId);
                connection.setEndNode(node);
                if (linkData.endNodeTargetId != -1) {
                    TargetConfig endTargetConfig = connectionManager.getNodeConnectionTarget(node.getItem(), linkData.endNodeTargetId);
                    connection.setEndTarget(endTargetConfig);
                    endTargetConfig.getInConnections().add(connection);
                }
            }
        }
    }

    private String getNextId() {
        return String.valueOf(idCounter.incrementAndGet());
    }

    public interface ExportCustomizer {
        void fillNodeData(NodeConfig node, Object extraNodeData);

        void fillConnectionData(ConnectionConfig connection, Object extraConnectionData);

        Object createExtraNodeData(NodeConfig node);

        Object createExtraConnectionData(ConnectionConfig connection);
    }

    public static abstract class ImportCustomizer {
        /**
         * You can create node/item of your type in this method. Position/rotation/scaling, wiring to other nodes and other properties will be set automatically.<br>
         * This method should add the created item to the scene and add it to the connectionManager via the <b>initConnectionTarget</b> method.
         */
        public abstract JGraphicsItem createNode(Object extraNodeData);

        /**
         * This method should just create connection item and that is all.
         */
        public abstract JGraphicsSplineItem createConnection(Object extraConnectionData);
    }

    public static class BiDirMap<K, V> {
        public Map<K, V> map = new HashMap<>();
        public Map<V, K> reverseMap = new HashMap<>();

        public void put(K k, V v) {
            map.put(k, v);
            reverseMap.put(v, k);
        }

        public V get(K k) {
            return map.get(k);
        }

        public K getReverse(V v) {
            return reverseMap.get(v);
        }

        public void delete(K k) {
            V v = map.get(k);
            map.remove(k);
            reverseMap.remove(v);
        }

        public void deleteReverse(V v) {
            K k = reverseMap.get(v);
            map.remove(k);
            reverseMap.remove(v);
        }

        public void clear() {
            map.clear();
            reverseMap.clear();
        }
    }
}
