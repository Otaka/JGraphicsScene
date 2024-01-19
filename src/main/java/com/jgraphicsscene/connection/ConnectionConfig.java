package com.jgraphicsscene.connection;

import com.jgraphicsscene.node.JGraphicsSplineItem;

public class ConnectionConfig {
    private final JGraphicsSplineItem item;
    private NodeConfig startNode;
    private TargetConfig startTarget;
    private NodeConfig endNode;
    private TargetConfig endTarget;

    public ConnectionConfig(JGraphicsSplineItem connectionItem) {
        this.item = connectionItem;
    }

    public JGraphicsSplineItem getItem() {
        return item;
    }

    public NodeConfig getStartNode() {
        return startNode;
    }

    public ConnectionConfig setStartNode(NodeConfig startNode) {
        this.startNode = startNode;
        return this;
    }

    public TargetConfig getStartTarget() {
        return startTarget;
    }

    public ConnectionConfig setStartTarget(TargetConfig startTarget) {
        this.startTarget = startTarget;
        return this;
    }

    public NodeConfig getEndNode() {
        return endNode;
    }

    public ConnectionConfig setEndNode(NodeConfig endNode) {
        this.endNode = endNode;
        return this;
    }

    public TargetConfig getEndTarget() {
        return endTarget;
    }

    public ConnectionConfig setEndTarget(TargetConfig endTarget) {
        this.endTarget = endTarget;
        return this;
    }

    public boolean isStartConnected() {
        return startNode != null && startTarget != null;
    }

    public boolean isEndConnected() {
        return endNode != null && endTarget != null;
    }
}