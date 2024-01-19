package com.jgraphicsscene.utils;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class Transform {
    private final AffineTransform transform;
    private int version;
    private boolean dirtyTransform = true;

    public Transform() {
        transform = new AffineTransform();
        transform.setToIdentity();
        version = 0;
    }

    public Transform(AffineTransform transform) {
        this();
        this.transform.setTransform(transform);
    }

    public static AffineTransform createInvertedAffineTransform(AffineTransform transform) {
        try {
            return transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFromTransform(Transform transform) {
        this.transform.setTransform(transform.getAffineTransform());
    }

    public void concatenate(Transform transform) {
        this.transform.concatenate(transform.getAffineTransform());
    }

    public boolean isDirty() {
        return dirtyTransform;
    }

    public void clearDirty() {
        dirtyTransform = false;
    }

    public void setDirty() {
        this.dirtyTransform = true;
        version++;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Get internal AffineTransform matrix
     */
    public AffineTransform getAffineTransform() {
        return transform;
    }

    public Shape transformImmutable(Shape shape) {
        return transform.createTransformedShape(shape);
    }

    public Point2D transformMut(Point2D point) {
        return transform.transform(point, point);
    }

    public Point2D transformImmutable(Point2D point) {
        return transform.transform(point, new Point2D.Float());
    }

    public Point2D transform(Point2D src, Point2D dest) {
        return transform.transform(src, dest);
    }

    public Point2D inverseTransformPointMut(Point2D point) {
        try {
            transform.inverseTransform(point, point);
            return point;
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public Point2D inverseTransformPoint(Point2D src, Point2D dest) {
        try {
            transform.inverseTransform(src, dest);
            return dest;
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public void translate(double tx, double ty) {
        transform.translate(tx, ty);
    }

    public void setToScale(double sx, double sy) {
        transform.setToScale(sx, sy);
    }

    public void rotate(double theta) {
        transform.rotate(theta);
    }

    public void scale(double sx, double sy) {
        transform.scale(sx, sy);
    }

    public void setToIdentity() {
        transform.setToIdentity();
    }

    public void concatenate(AffineTransform Tx) {
        transform.concatenate(Tx);
    }

    public void invert() throws NoninvertibleTransformException {
        transform.invert();
    }

    public boolean isIdentity() {
        return transform.isIdentity();
    }
}
