package com.jgraphicsscene.effects;

import com.jgraphicsscene.PaintContext;

public abstract class JGraphicsAbstractEffect {
    public abstract void beforeRender(PaintContext g);

    public abstract void afterRender(PaintContext g);
}
