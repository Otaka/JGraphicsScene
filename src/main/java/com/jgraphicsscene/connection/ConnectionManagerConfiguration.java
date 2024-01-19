package com.jgraphicsscene.connection;

import com.jgraphicsscene.node.JGraphicsItem;
import com.jgraphicsscene.node.JGraphicsSplineItem;

public abstract class ConnectionManagerConfiguration {
    /**
     * This method will be called by the system when it need to create new conection item.
     * If it returns null - it is mean, that you cannot create connection from this node.
     */
    public abstract JGraphicsSplineItem createConnection(TargetConfig targetConfig);

    /**
     * Method is called by system to check if currently dragged connection can be connected to node target.
     *
     * @param connection             - connection that is currently dragged
     * @param draggingEndManipulator - true if connection is dragged by it's end manipulator. false - if dragged by start manipulator
     * @param nodeTargetConfig       - target node that is currently touching by dragged connection manipulator
     */
    public boolean checkAllowToConnect(ConnectionConfig connection, boolean draggingEndManipulator, TargetConfig nodeTargetConfig) {
        return true;
    }

    /**
     * This method will be called before the node or connection will be deleted.<br>
     */
    public void onDelete(JGraphicsItem node, boolean isConnection) {

    }
}
