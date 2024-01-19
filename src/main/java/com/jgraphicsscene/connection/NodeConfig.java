package com.jgraphicsscene.connection;

import com.jgraphicsscene.node.JGraphicsItem;

import java.util.ArrayList;
import java.util.List;

public class NodeConfig {
    private final JGraphicsItem item;
    private final List<TargetConfig> targets = new ArrayList<>();

    public NodeConfig(JGraphicsItem item) {
        this.item = item;
    }

    public JGraphicsItem getItem() {
        return item;
    }

    public List<TargetConfig> getTargets() {
        return targets;
    }

    public TargetConfig getTargetById(int id) {
        for (TargetConfig target : targets)
            if (target.getId() == id)
                return target;
        return null;
    }
}
