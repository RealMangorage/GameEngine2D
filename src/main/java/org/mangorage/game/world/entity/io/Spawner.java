package org.mangorage.game.world.entity.io;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.item.Item;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class Spawner extends EntityIO {

    public Spawner(World world, BoundingBox box) {
        // Inputs: 0, Outputs: 1, Speed: ~5 sec, Spacing: 0.18
        super(
                Entities.SPAWNER_ENTITY_TYPE,
                world,
                box,
                0,
                1,
                1.0 / 215.0,
                0.18
        );
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            outputs.clear();
            items.clear();
            return;
        }

        // Manual spawn logic: ensure there is a connection and room at the entrance
        if (!outputs.isEmpty() && canAcceptNewItem()) {
            // We use the same internal logic to queue an item
            acceptItem(new Item("MangoItem"));
        }
    }

    @Override
    public void render(RenderContext ctx) {
        renderConnectionsAndItems(ctx, Color.GRAY, Color.YELLOW, 16);

        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(Color.BLUE);
            g.fillRect(b.x(), b.y(), b.width(), b.height());
        });
    }
}