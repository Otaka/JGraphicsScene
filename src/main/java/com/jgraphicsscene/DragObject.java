package com.jgraphicsscene;

public class DragObject<T> {
    private T userObject;
    private float dx;
    private float dy;

    public DragObject(T userObject) {
        this.userObject = userObject;
    }

    public DragObject() {
    }

    public void initPosition(float objX, float objY, float pointerX, float pointerY) {
        dx = pointerX - objX;
        dy = pointerY - objY;
    }

    public float calculateNewX(float pointerX) {
        return pointerX - dx;
    }

    public float calculateNewY(float pointerY) {
        return pointerY - dy;
    }

    public T getUserObject() {
        return userObject;
    }

    @Override
    public String toString() {
        return "Drag dx=" + dx + " dy=" + dy;
    }
}