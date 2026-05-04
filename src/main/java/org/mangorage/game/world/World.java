package org.mangorage.game.world;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.misc.InputHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class World {
    private final List<Entity> entities = new ArrayList<>();
    private boolean renderBoundingBoxes = false;

    public World() {
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public void updateInput(InputHandler inputHandler) {
        if (inputHandler.isKeyDown(KeyEvent.VK_F3)) {
            renderBoundingBoxes = !renderBoundingBoxes;
        }

    }

    // Expose whether debug bounding boxes are enabled
    public boolean isRenderBoundingBoxes() {
        return renderBoundingBoxes;
    }

    public void update(double delta) {
        for (Entity e : entities) {
            e.update(delta);
        }
    }

    public void render(RenderContext gameRenderContext) {
        // 1. COLLECT PHASE
        // Every entity (Spawner, RelayBox, etc.) adds their tasks to the context
        for (Entity e : entities) {
            // e.render should ONLY call ctx.submit(...)
            // It should NEVER call ctx.render() or draw to g directly
            e.render(gameRenderContext);
        }

        // 3. DEBUG OVERLAYS (Optional)
        if (renderBoundingBoxes) {
            gameRenderContext.submit(g -> {
                g.setColor(Color.WHITE);
                for (Entity e : entities) {
                    var bb = e.getBoundingBox();
                    var parts = bb.getPartsAbsolute();
                    for (var p : parts) {
                        g.drawRect(p.x(), p.y(), p.width(), p.height());
                    }
                }
            });
        }
    }

    // New method to process clicks
    public Entity handleClick(int worldX, int worldY) {
        for (Entity e : entities) {
            if (e.getBoundingBox().contains(worldX, worldY)) {
                return e;
            }
        }
        return null;
    }
}