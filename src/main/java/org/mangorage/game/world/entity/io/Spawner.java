package org.mangorage.game.world.entity.io;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.resource.item.Item;
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

        if (!outputs.isEmpty() && canAcceptNewItem()) {
            Point c = getCenter();
            acceptItem(new Item("MangoItem"), new org.mangorage.game.world.pos.Position(c.x, c.y, 0));
        }
    }

    @Override
    public void render(RenderContext ctx) {
        renderConnectionsAndItems(ctx, Color.GRAY, Color.YELLOW, 16);

        ctx.submit(g -> {
            Position pos = getPosition();
            var box = getBoundingBox();

            g.setColor(Color.BLUE);

            // Render using LOCAL bounding box + WORLD position (rotation-aware)
            if (box.parts().isEmpty()) {
                int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
                int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
                g.fillRect(pos.x(), pos.y(), w, h);
            } else {
                for (var p : box.parts(getFacing())) {
                    g.fillRect(
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