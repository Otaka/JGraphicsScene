package com.jgraphicsscene;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class Utils {
    public static Point2D inverseTransformPoint(Point2D point, AffineTransform transform) {
        try {
            transform.inverseTransform(point, point);
            return point;
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public static AffineTransform createInverted(AffineTransform transform) {
        try {
            return transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public static void normalizeRect(Rectangle rect) {
        if (rect.width < 0) {
            rect.width = rect.width * -1;
            rect.x = rect.x - rect.width;
        }
        if (rect.height < 0) {
            rect.height = rect.height * -1;
            rect.y = rect.y - rect.height;
        }
    }

    public static float lerp(float valueStart, float valueEnd, float t) {
        return valueStart + t * (valueEnd - valueStart);
    }
}