package com.jgraphicsscene.connection;

import com.jgraphicsscene.node.JGraphicsItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetConfig {
    private final int id;
    private final Set<String> userTags = new HashSet<>();
    private final JGraphicsItem item;
    private final List<ConnectionConfig> outConnections = new ArrayList<>();
    private final List<ConnectionConfig> inConnections = new ArrayList<>();
    /**
     * Owner node of this target
     */
    private JGraphicsItem node;

    public TargetConfig(int id, JGraphicsItem item, JGraphicsItem node) {
        this.id = id;
        this.item = item;
        this.node = node;
    }


    public int getId() {
        return id;
    }

    public Set<String> getUserTags() {
        return userTags;
    }

    public TargetConfig setUserTags(String... tags) {
        userTags.clear();
        Collections.addAll(userTags, tags);
        return this;
    }

    public boolean hasUserTag(String userTag) {
        return userTags.contains(userTag);
    }

    public boolean hasUserTags(String... userTags) {
        for (String userTag : userTags)
            if (!this.userTags.contains(userTag))
                return false;
        return true;
    }


    public JGraphicsItem getItem() {
        return item;
    }

    public JGraphicsItem getOwnerNode() {
        return node;
    }

    public TargetConfig setNode(JGraphicsItem node) {
        this.node = node;
        return this;
    }

    public List<ConnectionConfig> getInConnections() {
        return inConnections;
    }

    public List<ConnectionConfig> getOutConnections() {
        return outConnections;
    }
}
