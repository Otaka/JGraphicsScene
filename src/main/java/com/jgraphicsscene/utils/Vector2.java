package com.jgraphicsscene.utils;

import java.awt.geom.Point2D;

public class Vector2 {
    private float x;
    private float y;

    public Vector2() {
        x = 0;
        y = 0;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create vector from two points
     */
    public Vector2(Point2D start, Point2D end) {
        this.x = (float) (end.getX() - start.getX());
        this.y = (float) (end.getY() - start.getY());
    }

    public Vector2(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    @Override
    public Vector2 clone() {
        Vector2 v = new Vector2();
        v.x = x;
        v.y = y;
        return v;
    }

    public Vector2 convertToNormal() {
        float temp = getX();
        setX(0 - y);
        setY(temp);
        return this;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    public int getXint() {
        return (int) x;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public int getYint() {
        return (int) y;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void set(Vector2 v) {
        x = v.x;
        y = v.y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 v) {
        setX(getX() + v.getX());
        setY(getY() + v.getY());
        return this;
    }

    public Vector2 subtract(Vector2 v) {
        setX(getX() - v.getX());
        setY(getY() - v.getY());
        return this;
    }

    public Vector2 multiple(float value) {
        setX(getX() * value);
        setY(getY() * value);
        return this;
    }

    /**
     * Scalar vector multiplication. The result - cos of angles between vectors<br>
     * For real cases you should normalize vectors before doing scalar multiplication
     */
    public float scalarVectorMultiplication(Vector2 v) {
        return getX() * v.getX() + getY() * v.getY();
    }

    public float angleBetweenVectors(Vector2 v) {
        float dot = scalarVectorMultiplication(v);
        float det = x * v.y - y * v.x;
        return (float) Math.atan2(det, dot);
    }

    /**
     * convert this vector to unit vector(it's length will be 1)
     */
    public Vector2 normalize() {
        if (x == 0 && y == 0) return this;
        multiple(1.0f / length());
        return this;
    }


    public Vector2 getVectorProjection(Vector2 v) {
        v = v.clone();
        v.normalize();
        float projectionLength = scalarVectorMultiplication(v);
        return v.multiple(projectionLength);
    }

    public Vector2 getVectorNormal() {
        Vector2 n = clone();
        float tX = getX();
        n.setX(0 - y);
        n.setY(tX);
        return n;
    }

    public Vector2 reflectVector(Vector2 mirror) {
        Vector2 norm = mirror.getVectorNormal();
        norm.multiple(-1);
        norm.normalize();
        Vector2 projection = getVectorProjection(norm);
        projection.multiple(-2);
        add(projection);
        return this;
    }

    public Vector2 setVectorLength(float length) {
        normalize();
        multiple(length);
        return this;
    }

    public float length() {
        return (float) Math.sqrt(getX() * getX() + getY() * getY());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vector2 other = (Vector2) obj;
        if (!(Math.abs(this.x - other.x) < 0.000001f)) {
            return false;
        }
        return Math.abs(this.y - other.y) < 0.000001f;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (Float.floatToIntBits(this.x) ^ (Float.floatToIntBits(this.x) >>> 16));
        hash = 97 * hash + (Float.floatToIntBits(this.y) ^ (Float.floatToIntBits(this.y) >>> 16));
        return hash;
    }


    @Override
    public String toString() {
        return x + "," + y;
    }
}
