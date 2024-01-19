package com.jgraphicsscene.effects;

import com.jgraphicsscene.PaintContext;

import java.awt.Color;

public class JGraphicsXorColorizeEffect extends JGraphicsAbstractEffect {
    private final Color color;

    public JGraphicsXorColorizeEffect(Color color) {
        this.color = color;
    }

    @Override
    public void beforeRender(PaintContext p) {
        p.setXORMode(color);
    }

    @Override
    public void afterRender(PaintContext p) {
        p.restoreXORMode();
    }
}
