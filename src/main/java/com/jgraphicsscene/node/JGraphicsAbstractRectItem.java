package com.jgraphicsscene.node;

import com.jgraphicsscene.ManipulatorProcessor;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public abstract class JGraphicsAbstractRectItem extends JGraphicsItem {
    protected float xOffset = 0;
    protected float yOffset = 0;
    protected float width = -1;
    protected float height = -1;

    public float getXOffset() {
        return xOffset;
    }

    public JGraphicsAbstractRectItem setXOffset(float xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    public float getYOffset() {
        return yOffset;
    }

    public JGraphicsAbstractRectItem setYOffset(float yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    @Override
    public float getWidth() {
        return width;
    }

    public JGraphicsAbstractRectItem setWidth(float width) {
        this.width = width;
        return this;
    }

    @Override
    public float getHeight() {
        return height;
    }

    public JGraphicsAbstractRectItem setHeight(float height) {
        this.height = height;
        return this;
    }

    @Override
    public Shape createBoundingShape() {
        return new Rectangle.Float(xOffset, yOffset, getWidth(), getHeight());
    }

    private void recalculateManipulatorsPositions(List<ManipulatorProcessor> processors) {
        for (ManipulatorProcessor processor : processors) {
            processor.updateManipulatorPosition();
        }
    }

    @Override
    public List<JGraphicsItem> createManipulators() {
        List<JGraphicsItem> manipulators = new ArrayList<>();
        JGraphicsItem lt = scene.getManipulationHandlerFactory().createManipulationHandler(this);
        JGraphicsItem rt = scene.getManipulationHandlerFactory().createManipulationHandler(this);
        JGraphicsItem lb = scene.getManipulationHandlerFactory().createManipulationHandler(this);
        JGraphicsItem rb = scene.getManipulationHandlerFactory().createManipulationHandler(this);
        List<ManipulatorProcessor> processors = new ArrayList<>();
        processors.add(new ManipulatorProcessor(lt, this, () -> new Point2D.Float(xOffset, yOffset), (manipulator, dx, dy, processor) -> {
            setWidth(width - dx);
            setHeight(height - dy);
            setPosition(getX() + dx, getY() + dy, false);
            recalculateManipulatorsPositions(processors);
        }, 0).start());
        processors.add(new ManipulatorProcessor(rt, this, () -> new Point2D.Float(xOffset + width, yOffset), (manipulator, dx, dy, processor) -> {
            setWidth(width + dx);
            setHeight(height - dy);
            setPosition(getX(), getY() + dy, false);
            recalculateManipulatorsPositions(processors);
        }, 1).start());
        processors.add(new ManipulatorProcessor(lb, this, () -> new Point2D.Float(xOffset, yOffset + height), (manipulator, dx, dy, processor) -> {
            setWidth(width - dx);
            setHeight(height + dy);
            setPosition(getX() + dx, getY(), false);
            recalculateManipulatorsPositions(processors);
        }, 2).start());
        processors.add(new ManipulatorProcessor(rb, this, () -> new Point2D.Float(xOffset + width, yOffset + height), (manipulator, dx, dy, processor) -> {
            setWidth(width + dx);
            setHeight(height + dy);
            recalculateManipulatorsPositions(processors);
        }, 3).start());

        manipulators.add(lt);
        manipulators.add(lb);
        manipulators.add(rt);
        manipulators.add(rb);
        return manipulators;
    }
}
