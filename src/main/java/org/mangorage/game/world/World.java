package org.mangorage.game.world;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.entity.RelayBox;
import org.mangorage.game.world.entity.Spawner;
import org.mangorage.game.world.entity.Trash;
import org.mangorage.game.world.misc.InputHandler;
import org.mangorage.game.world.misc.Location;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class World {

    public static void setupBasicLogisticsChain(World world) {
        Spawner spawner = Entities.SPAWNER_ENTITY_TYPE.create(world, new Location(20, 50));
        Spawner spawner2 = Entities.SPAWNER_ENTITY_TYPE.create(world, new Location(20, 170));

        RelayBox relayBox1 = Entities.RELAY_BOX_ENTITY_TYPE.create(world, new Location(150, 100));
        RelayBox relayBox2 = Entities.RELAY_BOX_ENTITY_TYPE.create(world, new Location(250, 100));
        Trash trash = Entities.TRASH_ENTITY_TYPE.create(world, new Location(350, 250));

        world.addEntity(spawner);

        world.addEntity(relayBox1);
        world.addEntity(relayBox2);
        world.addEntity(trash);

        world.addEntity(spawner2);

        System.out.println("Logistics chain initialized: Spawner -> Wire1 -> Wire2 -> Trash");
    }

    private final List<Entity> entities = new ArrayList<>();
    private boolean renderBoundingBoxes = false;

    private double lastCheckedInput = 0;


    public World() {
        setupBasicLogisticsChain(this);
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
                    g.drawRect(bb.x(), bb.y(), bb.width(), bb.height());
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