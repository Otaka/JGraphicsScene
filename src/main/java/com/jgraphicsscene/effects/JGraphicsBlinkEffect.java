package com.jgraphicsscene.effects;

import com.jgraphicsscene.PaintContext;

public class JGraphicsBlinkEffect extends JGraphicsAbstractEffect {
    private long timestamp = System.currentTimeMillis();
    private Boolean oldRenderingEnabled;
    private boolean blinkHidden = false;

    @Override
    public void beforeRender(PaintContext p) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - timestamp > 300) {
            timestamp = currentTime;
            blinkHidden = !blinkHidden;
        }

        if (blinkHidden) {
            oldRenderingEnabled = p.isRenderingEnabled();
            p.setRenderingEnabled(false);
        } else {
            p.setRenderingEnabled(true);
        }
    }

    @Override
    public void afterRender(PaintContext p) {
        if (oldRenderingEnabled != null) {
            p.setRenderingEnabled(oldRenderingEnabled);
        }
    }
}
