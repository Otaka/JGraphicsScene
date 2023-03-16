package com.jgraphicsscene.events;

public class MouseEvent {
    private final int type;
    private final int button;
    private final int modifier;
    private float x;
    private float y;
    private boolean stopPropagation = false;

    public MouseEvent(int type, float x, float y, int button, int modifier) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.button = button;
        this.modifier = modifier;
    }

    public int getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getButton() {
        return button;
    }

    public int getModifier() {
        return modifier;
    }

    public void stopPropagation() {
        stopPropagation = true;
    }

    public boolean isStopPropagation() {
        return stopPropagation;
    }
}