package com.jgraphicsscene.events;

public class MouseEvent {
    private final int type;
    private final int button;
    private final int modifier;
    private float x;
    private float y;
    private final int clickCount;
    private boolean stopPropagation = false;

    public MouseEvent(int type, float x, float y, int button, int modifier) {
        this(type, x, y, button, modifier, 0);
    }

    public MouseEvent(int type, float x, float y, int button, int modifier, int clickCount) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.button = button;
        this.modifier = modifier;
        this.clickCount = clickCount;
    }

    public int getClickCount() {
        return clickCount;
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

    @Override
    public String toString() {
        return "MouseEvent{" +
                "type=" + type +
                ", button=" + button +
                ", modifier=" + modifier +
                ", x=" + x +
                ", y=" + y +
                ", stopPropagation=" + stopPropagation +
                '}';
    }
}