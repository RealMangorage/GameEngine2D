package org.mangorage.game.client.render.core;

import java.awt.Graphics2D;

@FunctionalInterface
public interface RenderCommand {
    void execute(Graphics2D graphics);
}