package org.mangorage.game.render;

import java.awt.Graphics2D;

@FunctionalInterface
public interface RenderCommand {
    void execute(Graphics2D graphics);
}