package org.mangorage.game.client.layer;

import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.client.render.core.RenderContext;

import java.awt.*;
import java.util.Queue;

public abstract class Layer {
    private final RenderContext renderContext = new RenderContext();

    public final void render(Graphics2D graphics) {
        render(renderContext);
        renderContext.render(graphics);
    }

    abstract public void update(double delta);
    abstract public void render(RenderContext context);
    abstract public void handleInput(double delta, Queue<GameMouseEvent> mouseEvents);
}