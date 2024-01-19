package com.jgraphicsscene.connection;

import java.util.ArrayList;
import java.util.List;

public class ExportData {
    List<Node> nodes;
    List<Connection> connections;
    List<LinkData> linkData;

    public ExportData(List<Node> nodes, List<Connection> connections, List<LinkData> linkData) {
        this.nodes = nodes;
        this.connections = connections;
        this.linkData = linkData;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public List<LinkData> getLinkData() {
        return linkData;
    }

    public static class Node {
        String id;
        float x;
        float y;
        float rotation;
        float scale;
        Object extra;

        public Node(String id, float x, float y, float rotation, float scale) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.scale = scale;
        }

        public Object getExtra() {
            return extra;
        }

        public Node setExtra(Object extra) {
            this.extra = extra;
            return this;
        }

        public String getId() {
            return id;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getRotation() {
            return rotation;
        }

        public float getScale() {
            return scale;
        }
    }

    public static class Connection {
        String id;
        int pointsCount;
        List<Float> pointsCoords = new ArrayList<>();
        Object extra;

        public Object getExtra() {
            return extra;
        }

        public String getId() {
            return id;
        }

        public int getPointsCount() {
            return pointsCount;
        }

        public List<Float> getPointsCoords() {
            return pointsCoords;
        }
    }

    public static class LinkData {
        String connectionId;

        String startNodeId;
        int startNodeTargetId;

        String endNodeId;
        int endNodeTargetId;

        public String getConnectionId() {
            return connectionId;
        }

        public String getStartNodeId() {
            return startNodeId;
        }

        public int getStartNodeTargetId() {
            return startNodeTargetId;
        }

        public String getEndNodeId() {
            return endNodeId;
        }

        public int getEndNodeTargetId() {
            return endNodeTargetId;
        }
    }
}
