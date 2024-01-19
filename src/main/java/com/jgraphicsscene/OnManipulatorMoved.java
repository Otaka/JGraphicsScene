package com.jgraphicsscene;

import com.jgraphicsscene.node.JGraphicsItem;

public interface OnManipulatorMoved {
    /*
    If manipulator is moved this event will be executed and dx and dy will be sent to the listener. They are translated to the manipulator owner space
     */
    void manipulatorMoved(JGraphicsItem manipulator, float dx, float dy, ManipulatorProcessor processor);
}
