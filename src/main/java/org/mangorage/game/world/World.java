package org.mangorage.game.world;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.misc.InputHandler;
import org.mangorage.game.world.pos.Position;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class World {

    private final List<Entity> entities = new ArrayList<>();
    private boolean renderBoundingBoxes = false;

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

    public boolean isRenderBoundingBoxes() {
        return renderBoundingBoxes;
    }

    public void update(double delta) {
        for (Entity e : entities) {
            e.update(delta);
        }
    }

    public void render(RenderContext ctx) {
        for (Entity e : entities) {
            e.render(ctx);
        }

        if (renderBoundingBoxes) {
            ctx.submit(g -> {
                g.setColor(Color.WHITE);

                for (Entity e : entities) {
                    Position pos = e.getPosition();

                    var bb = e.getBoundingBox();

                    for (var p : bb.parts()) {
                        g.drawRect(
                                pos.x() + p.offsetX(),
                                pos.y() + p.offsetY(),
                                p.width(),
                                p.height()
                        );
                    }
                }
            });
        }
    }

    // ----------------------------
    // FIXED CLICK HANDLING
    // ----------------------------
    public Entity handleClick(int worldX, int worldY) {
        for (Entity e : entities) {

            Position pos = e.getPosition();

            if (e.getBoundingBox().contains(pos.x(), pos.y(), worldX, worldY)) {
                return e;
            }
        }
        return null;
    }

    public Entity getEntityAt(int x, int y) {
        return handleClick(x, y);
    }
}